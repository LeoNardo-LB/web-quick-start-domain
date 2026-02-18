package org.smm.archetype.infrastructure.platform.file.persistence;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain.platform.file.FileBusiness;
import org.smm.archetype.domain.platform.file.FileMetadata;
import org.smm.archetype.domain.platform.file.FileRepository;
import org.smm.archetype.infrastructure.platform.file.FileBusinessConverter;
import org.smm.archetype.infrastructure.platform.file.FileMetaConverter;
import org.smm.archetype.infrastructure.shared.dal.generated.entity.FileBusinessDO;
import org.smm.archetype.infrastructure.shared.dal.generated.entity.FileMetadataDO;
import org.smm.archetype.infrastructure.shared.dal.generated.mapper.FileBusinessMapper;
import org.smm.archetype.infrastructure.shared.dal.generated.mapper.FileMetadataMapper;

import java.util.List;
import java.util.Optional;

/**
 * 通用文件仓储实现
 *
 负责FileBusiness和FileMeta的持久化操作
 
 
 */
@Slf4j
@RequiredArgsConstructor
public class FileRepositoryImpl implements FileRepository {

    private final FileBusinessMapper    businessMapper;
    private final FileMetadataMapper    metadataMapper;
    private final FileBusinessConverter fileBusinessConverter;
    private final FileMetaConverter     fileMetaConverter;

    @Override
    public FileBusiness save(FileBusiness fileBusiness) {
        log.debug("Saving business file: businessId={}, type={}, usage={}",
                fileBusiness.getBusinessId(),
                fileBusiness.getType(),
                fileBusiness.getUsage());

        // 1. 保存文件元数据（使用原子操作 upsertByMd5）
        FileMetadata fileMetadata = fileBusiness.getFileMetadata();
        FileMetadataDO metadataDO = fileMetaConverter.toDataObject(fileMetadata);
        metadataMapper.upsertByMd5(metadataDO);

        // 2. 保存业务关联（使用原子操作 upsertById）
        FileBusinessDO businessDO = fileBusinessConverter.toDataObject(fileBusiness);
        businessDO.setFileMetaId(String.valueOf(metadataDO.getId()));
        businessMapper.upsertById(businessDO);

        log.debug("Business file saved successfully: id={}", businessDO.getId());
        return fileBusiness;
    }

    @Override
    public Optional<FileBusiness> findById(String id) {
        log.debug("Finding business file by id: {}", id);

        // 从file_business表查询
        FileBusinessDO businessDO = businessMapper.selectById(id);
        if (businessDO == null) {
            log.warn("Business file not found: id={}", id);
            return Optional.empty();
        }

        // 查询文件元数据 - 使用 id 关联
        FileMetadataDO metadataDO = metadataMapper.selectOne(
                Wrappers.<FileMetadataDO>lambdaQuery()
                        .eq(FileMetadataDO::getId, Long.parseLong(businessDO.getFileMetaId()))
        );

        if (metadataDO == null) {
            log.warn("File metadata not found: fileMetaId={}", businessDO.getFileMetaId());
            return Optional.empty();
        }

        // 转换为领域对象
        FileBusiness fileBusiness = toFileBusiness(businessDO, metadataDO);
        return Optional.of(fileBusiness);
    }

    @Override
    public List<FileBusiness> findByBusinessIdAndTypeAndUsage(String businessId,
                                                              FileBusiness.Type type,
                                                              FileBusiness.Usage usage) {
        log.debug("Finding business files: businessId={}, type={}, usage={}", businessId, type, usage);

        // 从file_business表查询
        List<FileBusinessDO> businessDOList = businessMapper.selectList(
                Wrappers.<FileBusinessDO>lambdaQuery()
                        .eq(FileBusinessDO::getBusinessId, businessId)
                        .eq(FileBusinessDO::getType, type.name())
                        .eq(FileBusinessDO::getUsage, usage.name())
                        .orderByAsc(FileBusinessDO::getSort)
        );

        // 转换为领域对象
        return businessDOList.stream()
                       .map(this::toFileBusinessWithMetadata)
                       .filter(Optional::isPresent)
                       .map(Optional::get)
                       .toList();
    }

    @Override
    public Optional<FileMetadata> findFileMetaByFileId(String fileId) {
        log.debug("Finding file meta by id: {}", fileId);

        FileMetadataDO metadataDO = metadataMapper.selectOne(
                Wrappers.<FileMetadataDO>lambdaQuery()
                        .eq(FileMetadataDO::getId, Long.parseLong(fileId))
        );

        if (metadataDO == null) {
            log.warn("File metadata not found: id={}", fileId);
            return Optional.empty();
        }

        FileMetadata fileMetadata = fileMetaConverter.toEntity(metadataDO);
        return Optional.of(fileMetadata);
    }

    @Override
    public void deleteById(String id) {
        log.debug("Deleting business file: id={}", id);

        // 删除业务关联
        businessMapper.deleteById(id);

        log.debug("Business file deleted successfully: id={}", id);
    }

    // ==================== 私有方法 ====================

    /**
     * 将FileBusinessDO和FileMetadataDO转换为FileBusiness
     */
    private FileBusiness toFileBusiness(FileBusinessDO businessDO, FileMetadataDO metadataDO) {
        FileBusiness fileBusiness = fileBusinessConverter.toEntity(businessDO);
        FileMetadata fileMetadata = fileMetaConverter.toEntity(metadataDO);
        fileBusiness.setFileMetadata(fileMetadata);
        return fileBusiness;
    }

    /**
     * 将FileBusinessDO转换为FileBusiness（包含元数据）
     */
    private Optional<FileBusiness> toFileBusinessWithMetadata(FileBusinessDO businessDO) {
        FileMetadataDO metadataDO = metadataMapper.selectOne(
                Wrappers.<FileMetadataDO>lambdaQuery()
                        .eq(FileMetadataDO::getId, Long.parseLong(businessDO.getFileMetaId()))
        );

        if (metadataDO == null) {
            log.warn("File metadata not found for business file: fileMetaId={}", businessDO.getFileMetaId());
            return Optional.empty();
        }

        return Optional.of(toFileBusiness(businessDO, metadataDO));
    }

}
