# DOMAIN/COMMON/SEARCH LAYER

**Generated**: 2026-02-01 15:42:00
**Commit**: latest
**Branch**: main

## OVERVIEW

Unified search abstraction layer supporting traditional BM25, vector similarity (HNSW/IVF/FLAT), AI-enhanced search, and hybrid strategies.
Business-layer wrapper around Elasticsearch technical implementation.

## STRUCTURE

```
domain/common/search/
├── query/          # Search query objects (input)
│   ├── SearchQuery.java           # Base query interface
│   ├── VectorSearchQuery.java      # Vector search input
│   ├── AiSearchQuery.java          # AI-enhanced search input
│   └── HybridSearchQuery.java       # BM25 + Vector hybrid input
├── result/         # Search result objects (output)
│   ├── SearchResult.java           # Base result interface
│   ├── VectorSearchResult.java     # Vector search output
│   ├── AggregationBucket.java       # Aggregation results
│   └── SearchHit.java             # Individual search hits
├── enums/          # Type definitions and configurations
│   ├── SearchStrategy.java          # BM25/VECTOR/HYBRID/AI_MODEL selector
│   ├── FilterOperator.java          # EQ/NE/GT/LTE/IN/EXISTS comparison
│   ├── AggregationType.java        # SUM/COUNT/AVG/MAX aggregation
│   ├── SortOrder.java              # ASC/DESC sorting
│   ├── VectorIndexType.java        # HNSW/IVF/FLAT vector index types
│   ├── VectorDistanceType.java      # COSINE/L2/DOT_PRODUCT distance metrics
│   └── AiSearchModelType.java      # AI model selector
└── SearchService.java              # Business service orchestrating search
```

## WHERE TO LOOK

| Task                 | Location               | Notes                                                   |
|----------------------|------------------------|---------------------------------------------------------|
| Search orchestration | SearchService.java     | Business layer wrapper, delegates to SearchClient       |
| Create queries       | query/ package         | Build query objects for different search types          |
| Handle results       | result/ package        | Process search responses into business objects          |
| Search configuration | enums/ package         | Select strategy, operator, aggregation types            |
| Vector search        | VectorSearchQuery.java | HNSW/IVF/FLAT algorithms                                |
| AI search            | AiSearchQuery.java     | Reranking strategies (NONE/SCORE_WEIGHTED/RRF/AI_MODEL) |

## CONVENTIONS

- **Layer Separation**: SearchService is business-layer, uses SearchClient (domain/bizshared) for technical ops
- **Strategy Pattern**: SearchStrategy enum selects implementation at runtime
- **Type Safety**: Generic Result<T> wrappers for all result types
- **Fluent Builders**: Lombok @Builder for complex query/result objects
- **Operator Mapping**: FilterOperator enum to Elasticsearch DSL (9 cases)
- **Aggregation Types**: SUM/COUNT/AVG/MAX correspond to Elasticsearch aggregations
- **Vector Indexing**: Support for HNSW (Hierarchical Navigable Small World), IVF (Inverted File Index), FLAT indices
- **Distance Metrics**: COSINE (default), L2 (Euclidean), DOT_PRODUCT for vector similarity

## ANTI-PATTERNS (THIS LAYER)

- ❌ Direct Elasticsearch operations (must use SearchClient)
- ❌ String-based operator/type (use enums: FilterOperator, AggregationType, etc.)
- ❌ Hardcoded search strategies (use SearchStrategy enum)
- ❌ Returning raw ES responses (must wrap in SearchResult<T>)

## UNIQUE STYLES

- **Multi-Modal Search**: Four search types unified in one service (BM25, vector, AI, hybrid)
- **Auto-Detection**: SearchClient detects availability of Elasticsearch and falls back to DisabledSearchClientImpl
- **AI Reranking**: Four reranking strategies configurable per query (NONE, SCORE_WEIGHTED, RRF, AI_MODEL)
- **Type-Safe Hits**: SearchHit<T> generic wrapper prevents raw Elasticsearch _source access
- **Pagination Support**: PageRequest/PageResult from domain/bizshared/base for consistent pagination

## COMPLEXITY HOTSPOTS

- **SearchService.java** (789 lines in infrastructure) - 4 search types, extensive DSL building, deep switch statements
    - Refactor recommendation: Extract search type strategies (BM25SearchStrategy, VectorSearchStrategy, etc.)
