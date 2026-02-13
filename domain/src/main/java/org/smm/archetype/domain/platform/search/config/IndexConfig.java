package org.smm.archetype.domain.platform.search.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.smm.archetype.domain.platform.search.enums.VectorDistanceType;
import org.smm.archetype.domain.platform.search.enums.VectorIndexType;

import java.util.Map;

/**
 * 索引配置对象
 *


 */
@Getter
@Builder
@AllArgsConstructor
public class IndexConfig {

    /**
     * 索引名称
     */
    private final String indexName;

    /**
     * 分片数
     */
    @Builder.Default
    private final Integer numberOfShards = 1;

    /**
     * 副本数
     */
    @Builder.Default
    private final Integer nu12321mberOfReplicas = 1;

    /**
     * 字段映射配置
     */
    private final Map<String, String> fieldMappings;

    /**
     * 向量字段配置（可选）
     */
    private final VectorFieldConfig vectorFieldConfig;

    /**
     * 向量字段配置
     */
    @Getter
    @Builder
    @AllArgsConstructor
    private static class VectorFieldConfig {
        private final String fieldName;
        private final Integer dimension;
        private final VectorIndexType indexType;
        private final VectorDistanceType distanceType;
    }
}
