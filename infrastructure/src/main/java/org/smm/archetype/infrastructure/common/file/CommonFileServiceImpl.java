package org.smm.archetype.infrastructure.common.file;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain._shared.client.IdClient;
import org.smm.archetype.domain._shared.client.OssClient;
import org.smm.archetype.domain.common.file.CommonFileRepository;
import org.smm.archetype.domain.common.file.CommonFileService;
import org.smm.archetype.domain.common.file.FileBusiness;
import org.smm.archetype.domain.common.file.FileMetadata;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;

/**
 * 通用文件服务实现
 *
 * <p>整合OssClient、CommonFileRepository，提供完整的文件管理功能
 * @author Leonardo
 * @since 2026/01/10
 */
@Slf4j
@RequiredArgsConstructor
public class CommonFileServiceImpl implements CommonFileService {

    private final OssClient            ossClient;
    private final CommonFileRepository commonFileRepository;
    private final IdClient             idClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadFile(InputStream inputStream, FileMetadata fileMetadata, FileBusiness fileBusiness) {
        log.info("Uploading file: fileName={}, businessId={}, type={}, usage={}",
                fileMetadata.getFileName(),
                fileBusiness.getBusinessId(),
                fileBusiness.getType(),
                fileBusiness.getUsage());

        // 1. 上传文件到对象存储
        String filePath = ossClient.upload(inputStream, fileMetadata.getFileName(), fileMetadata.getContentType());
        log.debug("File uploaded to OSS: filePath={}", filePath);

        // 2. 设置文件元数据
        fileMetadata.setFilePath(filePath);
        fileMetadata.setFileUrl(ossClient.generateUrl(filePath, 0)); // 永久有效

        // 3. 设置业务文件关联
        fileBusiness.setFileMetadata(fileMetadata);

        // 4. 保存到数据库（ID由数据库自动生成）
        commonFileRepository.save(fileBusiness);

        log.info("File uploaded successfully: businessId={}", fileBusiness.getBusinessId());
    }

    @Override
    public List<FileBusiness> listFileBusinesss(String businessId,
                                                FileBusiness.Type type,
                                                FileBusiness.Usage usage) {
        log.debug("Listing business files: businessId={}, type={}, usage={}", businessId, type, usage);

        return commonFileRepository.findByBusinessIdAndTypeAndUsage(businessId, type, usage);
    }

    @Override
    public FileBusiness getFileBusiness(String id) {
        log.debug("Getting business file: id={}", id);

        return commonFileRepository.findById(id)
                       .orElseThrow(() -> new IllegalArgumentException("Business file not found: " + id));
    }

    @Override
    public FileMetadata getFileMeta(String id) {
        log.debug("Getting file meta: id={}", id);

        return commonFileRepository.findFileMetaByFileId(id)
                       .orElseThrow(() -> new IllegalArgumentException("File meta not found: " + id));
    }

    @Override
    public String getFileUrl(String fileMetaId) {
        log.debug("Getting file URL: fileMetaId={}", fileMetaId);

        FileMetadata fileMetadata = getFileMeta(fileMetaId);
        String url = ossClient.generateUrl(fileMetadata.getFilePath(), 0); // 永久有效

        log.debug("File URL generated: fileId={}, url={}", fileMetaId, url);
        return url;
    }

}
