package org.smm.archetype.config.logging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 日志配置验证类
 *
 * <p>在应用启动时验证日志配置的正确性，包括：
 * <ul>
 *   <li>日志路径配置检查</li>
 *   <li>日志目录存在性验证</li>
 *   <li>日志目录可写性验证</li>
 *   <li>权限问题检测和处理</li>
 * </ul>
 *
 * <p>如果检测到配置问题，会记录日志并根据严重程度决定是否启动失败。
 */
@Slf4j
public class LoggingConfiguration implements ApplicationListener<ApplicationReadyEvent> {

    private static final String LOG_PATH_PROPERTY = "logging.file.path";
    private static final String DEFAULT_LOG_PATH  = ".logs";

    private final Environment environment;

    public LoggingConfiguration(Environment environment) {
        this.environment = environment;
    }

    /**
     * 应用启动完成事件处理
     * @param event 应用启动完成事件
     */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("[CONFIG] 开始验证日志配置...");

        String logPath = environment.getProperty(LOG_PATH_PROPERTY, DEFAULT_LOG_PATH);
        log.info("[CONFIG] 日志路径配置: {}", logPath);

        Path logDirPath = Paths.get(logPath);

        // 检查日志目录存在性
        checkLogDirectoryExists(logDirPath);

        // 检查日志目录可写性
        checkLogDirectoryWritable(logDirPath);

        // 检查磁盘空间
        checkDiskSpace(logDirPath);

        log.info("[CONFIG] 日志配置验证完成 | 路径:{} | 状态:正常", logDirPath.toAbsolutePath());
    }

    /**
     * 检查日志目录是否存在
     * @param logDirPath 日志目录路径
     */
    private void checkLogDirectoryExists(Path logDirPath) {
        if (!Files.exists(logDirPath)) {
            log.warn("[CONFIG] 日志目录不存在，尝试创建: {}", logDirPath.toAbsolutePath());
            try {
                Files.createDirectories(logDirPath);
                log.info("[CONFIG] 日志目录创建成功: {}", logDirPath.toAbsolutePath());
            } catch (Exception e) {
                log.error("[CONFIG] 无法创建日志目录: {} | 错误: {}", logDirPath.toAbsolutePath(), e.getMessage());
                throw new IllegalStateException("无法创建日志目录: " + logDirPath.toAbsolutePath(), e);
            }
        } else {
            log.info("[CONFIG] 日志目录已存在: {}", logDirPath.toAbsolutePath());
        }
    }

    /**
     * 检查日志目录是否可写
     * @param logDirPath 日志目录路径
     */
    private void checkLogDirectoryWritable(Path logDirPath) {
        if (!Files.isWritable(logDirPath)) {
            String errorMsg = "日志目录不可写: " + logDirPath.toAbsolutePath() +
                                      " | 请检查目录权限";
            log.error("[CONFIG] {}", errorMsg);
            throw new IllegalStateException(errorMsg);
        }

        // 尝试创建临时文件验证可写性
        Path testFile = logDirPath.resolve(".log-writable-test");
        try {
            Files.writeString(testFile, "test");
            Files.deleteIfExists(testFile);
            log.info("[CONFIG] 日志目录可写性验证通过");
        } catch (Exception e) {
            String errorMsg = "日志目录可写性测试失败: " + logDirPath.toAbsolutePath() +
                                      " | 错误: " + e.getMessage();
            log.error("[CONFIG] {}", errorMsg);
            throw new IllegalStateException(errorMsg, e);
        }
    }

    /**
     * 检查磁盘空间
     * @param logDirPath 日志目录路径
     */
    private void checkDiskSpace(Path logDirPath) {
        try {
            long freeSpaceBytes = logDirPath.toFile().getFreeSpace();
            long freeSpaceMB = freeSpaceBytes / (1024 * 1024);
            long freeSpaceGB = freeSpaceMB / 1024;

            if (freeSpaceMB < 500) {
                log.warn("[CONFIG] 日志目录磁盘空间不足: {}MB | 建议至少预留 500MB", freeSpaceMB);
            } else {
                log.info("[CONFIG] 日志目录磁盘空间充足: {}GB 可用", freeSpaceGB);
            }
        } catch (Exception e) {
            log.warn("[CONFIG] 无法检查磁盘空间: {} | 错误: {}", logDirPath.toAbsolutePath(), e.getMessage());
        }
    }

}
