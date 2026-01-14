package org.smm.archetype.infrastructure._shared.client.es.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.smm.archetype.infrastructure._shared.client.es.AbstractEsClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 内存ES实现（基于Lucene）
 *
 * <p>提供纯内存的ES兼容实现，用于开发和测试
 *
 * @author Leonardo
 * @since 2026-01-14
 */
@Slf4j
public class MemoryEsClientImpl extends AbstractEsClient {

    private final Map<String, IndexWriter> indexWriters = new ConcurrentHashMap<>();
    private final Map<String, IndexSearcher> indexSearchers = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MemoryEsClientImpl(String dataPath) {
        // dataPath参数暂时未使用，未来可支持持久化
        log.info("Initializing Memory ES Client");
    }

    @Override
    protected void doIndex(String index, String id, Map<String, Object> document) {
        try {
            IndexWriter writer = getOrCreateIndexWriter(index);
            Document doc = new Document();

            // 存储ID
            doc.add(new TextField("_id", id, Field.Store.YES));

            // 存储文档字段
            for (Map.Entry<String, Object> entry : document.entrySet()) {
                String fieldName = entry.getKey();
                Object value = entry.getValue();

                if (value instanceof String) {
                    doc.add(new TextField(fieldName, (String) value, Field.Store.YES));
                } else if (value instanceof Number) {
                    String strValue = value.toString();
                    doc.add(new TextField(fieldName, strValue, Field.Store.YES));
                } else if (value instanceof Boolean) {
                    String strValue = value.toString();
                    doc.add(new TextField(fieldName, strValue, Field.Store.YES));
                } else {
                    String jsonValue = objectMapper.writeValueAsString(value);
                    doc.add(new TextField(fieldName, jsonValue, Field.Store.YES));
                }
            }

            writer.updateDocument(new org.apache.lucene.index.Term("_id", id), doc);
            writer.commit();

            // 清除旧的搜索器缓存
            indexSearchers.remove(index);
        } catch (Exception e) {
            throw new RuntimeException("Failed to index document", e);
        }
    }

    @Override
    protected void doBulkIndex(String index, List<Map.Entry<String, Map<String, Object>>> documents) {
        for (Map.Entry<String, Map<String, Object>> entry : documents) {
            index(index, entry.getKey(), entry.getValue());
        }
    }

    @Override
    protected void doDelete(String index, String id) {
        try {
            IndexWriter writer = getOrCreateIndexWriter(index);
            writer.deleteDocuments(new org.apache.lucene.index.Term("_id", id));
            writer.commit();
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete document", e);
        }
    }

    @Override
    protected void doBulkDelete(String index, List<String> ids) {
        for (String id : ids) {
            delete(index, id);
        }
    }

    @Override
    protected Map<String, Object> doGet(String index, String id) {
        try {
            IndexSearcher searcher = getOrCreateIndexSearcher(index);
            Query query = new QueryParser("_id", new StandardAnalyzer()).parse(id);
            TopDocs topDocs = searcher.search(query, 1);

            if (topDocs.totalHits.value == 0) {
                return null;
            }

            Document doc = searcher.doc(topDocs.scoreDocs[0].doc);
            return convertLuceneDocToMap(doc);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get document", e);
        }
    }

    @Override
    protected Map<String, Map<String, Object>> doBulkGet(String index, List<String> ids) {
        Map<String, Map<String, Object>> results = new HashMap<>();
        for (String id : ids) {
            Map<String, Object> doc = get(index, id);
            if (doc != null) {
                results.put(id, doc);
            }
        }
        return results;
    }

