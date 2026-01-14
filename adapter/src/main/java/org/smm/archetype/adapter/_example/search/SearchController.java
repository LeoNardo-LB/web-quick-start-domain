package org.smm.archetype.adapter._example.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.app._example.search.SearchAppService;
import org.smm.archetype.app._example.search.command.SearchCommand;
import org.smm.archetype.app._example.search.command.VectorSearchCommand;
import org.smm.archetype.app._example.search.command.AiSearchCommand;
import org.smm.archetype.app._example.search.command.HybridSearchCommand;
import org.smm.archetype.app._example.search.dto.PageResultDTO;
import org.smm.archetype.app._example.search.dto.VectorSearchResultDTO;
import org.smm.archetype.app._example.search.dto.AiSearchResultDTO;
import org.springframework.web.bind.annotation.*;

/**
 * 搜索控制器
 *
 * @author Leonardo
 * @since 2026-01-14
 */
@Slf4j
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchAppService searchAppService;

    /**
     * 通用搜索接口
     *
     * @param index 索引名称
     * @param command 搜索命令
     * @return 分页搜索结果
     */
    @PostMapping("/{index}")
    public PageResultDTO<?> search(
        @PathVariable String index,
        @RequestBody SearchCommand command
    ) {
        log.info("Search request: index={}, command={}", index, command);
        return searchAppService.search(index, command, Object.class);
    }

    /**
     * 索引文档
     *
     * @param index 索引名称
     * @param id 文档ID
     * @param document 文档内容
     */
    @PutMapping("/{index}/{id}")
    public void index(
        @PathVariable String index,
        @PathVariable String id,
        @RequestBody Object document
    ) {
        log.info("Index document: index={}, id={}", index, id);
        searchAppService.index(index, id, document);
    }

    /**
     * 获取文档
     *
     * @param index 索引名称
     * @param id 文档ID
     * @return 文档内容
     */
    @GetMapping("/{index}/{id}")
    public Object get(
        @PathVariable String index,
        @PathVariable String id
    ) {
        log.debug("Get document: index={}, id={}", index, id);
        return searchAppService.get(index, id, Object.class);
    }

    /**
     * 删除文档
     *
     * @param index 索引名称
     * @param id 文档ID
     */
    @DeleteMapping("/{index}/{id}")
    public void delete(
        @PathVariable String index,
        @PathVariable String id
    ) {
        log.info("Delete document: index={}, id={}", index, id);
        searchAppService.delete(index, id);
    }

    /**
     * 刷新索引
     *
     * @param index 索引名称
     */
    @PostMapping("/{index}/_refresh")
    public void refresh(@PathVariable String index) {
        log.info("Refresh index: index={}", index);
        searchAppService.refresh(index);
    }

    /**
     * 创建索引
     *
     * @param index 索引名称
     * @param mapping 索引映射
     */
    @PutMapping("/{index}/_create")
    public void createIndex(
        @PathVariable String index,
        @RequestBody String mapping
    ) {
        log.info("Create index: index={}", index);
        searchAppService.createIndex(index, mapping);
    }

    /**
     * 删除索引
     *
     * @param index 索引名称
     */
    @DeleteMapping("/{index}")
    public void deleteIndex(@PathVariable String index) {
        log.info("Delete index: index={}", index);
        searchAppService.deleteIndex(index);
    }

    // ========== 向量搜索 ==========

    /**
     * 向量搜索
     *
     * @param index 索引名称
     * @param command 向量搜索命令
     * @return 向量搜索结果
     */
    @PostMapping("/{index}/_vector")
    public VectorSearchResultDTO<?> vectorSearch(
        @PathVariable String index,
        @RequestBody VectorSearchCommand command
    ) {
        log.info("Vector search request: index={}, vectorField={}, k={}",
            index, command.getVectorField(), command.getK());
        return searchAppService.vectorSearch(index, command, Object.class);
    }

    /**
     * 创建向量索引
     *
     * @param index 索引名称
     * @param vectorField 向量字段名
     * @param dimension 向量维度
     * @param indexType 索引类型
     * @param distanceType 距离类型
     */
    @PutMapping("/{index}/_vector/{vectorField}")
    public void createVectorIndex(
        @PathVariable String index,
        @PathVariable String vectorField,
        @RequestParam int dimension,
        @RequestParam String indexType,
        @RequestParam String distanceType
    ) {
        log.info("Create vector index: index={}, field={}, dimension={}, type={}, distance={}",
            index, vectorField, dimension, indexType, distanceType);
        searchAppService.createVectorIndex(index, vectorField, dimension, indexType, distanceType);
    }

    // ========== AI搜索 ==========

    /**
     * AI增强搜索
     *
     * @param index 索引名称
     * @param command AI搜索命令
     * @return AI搜索结果
     */
    @PostMapping("/{index}/_ai")
    public AiSearchResultDTO<?> aiSearch(
        @PathVariable String index,
        @RequestBody AiSearchCommand command
    ) {
        log.info("AI search request: index={}, modelType={}, rerankStrategy={}",
            index, command.getModelType(), command.getRerankStrategy());
        return searchAppService.aiSearch(index, command, Object.class);
    }

    // ========== 混合搜索 ==========

    /**
     * 混合搜索（BM25 + 向量）
     *
     * @param index 索引名称
     * @param command 混合搜索命令
     * @return 分页搜索结果
     */
    @PostMapping("/{index}/_hybrid")
    public PageResultDTO<?> hybridSearch(
        @PathVariable String index,
        @RequestBody HybridSearchCommand command
    ) {
        log.info("Hybrid search request: index={}, bm25Weight={}",
            index, command.getBm25Weight());
        return searchAppService.hybridSearch(index, command, Object.class);
    }
}
