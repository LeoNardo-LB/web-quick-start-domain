package org.smm.archetype.infrastructure.platform.file;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain.platform.file.FileRepository;
import org.smm.archetype.domain.platform.file.FileBusiness;
import org.smm.archetype.domain.platform.file.FileMetadata;
import org.smm.archetype.infrastructure.shared.dal.generated.entity.FileBusinessDO;
import org.smm.archetype.infrastructure.shared.dal.generated.entity.FileMetadataDO;
import org.smm.archetype.infrastructure.shared.dal.generated.mapper.FileBusinessMapper;
import org.smm.archetype.infrastructure.shared.dal.generated.mapper.FileMetadataMapper;

import java.util.List;
import java.util.Optional;

import static org.smm.archetype.infrastructure.shared.dal.generated.entity.table.FileBusinessDOTableDef.FILE_BUSINESS_DO;
import static org.smm.archetype.infrastructure.shared.dal.generated.entity.table.FileMetadataDOTableDef.FILE_METADATA_DO;

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

        // 1. 保存文件元数据
        FileMetadata fileMetadata = fileBusiness.getFileMetadata();
        FileMetadataDO metadataDO = fileMetaConverter.toDataObject(fileMetadata);
        metadataMapper.insertOrUpdate(metadataDO);

        // 2. 保存业务关联（使用 file_metadata.id 作为关联）
        FileBusinessDO businessDO = fileBusinessConverter.toDataObject(fileBusiness);
        businessDO.setFileMetaId(String.valueOf(metadataDO.getId()));
        businessMapper.insertOrUpdate(businessDO);

        log.debug("Business file saved successfully: id={}", businessDO.getId());
        return fileBusiness;
    }

    @Override
    public Optional<FileBusiness> findById(String id) {
        log.debug("Finding business file by id: {}", id);

        // 从file_business表查询
        FileBusinessDO businessDO = businessMapper.selectOneById(id);
        if (businessDO == null) {
            log.warn("Business file not found: id={}", id);
            return Optional.empty();
        }

        // 查询文件元数据 - 使用 id 关联
        FileMetadataDO metadataDO = metadataMapper.selectOneByQuery(
                QueryWrapper.create()
                        .select()
                        .from(FILE_METADATA_DO)
                        .where(FILE_METADATA_DO.ID.eq(Long.parseLong(businessDO.getFileMetaId())))
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

        // 暂时只按type和usage查询，不按businessId过滤
        // 从file_business表查询
        QueryWrapper query = QueryWrapper.create()
                                     .select()
                                     .from(FILE_BUSINESS_DO)
                                     .where(FILE_BUSINESS_DO.BUSINESS_ID.eq(businessId))  // 取消注释：表结构中缺少business_id字段
                                     .where(FILE_BUSINESS_DO.TYPE.eq(type.name()))
                                     .and(FILE_BUSINESS_DO.USAGE.eq(usage.name()))
                                     .orderBy(FILE_BUSINESS_DO.SORT.asc());

        List<FileBusinessDO> businessDOList = businessMapper.selectListByQuery(query);

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

        FileMetadataDO metadataDO = metadataMapper.selectOneByQuery(
                QueryWrapper.create()
                        .select()
                        .from(FILE_METADATA_DO)
                        .where(FILE_METADATA_DO.ID.eq(Long.parseLong(fileId)))
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
        FileMetadataDO metadataDO = metadataMapper.selectOneByQuery(
                QueryWrapper.create()
                        .select()
                        .from(FILE_METADATA_DO)
                        .where(FILE_METADATA_DO.ID.eq(Long.parseLong(businessDO.getFileMetaId())))
        );

        if (metadataDO == null) {
            log.warn("File metadata not found for business file: fileMetaId={}", businessDO.getFileMetaId());
            return Optional.empty();
        }

        return Optional.of(toFileBusiness(businessDO, metadataDO));
    }

}