    @Override
    protected Map<String, Object> doSearch(String index, String query, int from, int size) {
        try {
            IndexSearcher searcher = getOrCreateIndexSearcher(index);
            JsonNode queryJson = objectMapper.readTree(query);

            // 检测查询类型
            if (queryJson.has("function_score")) {
                // SCORE_WEIGHTED策略
                return executeFunctionScoreQuery(searcher, queryJson, from, size);
            } else if (queryJson.has("query") && queryJson.get("query").has("bool")) {
                var boolObj = queryJson.get("query").get("bool");
                if (boolObj.has("should")) {
                    // 混合搜索（BM25 + kNN）
                    return executeHybridSearch(searcher, boolObj, from, size, query);
                }
            }

            // 普通查询（match、match_all）
            Query luceneQuery = parseQuery(queryJson, searcher);
            TopDocs topDocs = searcher.search(luceneQuery, from + size);
            return convertTopDocsToResponse(searcher, topDocs, from, size);

        } catch (Exception e) {
            throw new RuntimeException("Failed to search", e);
        }
    }

    @Override
    protected Map<String, Object> doAggregate(String index, String query) {
        // 简化实现：聚合使用search返回空结果
        Map<String, Object> result = new HashMap<>();
        result.put("aggregations", Map.of());
        return result;
    }

    @Override
    protected boolean doExistsIndex(String index) {
        return indexWriters.containsKey(index);
    }

    @Override
    protected void doCreateIndex(String index, String mapping) {
        getOrCreateIndexWriter(index);
    }

