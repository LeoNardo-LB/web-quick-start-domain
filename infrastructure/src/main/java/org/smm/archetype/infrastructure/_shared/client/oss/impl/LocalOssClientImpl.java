package org.smm.archetype.infrastructure._shared.client.oss.impl;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain._shared.client.IdClient;
import org.smm.archetype.domain.common.file.File;
import org.smm.archetype.infrastructure._shared.client.oss.AbstractOssClient;
import org.smm.archetype.infrastructure._shared.generated.entity.FileBusinessDO;
import org.smm.archetype.infrastructure._shared.generated.entity.FileMetadataDO;
import org.smm.archetype.infrastructure._shared.generated.mapper.FileBusinessMapper;
import org.smm.archetype.infrastructure._shared.generated.mapper.FileMetadataMapper;
import org.smm.archetype.infrastructure.common.file.config.OssProperties;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.smm.archetype.infrastructure._shared.generated.entity.table.FileBusinessDOTableDef.FILE_BUSINESS_DO;
import static org.smm.archetype.infrastructure._shared.generated.entity.table.FileMetadataDOTableDef.FILE_METADATA_DO;

/**
 * 本地文件系统对象存储服务实现
 *
 * <p>作为默认兜底实现，使用本地文件系统存储文件。
 *
 * <h3>特性</h3>
 * <ul>
 *   <li>零拷贝上传/下载（使用 NIO FileChannel.transferTo）</li>
 *   <li>顺序存储（按日期分层：yyyy/MM）</li>
 *   <li>总是可用，不依赖外部服务</li>
 * </ul>
 *
 * <h3>存储路径</h3>
 * <pre>
 * 用户文件夹/.project/${spring.application.name}/oss/
 * ├── 2026/
 * │   ├── 01/
 * │   │   ├── file123-test1.txt
 * │   │   └── file456-test2.jpg
 * │   └── 02/
 * │       └── file789-test3.pdf
 * </pre>
 * @author Leonardo
 * @since 2026/1/10
 */
@Slf4j
public class LocalOssClientImpl extends AbstractOssClient {

    private final Path baseStoragePath;

    /**
     * 构造函数
     * @param properties     对象存储配置
     * @param metadataMapper 文件元数据 Mapper
     * @param businessMapper 文件业务 Mapper
     * @param idClient       ID 生成服务
     * @throws IOException 如果创建存储目录失败
     */
    public LocalOssClientImpl(OssProperties properties,
                              FileMetadataMapper metadataMapper,
                              FileBusinessMapper businessMapper,
                              IdClient idClient) throws IOException {
        super(properties, metadataMapper, businessMapper, idClient);

        // 解析基础路径: 用户文件夹/.project/${spring.application.name}/oss
        String userHome = System.getProperty("user.home");
        String appName = System.getProperty("spring.application.name", "quickstart");
        String basePath = properties.getLocal().getBasePath();

        if (basePath != null && !basePath.isBlank()) {
            this.baseStoragePath = Paths.get(basePath);
        } else {
            this.baseStoragePath = Paths.get(userHome, ".project", appName, "oss");
        }

        // 创建目录（如果不存在）
        Files.createDirectories(baseStoragePath);
        log.info("Local object storage initialized: basePath={}", baseStoragePath.toAbsolutePath());
    }

