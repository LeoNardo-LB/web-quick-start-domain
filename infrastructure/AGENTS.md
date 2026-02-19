# Infrastructure 层 - 技术实现层

**技术实现层**：为 Domain 层提供技术实现，包含持久化、事件发布、缓存、文件服务。

## 目录结构

```
infrastructure/src/main/java/org/smm/archetype/infrastructure/
├── shared/             # 共享基础设施
│   ├── event/          # 事件发布（EventPublisher、EventRepository）
│   ├── dal/            # 数据访问层（MyBatis Plus 生成代码）
│   └── util/           # 工具类（SpringContextUtils、KryoSerializer）
├── platform/           # 平台基础设施
│   ├── file/           # 文件存储实现
│   └── search/         # 搜索实现（Elasticsearch）
└── {模块}/             # 业务模块
    └── persistence/    # Repository 实现 + Converter
```

## 关键查找

| 目标              | 位置                                                   |
|-----------------|------------------------------------------------------|
| Repository 实现   | `infrastructure/**/persistence/*RepositoryImpl.java` |
| Converter       | `infrastructure/**/*Converter.java`                  |
| Event Publisher | `infrastructure/shared/event/`                       |
| 缓存实现            | `infrastructure/platform/cache/`                     |

## 核心规则

### Repository 实现（NON-NEGOTIABLE）

| 规则   | 说明                                     |
|------|----------------------------------------|
| 转换工具 | 必须使用 MapStruct 进行 Domain ↔ DO 转换       |
| 枚举转换 | Domain ↔ DO 的枚举转换**必须**在 Converter 中完成 |
| 代码生成 | Mapper 和 DO 类使用 MyBatis Plus（Lambda查询） |

### 事件驱动架构（NON-NEGOTIABLE）

| 规则        | 说明                                            |
|-----------|-----------------------------------------------|
| 事件发布      | 通过 `EventPublisher.publishEvent(DomainEvent)` |
| 重试策略      | 优先使用内置指数退避，必要时支持 XXL-JOB/PowerJob             |
| 幂等性       | 事件必须幂等，重复处理不得产生副作用                            |

**发布链路**：
```
Domain.recordEvent() 
    → DomainEventCollectPublisher (收集 + 持久化)
        → SpringDomainEventPublisher (发布)
```

### 中间件接入规范

| 场景        | 方式                     |
|-----------|------------------------|
| 简单接入（轻量级） | 直接实现 `*Client` 接口      |
| 复杂接入（多实现） | `Abstract*Client` 抽象基类 |

**条件化配置**：使用 `@ConditionalOnProperty` 按需加载

## 代码模板

### Converter（枚举转换）

```java

@Mapper(componentModel = "spring")
public interface FileBusinessConverter {

    // DO → Domain：枚举从 String 转换
    @Mapping(target = "type", expression = "java(dataObject.getType() != null ? "
            + "FileBusiness.Type.valueOf(dataObject.getType()) : null)")
    @Mapping(target = "usage", expression = "java(dataObject.getUsage() != null ? "
            + "FileBusiness.Usage.valueOf(dataObject.getUsage()) : null)")
    @Mapping(target = "fileMetadata", ignore = true)
    // 需要单独查询
    FileBusiness toEntity(FileBusinessDO dataObject);

    // Domain → DO：枚举转 String
    @Mapping(target = "type", expression = "java(entity.getType() != null ? "
            + "entity.getType().name() : null)")
    @Mapping(target = "usage", expression = "java(entity.getUsage() != null ? "
            + "entity.getUsage().name() : null)")
    FileBusinessDO toDataObject(FileBusiness entity);

}
```

### Converter（复杂转换：default 方法）

```java

@Mapper(componentModel = "spring")
public interface FileMetaConverter {

    @Mapping(target = "status", expression = "java(toStatus(dataObject))")
    FileMetadata toEntity(FileMetadataDO dataObject);

    @Mapping(target = "deleteTime", expression = "java(toDeleteTime(entity))")
    FileMetadataDO toDataObject(FileMetadata entity);

    // DO.deleteTime → Domain.Status
    default Status toStatus(FileMetadataDO dataObject) {
        if (dataObject == null || dataObject.getDeleteTime() == null || dataObject.getDeleteTime() == 0) {
            return Status.ACTIVE;
        }
        return Status.DELETED;
    }

    // Domain.Status → DO.deleteTime
    default Long toDeleteTime(FileMetadata entity) {
        return entity.getStatus() == Status.ACTIVE ? 0L : System.currentTimeMillis();
    }

}
```

### Repository 实现

```java

@RequiredArgsConstructor
public class FileRepositoryImpl implements FileRepository {

    private final FileBusinessMapper    businessMapper;
    private final FileMetadataMapper    metadataMapper;
    private final FileBusinessConverter converter;

    @Override
    public FileBusiness save(FileBusiness fileBusiness) {
        // 1. 保存元数据
        FileMetadataDO metadataDO = metaConverter.toDataObject(fileBusiness.getFileMetadata());
        metadataMapper.upsertByMd5(metadataDO);

        // 2. 保存业务关联
        FileBusinessDO businessDO = converter.toDataObject(fileBusiness);
        businessDO.setFileMetaId(String.valueOf(metadataDO.getId()));
        businessMapper.upsertById(businessDO);

        return fileBusiness;
    }

    @Override
    public Optional<FileBusiness> findById(String id) {
        FileBusinessDO businessDO = businessMapper.selectById(id);
        if (businessDO == null)
            return Optional.empty();

        // 查询关联的元数据
        FileMetadataDO metadataDO = metadataMapper.selectOne(
                Wrappers.<FileMetadataDO>lambdaQuery()
                        .eq(FileMetadataDO::getId, Long.parseLong(businessDO.getFileMetaId()))
        );
        if (metadataDO == null)
            return Optional.empty();

        // 组装领域对象
        FileBusiness fileBusiness = converter.toEntity(businessDO);
        fileBusiness.setFileMetadata(metaConverter.toEntity(metadataDO));
        return Optional.of(fileBusiness);
    }

    @Override
    public List<FileBusiness> findByBusinessIdAndTypeAndUsage(String businessId, Type type, Usage usage) {
        return businessMapper.selectList(
                Wrappers.<FileBusinessDO>lambdaQuery()
                        .eq(FileBusinessDO::getBusinessId, businessId)
                        .eq(FileBusinessDO::getType, type.name())
                        .eq(FileBusinessDO::getUsage, usage.name())
                        .orderByAsc(FileBusinessDO::getSort)
        ).stream().map(this::toFileBusinessWithMetadata).toList();
    }

    @Override
    public void deleteById(String id) {
        businessMapper.deleteById(id);
    }

}
```

## 禁止

| ❌ 禁止                           | ✅ 正确                         |
|--------------------------------|------------------------------|
| Domain 层处理 DO ↔ Domain 转换      | 转换在 Converter 中完成            |
| Repository 直接操作数据库实体           | 使用 Mapper 操作，通过 Converter 转换 |
| 为抽象而抽象（简单场景创建 Abstract*Client） | 直接实现接口                       |
| 创建配置类                          | 配置类在 start 模块                |

---
← [项目知识库](../AGENTS.md) | **版本**: 3.4 | **更新**: 2026-02-19
