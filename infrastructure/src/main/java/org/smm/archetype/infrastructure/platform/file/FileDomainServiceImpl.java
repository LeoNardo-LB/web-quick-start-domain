package org.smm.archetype.infrastructure.platform.file;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain.shared.client.OssClient;
import org.smm.archetype.domain.platform.file.FileBusiness;
import org.smm.archetype.domain.platform.file.FileDomainService;
import org.smm.archetype.domain.platform.file.FileMetadata;
import org.smm.archetype.domain.platform.file.FileRepository;

import java.io.InputStream;
import java.util.List;

/**
 * 文件服务实现，整合OSS和仓储，提供完整文件管理。


 */
@Slf4j
@RequiredArgsConstructor
public class FileDomainServiceImpl implements FileDomainService {

    private final OssClient      ossClient;
    private final FileRepository fileRepository;

    @Override
    public void uploadFile(InputStream inputStream, FileMetadata fileMetadata, FileBusiness fileBusiness) {
        log.info("正在上传文件: 文件名={}, 业务ID={}, 类型={}, 用途={}",
                fileMetadata.getFileName(),
                fileBusiness.getBusinessId(),
                fileBusiness.getType(),
                fileBusiness.getUsage());

        // 1. 上传文件到对象存储
        String filePath = ossClient.upload(inputStream, fileMetadata.getFileName(), fileMetadata.getContentType());
        log.debug("文件已上传到OSS: filePath={}", filePath);

        // 2. 设置文件元数据
        fileMetadata.setFilePath(filePath);
        fileMetadata.setFileUrl(ossClient.generateUrl(filePath, 0)); // 永久有效

        // 3. 设置业务文件关联
        fileBusiness.setFileMetadata(fileMetadata);

        // 4. 保存到数据库（ID由数据库自动生成）
        fileRepository.save(fileBusiness);

        log.info("文件上传成功: businessId={}", fileBusiness.getBusinessId());
    }

    @Override
    public List<FileBusiness> listFileBusinesss(String businessId,
                                                FileBusiness.Type type,
                                                FileBusiness.Usage usage) {
        log.debug("Listing business files: businessId={}, type={}, usage={}", businessId, type, usage);

        return fileRepository.findByBusinessIdAndTypeAndUsage(businessId, type, usage);
    }

    @Override
    public FileBusiness getFileBusiness(String id) {
        log.debug("Getting business file: id={}", id);

        return fileRepository.findById(id)
                       .orElseThrow(() -> new IllegalArgumentException("Business file not found: " + id));
    }

    @Override
    public FileMetadata getFileMeta(String id) {
        log.debug("获取文件元数据: id={}", id);

        return fileRepository.findFileMetaByFileId(id)
                       .orElseThrow(() -> new IllegalArgumentException("文件元数据未找到: " + id));
    }

    @Override
    public String getFileUrl(String fileMetaId) {
        log.debug("Getting file URL: fileMetaId={}", fileMetaId);

        FileMetadata fileMetadata = getFileMeta(fileMetaId);
        String url = ossClient.generateUrl(fileMetadata.getFilePath(), 0); // 永久有效

        log.debug("获取文件URL: fileMetaId={}", fileMetaId);
        return url;
    }

}
