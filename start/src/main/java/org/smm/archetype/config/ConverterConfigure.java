package org.smm.archetype.config;

import org.smm.archetype.infrastructure._shared.event.EventConsumeRecordConverter;
import org.smm.archetype.infrastructure.common.file.FileBusinessConverter;
import org.smm.archetype.infrastructure.common.file.FileMetaConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 转换器配置
 *
 * <p>职责：
 * <ul>
 *   <li>注册共享的转换器Bean（不属于特定聚合根的转换器）</li>
 *   <li>集中管理转换器的依赖注入</li>
 *   <li>提供条件装配支持（@ConditionalOnMissingBean）</li>
 * </ul>
 *
 * <p>设计原则：
 * <ul>
 *   <li>使用 @Configuration + @Bean 模式（而非 @Component）</li>
 *   <li>配置类位于 start/src/main/java/org/smm/archetype/config/</li>
 *   <li>特定聚合根的转换器在对应的聚合根配置类中注册（如 OrderConfigure）</li>
 * </ul>
 *
 * <p>为什么使用配置类而非 @Component：
 * <ul>
 *   <li>显式优于隐式：通过配置类显式注册Bean，便于查看和管理</li>
 *   <li>符合规范：遵循《业务代码编写规范》第2.1节</li>
 *   <li>统一管理：所有转换器Bean在配置类中集中注册</li>
 * </ul>
 * @author Leonardo
 * @since 2026-01-12
 */
@Configuration
public class ConverterConfigure {

    // ==================== 文件转换器 ====================

    /**
     * 文件元数据转换器Bean
     * <p>职责：FileMetadata与FileMetadataDO之间的转换
     * @return FileMetaConverter
     */
    @Bean
    @ConditionalOnMissingBean(FileMetaConverter.class)
    public FileMetaConverter fileMetaConverter() {
        return new FileMetaConverter();
    }

    /**
     * 业务文件转换器Bean
     * <p>职责：FileBusiness与FileBusinessDO之间的转换
     * @return FileBusinessConverter
     */
    @Bean
    @ConditionalOnMissingBean(FileBusinessConverter.class)
    public FileBusinessConverter fileBusinessConverter() {
        return new FileBusinessConverter();
    }

    // ==================== 事件转换器 ====================

    /**
     * 事件消费记录转换器Bean
     * <p>职责：EventConsumeRecord与EventConsumeDO之间的转换
     * @return EventConsumeRecordConverter
     */
    @Bean
    @ConditionalOnMissingBean(EventConsumeRecordConverter.class)
    public EventConsumeRecordConverter eventConsumeRecordConverter() {
        return new EventConsumeRecordConverter();
    }

}