    @Override
    protected String doUpload(byte[] contentBytes, String fileName, String contentType, String fileId) throws Exception {
        // 生成文件路径: {year}/{month}/{fileId}-{fileName}
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        Path targetPath = baseStoragePath.resolve(datePath);

        // 创建日期目录
        Files.createDirectories(targetPath);

        // 完整文件路径
        Path filePath = targetPath.resolve(fileId + "-" + fileName);

        // 零拷贝上传
        if (properties.getLocal().isZeroCopy()) {
            // 先写入临时文件
            Path tempFile = Files.createTempFile("upload-", ".tmp");
            try {
                Files.write(tempFile, contentBytes, StandardOpenOption.WRITE);

                // NIO transferTo 零拷贝
                try (FileChannel sourceChannel = FileChannel.open(tempFile, StandardOpenOption.READ);
                     FileChannel targetChannel = FileChannel.open(filePath,
                             StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {
                    long size = sourceChannel.transferTo(0, sourceChannel.size(), targetChannel);
                    log.debug("Zero-copy upload completed: size={}, path={}", size, filePath);
                }
            } finally {
                // 删除临时文件
                Files.deleteIfExists(tempFile);
            }
        } else {
            // 传统拷贝方式
            Files.write(filePath, contentBytes, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
            log.debug("Traditional upload completed: path={}", filePath);
        }

        // 返回相对路径
        return datePath + "/" + fileId + "-" + fileName;
    }

    @Override
    protected java.io.InputStream doDownload(String filePath, String fileId) throws Exception {
        Path fullPath = baseStoragePath.resolve(filePath);

        if (!Files.exists(fullPath)) {
            throw new FileNotFoundException("File not found: " + filePath);
        }

        // 返回 FileInputStream
        return new FileInputStream(fullPath.toFile());
    }

    @Override
    protected void doDelete(String filePath, String fileId) throws Exception {
        Path fullPath = baseStoragePath.resolve(filePath);

        // 删除文件
        boolean deleted = Files.deleteIfExists(fullPath);

        if (deleted) {
            log.debug("File deleted: path={}", fullPath);
        } else {
            log.warn("File not found for deletion: path={}", fullPath);
        }
    }

    @Override
    protected String doGenerateUrl(String filePath, long expireSeconds) throws Exception {
        // 本地文件使用 file:// 协议
        Path fullPath = baseStoragePath.resolve(filePath);
        return fullPath.toUri().toString();
    }

    @Override
    protected List<File> doSearchFiles(String fileNamePattern, File.FileBusinessEntityType businessEntityType, String businessId)
            throws Exception {
        // 从数据库查询（使用 MyBatis-Flex）
        QueryWrapper query = QueryWrapper.create()
                                     .select()
                                     .from(FILE_BUSINESS_DO)
                                     .where(FILE_BUSINESS_DO.TYPE.eq(businessEntityType.name()))
                                     .and(FILE_BUSINESS_DO.BUSINESS_ID.eq(businessId))
                                     .leftJoin(FILE_METADATA_DO)
                                     .on(FILE_BUSINESS_DO.FILE_ID.eq(FILE_METADATA_DO.FILE_ID));

        // 如果有文件名模式，添加模糊查询
        if (fileNamePattern != null && !fileNamePattern.isBlank()) {
            // 将通配符 * 和 ? 转换为 SQL 的 % 和 _
            String sqlPattern = fileNamePattern.replace("*", "%").replace("?", "_");
            query.and(FILE_METADATA_DO.PATH.like(sqlPattern));
        }

        List<FileBusinessDO> businessDOs = businessMapper.selectListByQuery(query);

        // 转换为 File 领域对象
        return businessDOs.stream()
                       .map(businessDO -> {
                           // 查询对应的元数据
                           FileMetadataDO metadataDO = metadataMapper.selectOneByQuery(
                                   QueryWrapper.create()
                                           .select()
                                           .from(FILE_METADATA_DO)
                                           .where(FILE_METADATA_DO.FILE_ID.eq(businessDO.getFileId()))
                           );
                           return toFile(metadataDO, businessDO);
                       })
                       .toList();
    }

    @Override
    protected boolean doExists(String filePath) throws Exception {
        Path fullPath = baseStoragePath.resolve(filePath);
        return Files.exists(fullPath);
    }

    @Override
    protected long doGetFileSize(String filePath) throws Exception {
        Path fullPath = baseStoragePath.resolve(filePath);

        if (!Files.exists(fullPath)) {
            throw new FileNotFoundException("File not found: " + filePath);
        }

        return Files.size(fullPath);
    }

    // ==================== 辅助方法 ====================

    /**
     * 将 FileMetadataDO + FileBusinessDO 转换为 File 领域对象
     * @param metadataDO 文件元数据DO
     * @param businessDO 文件业务DO
     * @return File 领域对象
     */
    private File toFile(FileMetadataDO metadataDO, FileBusinessDO businessDO) {
        if (metadataDO == null) {
            return null;
        }

        File.FileBuilder fileBuilder = File.builder()
                                               .setFileId(metadataDO.getFileId())
                                               .setFilePath(metadataDO.getPath())
                                               .setFileUrl(metadataDO.getUrl())
                                               .setMd5(metadataDO.getMd5())
                                               .setContentType(metadataDO.getContentType())
                                               .setFileSize(metadataDO.getSize())
                                               .setStatus(File.FileStatus.ACTIVE)
                                               .setCreateTime(metadataDO.getCreateTime())
                                               .setUpdateTime(metadataDO.getUpdateTime());

        // 如果有业务关联信息，则设置
        if (businessDO != null) {
            fileBuilder.setFileName(businessDO.getName())
                    .setFileBusiness(File.FileBusiness.builder()
                                             .setBusinessEntityType(toBusinessEntityType(businessDO.getType()))
                                             .setBusinessId(businessDO.getBusinessId())
                                             .setUsageType(toUsageType(businessDO.getUsage()))
                                             .setRemark(businessDO.getRemark())
                                             .setOrder(businessDO.getSort())
                                             .build());
        }

        return fileBuilder.build();
    }

    /**
     * 将字符串转换为 FileBusinessEntityType 枚举
     * @param type 业务类型字符串
     * @return FileBusinessEntityType 枚举
     */
    private File.FileBusinessEntityType toBusinessEntityType(String type) {
        if (type == null || type.isBlank()) {
            return File.FileBusinessEntityType.OTHER;
        }

        try {
            return File.FileBusinessEntityType.valueOf(type);
        } catch (IllegalArgumentException e) {
            return File.FileBusinessEntityType.OTHER;
        }
    }

    /**
     * 将字符串转换为 UsageType 枚举
     * @param usage 使用场景字符串
     * @return UsageType 枚举
     */
    private File.UsageType toUsageType(String usage) {
        if (usage == null || usage.isBlank()) {
            return File.UsageType.OTHER;
        }

        try {
            return File.UsageType.valueOf(usage);
        } catch (IllegalArgumentException e) {
            return File.UsageType.OTHER;
        }
    }

}