    @Override
    protected void doDeleteIndex(String index) {
        try {
            IndexWriter writer = indexWriters.remove(index);
            if (writer != null) {
                writer.close();
            }
            IndexSearcher searcher = indexSearchers.remove(index);
            if (searcher != null) {
                searcher.getIndexReader().close();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete index", e);
        }
    }

    @Override
    protected void doRefresh(String index) {
        // 内存实现不需要refresh
    }

    @Override
    protected Map<String, Object> doVectorSearch(String index, String query) {
        try {
            IndexSearcher searcher = getOrCreateIndexSearcher(index);
            JsonNode queryJson = objectMapper.readTree(query);

            // 解析kNN查询
            if (queryJson.has("knn")) {
                JsonNode knnNode = queryJson.get("knn");
                String vectorField = knnNode.has("field") ? knnNode.get("field").asText() : "vector";
                int k = knnNode.has("k") ? knnNode.get("k").asInt() : 10;

                // 获取查询向量
                List<Float> queryVector = null;
                if (knnNode.has("query_vector")) {
                    JsonNode vectorNode = knnNode.get("query_vector");
                    queryVector = objectMapper.convertValue(vectorNode, List.class);
                }

                if (queryVector == null || queryVector.isEmpty()) {
                    throw new RuntimeException("Query vector is required for kNN search");
                }

                // 执行向量相似度搜索
                return executeVectorSearch(searcher, vectorField, queryVector, k);
            }

            // 默认返回空结果
            Map<String, Object> result = new HashMap<>();
            result.put("hits", Map.of("total", Map.of("value", 0), "hits", List.of()));
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute vector search", e);
        }
    }

    @Override
    protected void doCreateVectorIndex(String index, String vectorField, int dimension, String indexType, String distanceType) {
        // 内存实现：不需要特殊配置，直接创建索引
        getOrCreateIndexWriter(index);
        log.info("Vector index created in memory: index={}, field={}, dimension={}", index, vectorField, dimension);
    }

    // ========== 私有方法 ==========

    private IndexWriter getOrCreateIndexWriter(String index) {
        return indexWriters.computeIfAbsent(index, idx -> {
            try {
                var directory = new ByteBuffersDirectory();
                Analyzer analyzer = new StandardAnalyzer();
                IndexWriterConfig config = new IndexWriterConfig(analyzer);
                config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
                return new IndexWriter(directory, config);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create index writer", e);
            }
        });
    }

    private IndexSearcher getOrCreateIndexSearcher(String index) throws Exception {
        return indexSearchers.computeIfAbsent(index, idx -> {
            try {
                IndexWriter writer = getOrCreateIndexWriter(idx);
                writer.commit();
                DirectoryReader reader = DirectoryReader.open(writer);
                return new IndexSearcher(reader);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create index reader", e);
            }
        });
    }

    private Query parseQuery(JsonNode queryJson, IndexSearcher searcher) throws Exception {
        // 简化实现：只支持match查询
        if (queryJson.has("match")) {
            JsonNode matchNode = queryJson.get("match");
            // 获取第一个字段名
            var fieldsIterator = matchNode.fields();
            if (fieldsIterator.hasNext()) {
                var entry = fieldsIterator.next();
                String fieldName = entry.getKey();
                String value = entry.getValue().asText();

                Analyzer analyzer = new StandardAnalyzer();
                QueryParser parser = new QueryParser(fieldName, analyzer);
                return parser.parse(value);
            }
            return new MatchAllDocsQuery();
        } else if (queryJson.has("match_all")) {
            return new MatchAllDocsQuery();
        } else {
            // 默认返回所有文档
            return new MatchAllDocsQuery();
        }
    }

    private Map<String, Object> convertLuceneDocToMap(Document doc) {
        Map<String, Object> map = new HashMap<>();
        for (IndexableField field : doc.getFields()) {
            String name = field.name();
            if (name.equals("_id")) {
                map.put(name, doc.get(name));
            } else {
                // 简化处理：所有字段转为字符串
                String stringValue = doc.get(name);
                if (stringValue != null) {
                    map.put(name, stringValue);
                }
            }
        }
        return map;
    }

    /**
     * 执行向量相似度搜索（简化实现）
     */
    private Map<String, Object> executeVectorSearch(IndexSearcher searcher, String vectorField,
                                                     List<Float> queryVector, int k) throws Exception {
        // 获取所有文档
        Query allDocsQuery = new MatchAllDocsQuery();
        TopDocs allDocs = searcher.search(allDocsQuery, Integer.MAX_VALUE);

        List<Map<String, Object>> results = new ArrayList<>();

        // 遍历所有文档，计算余弦相似度
        for (ScoreDoc scoreDoc : allDocs.scoreDocs) {
            Document doc = searcher.doc(scoreDoc.doc);

            // 尝试解析向量字段
            String vectorStr = doc.get(vectorField);
            if (vectorStr != null) {
                try {
                    // 向量字段存储为JSON数组字符串
                    List<Float> docVector = objectMapper.readValue(vectorStr, List.class);

                    // 计算余弦相似度
                    double similarity = cosineSimilarity(queryVector, docVector);

                    Map<String, Object> hit = new HashMap<>();
                    hit.put("_id", doc.get("_id"));
                    hit.put("_score", similarity);
                    hit.put("_source", convertLuceneDocToMap(doc));
                    results.add(hit);
                } catch (Exception e) {
                    // 向量字段解析失败，跳过该文档
                    log.debug("Failed to parse vector field: {}", e.getMessage());
                }
            }
        }

        // 按相似度排序（降序）
        results.sort((a, b) -> {
            Double scoreA = (Double) a.get("_score");
            Double scoreB = (Double) b.get("_score");
            return scoreB.compareTo(scoreA);
        });

        // 返回top-k结果
        List<Map<String, Object>> topKResults = results.subList(0, Math.min(k, results.size()));

        Map<String, Object> result = new HashMap<>();
        result.put("hits", Map.of(
            "total", Map.of("value", results.size()),
            "hits", topKResults
        ));
        result.put("took", 1L);

        return result;
    }

    /**
     * 计算余弦相似度
     */
    private double cosineSimilarity(List<Float> vectorA, List<Float> vectorB) {
        if (vectorA.size() != vectorB.size()) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.size(); i++) {
            dotProduct += vectorA.get(i) * vectorB.get(i);
            normA += vectorA.get(i) * vectorA.get(i);
            normB += vectorB.get(i) * vectorB.get(i);
        }

        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /**
     * 转换TopDocs为ES响应格式
     */
    private Map<String, Object> convertTopDocsToResponse(IndexSearcher searcher, TopDocs topDocs, int from, int size) throws Exception {
        List<Map<String, Object>> hits = new ArrayList<>();
        int start = Math.min(from, topDocs.scoreDocs.length);
        int end = Math.min(from + size, topDocs.scoreDocs.length);

        for (int i = start; i < end; i++) {
            ScoreDoc scoreDoc = topDocs.scoreDocs[i];
            Document doc = searcher.doc(scoreDoc.doc);
            Map<String, Object> hit = convertLuceneDocToMap(doc);
            hit.put("_score", (double) scoreDoc.score);
            hit.put("_id", doc.get("_id"));
            hits.add(hit);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("hits", Map.of(
            "total", Map.of("value", topDocs.totalHits.value),
            "hits", hits
        ));
        result.put("took", 1L);

        return result;
    }

    /**
     * 执行function_score查询（SCORE_WEIGHTED策略）
     */
    private Map<String, Object> executeFunctionScoreQuery(
            IndexSearcher searcher,
            JsonNode functionScoreJson,
            int from,
            int size) throws Exception {

        // 1. 解析基础查询
        var queryNode = functionScoreJson.get("query");
        Query baseQuery = parseQuery(queryNode, searcher);

        // 2. 执行基础查询获取候选文档
        TopDocs baseDocs = searcher.search(baseQuery, from + size);

        // 3. 应用functions（加权）
        var functionsArray = functionScoreJson.get("functions");
        Map<String, Float> finalScores = new HashMap<>();

        for (ScoreDoc scoreDoc : baseDocs.scoreDocs) {
            String docId = searcher.doc(scoreDoc.doc).get("_id");
            float finalScore = scoreDoc.score;

            // 遍历functions计算分数
            for (JsonNode function : functionsArray) {
                if (function.has("weight")) {
                    float weight = function.get("weight").floatValue();
                    finalScore = finalScore * weight;
                }
            }

            finalScores.put(docId, finalScore);
        }

        // 4. 按最终分数排序
        List<Map.Entry<String, Float>> sortedEntries = finalScores.entrySet().stream()
            .sorted(Map.Entry.<String, Float>comparingByValue().reversed())
            .collect(Collectors.toList());

        // 5. 构建响应
        return buildResponseFromSortedScores(searcher, sortedEntries, from, size);
    }

    /**
     * 执行混合搜索（BM25 + kNN）
     */
    private Map<String, Object> executeHybridSearch(
            IndexSearcher searcher,
            JsonNode boolNode,
            int from,
            int size,
            String originalQuery) throws Exception {

        var shouldArray = boolNode.get("should");

        Query bm25Query = null;
        List<Float> queryVector = null;
        int k = 10;
        float bm25Weight = 0.5f; // 默认值

        // 解析should子句
        for (JsonNode shouldNode : shouldArray) {
            if (shouldNode.has("match")) {
                // BM25查询
                bm25Query = parseQuery(shouldNode, searcher);
            } else if (shouldNode.has("knn")) {
                // kNN查询
                var knnNode = shouldNode.get("knn");
                k = knnNode.has("k") ? knnNode.get("k").asInt() : 10;
                var vectorNode = knnNode.get("query_vector");
                queryVector = objectMapper.convertValue(vectorNode, List.class);
            }
        }

        if (bm25Query == null || queryVector == null) {
            throw new RuntimeException("Hybrid search requires both BM25 and kNN queries");
        }

        // 1. 执行BM25搜索
        TopDocs bm25Docs = searcher.search(bm25Query, k * 2);
        Map<String, Float> bm25Scores = new HashMap<>();
        for (ScoreDoc scoreDoc : bm25Docs.scoreDocs) {
            String docId = searcher.doc(scoreDoc.doc).get("_id");
            bm25Scores.put(docId, scoreDoc.score);
        }

        // 2. 执行向量搜索
        TopDocs vectorDocs = executeVectorRanking(searcher, queryVector, k * 2);
        Map<String, Float> vectorScores = new HashMap<>();
        for (ScoreDoc scoreDoc : vectorDocs.scoreDocs) {
            String docId = searcher.doc(scoreDoc.doc).get("_id");
            vectorScores.put(docId, scoreDoc.score);
        }

        // 3. 融合分数（加权）
        float vectorWeight = 1.0f - bm25Weight;
        Map<String, Float> combinedScores = new HashMap<>();

        // 合并所有文档ID
        Set<String> allDocIds = new HashSet<>();
        allDocIds.addAll(bm25Scores.keySet());
        allDocIds.addAll(vectorScores.keySet());

        for (String docId : allDocIds) {
            float bm25Score = bm25Scores.getOrDefault(docId, 0.0f);
            float vectorScore = vectorScores.getOrDefault(docId, 0.0f);
            float combinedScore = bm25Score * bm25Weight + vectorScore * vectorWeight;
            combinedScores.put(docId, combinedScore);
        }

        // 4. 按融合分数排序
        List<Map.Entry<String, Float>> sortedEntries = combinedScores.entrySet().stream()
            .sorted(Map.Entry.<String, Float>comparingByValue().reversed())
            .collect(Collectors.toList());

        // 5. 取top-K结果
        List<Map.Entry<String, Float>> topK = sortedEntries.subList(
            0, Math.min(size, sortedEntries.size())
        );

        // 6. 构建响应
        return buildResponseFromSortedScores(searcher, topK, from, size);
    }

    /**
     * 执行向量排名
     */
    private TopDocs executeVectorRanking(IndexSearcher searcher, List<Float> queryVector, int topN)
            throws Exception {
        // 获取所有文档
        Query allDocsQuery = new MatchAllDocsQuery();
        TopDocs allDocs = searcher.search(allDocsQuery, Integer.MAX_VALUE);

        // 计算每个文档的向量分数
        List<ScoreDoc> scoredDocs = new ArrayList<>();
        for (ScoreDoc scoreDoc : allDocs.scoreDocs) {
            Document doc = searcher.doc(scoreDoc.doc);
            String vectorStr = doc.get("vector");
            if (vectorStr != null) {
                try {
                    @SuppressWarnings("unchecked")
                    List<Float> docVector = objectMapper.readValue(vectorStr, List.class);
                    double similarity = cosineSimilarity(queryVector, docVector);
                    scoredDocs.add(new ScoreDoc(scoreDoc.doc, (float) similarity));
                } catch (Exception e) {
                    log.debug("Failed to parse vector: {}", e.getMessage());
                }
            }
        }

        // 按相似度排序
        scoredDocs.sort((a, b) -> Float.compare(b.score, a.score));

        // 返回top-N
        List<ScoreDoc> topDocs = scoredDocs.subList(0, Math.min(topN, scoredDocs.size()));
        return new TopDocs(allDocs.totalHits, topDocs.toArray(new ScoreDoc[0]));
    }

    /**
     * 从排序的分数构建响应
     */
    private Map<String, Object> buildResponseFromSortedScores(
            IndexSearcher searcher,
            List<Map.Entry<String, Float>> sortedEntries,
            int from,
            int size) throws Exception {

        int start = Math.min(from, sortedEntries.size());
        int end = Math.min(from + size, sortedEntries.size());

        List<Map<String, Object>> hits = new ArrayList<>();
        for (int i = start; i < end; i++) {
            var entry = sortedEntries.get(i);
            String docId = entry.getKey();
            float score = entry.getValue();

            // 获取文档内容
            Query idQuery = new QueryParser("_id", new StandardAnalyzer()).parse(docId);
            TopDocs docHits = searcher.search(idQuery, 1);
            if (docHits.totalHits.value > 0) {
                Document doc = searcher.doc(docHits.scoreDocs[0].doc);
                Map<String, Object> hit = convertLuceneDocToMap(doc);
                hit.put("_id", docId);
                hit.put("_score", score);
                hits.add(hit);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("hits", Map.of(
            "total", Map.of("value", sortedEntries.size()),
            "hits", hits
        ));
        result.put("took", 1L);

        return result;
    }

    @Override
    protected List<Map<String, Object>> doBulkVectorSearch(String index, List<String> queries) {
        // 简化实现：逐个执行向量搜索
        List<Map<String, Object>> results = new ArrayList<>();
        for (String query : queries) {
            Map<String, Object> result = vectorSearch(index, query);
            results.add(result);
        }
        return results;
    }
}
