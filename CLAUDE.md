# CLAUDE.md - Java后端开发规范指南

**核心原则**: 安全第一、性能优先、可维护性至上

## 🎯 项目架构

### 架构模式

- **架构模式**: 领域驱动设计(DDD) + 清洁架构
- **数据流**: 命令查询职责分离(CQRS)模式

### 技术栈

- **核心框架**: Spring Boot
- **持久层**: MyBatis-Flex
- **消息队列**: Kafka
- **缓存**: Redis
- **配置管理**: 优先使用Spring Boot推荐的starter形式依赖

## 📝 代码规范

### 包结构

```
src/main/java/com/project/
├── application/          # 应用层
├── domain/               # 领域层
├── infrastructure/       # 基础设施层
├── interfaces/           # 接口层
└── common/               # 通用组件
```

### 通用编码规范

- **缩进**: 4个空格
- **行长度**: 最大120字符
- **命名规范**:
    - 类名: 大驼峰 (UserService)
    - 方法名: 小驼峰 (getUserById)
    - 变量名: 小驼峰 (userId)
    - 常量名: 大写+下划线 (MAX_SIZE)
- **注释要求**: 所有public方法必须有Javadoc
- **代码整洁性**: 使用lombok注解控制对象行为，规则：
    - 禁止使用@Data全包
    - 创建对象时：优先使用 @Builder 或 @SuperBuilder(setterPrefix = "set") 模式
    - 避免同时使用多个构造函数注解，防止冲突
    - 依赖注入使用@RequiredArgsConstructor（final字段自动注入）
    - 获取值：@Getter
    - 注意: Lombok注解使用可能会出现注解不兼容的情况，如果遇到此类问题，请根据具体情况进行分析及解决
- **日志输出**: 所有需要日志的类必须使用@Slf4j

## 🗄️ 数据库规范

### DDL文件位置

- 所有数据表结构定义统一放在项目根目录的`DDL-MySQL.sql`文件中

### 字段规范

```sql
-- 1. 所有主键ID统一为：BIGINT NOT NULL AUTO_INCREMENT
-- 2. 用户ID字段统一为：VARCHAR(64) DEFAULT NULL
-- 3. 移除所有显示宽度指定（INT(11)、BIGINT(20)、BIGINT(32)等）
-- 4. UUID/唯一标识：VARCHAR(64)
-- 5. 业务类型：VARCHAR(32)
-- 6. 服务名称/组名：VARCHAR(128)
-- 7. 状态字段：VARCHAR(32)
-- 8. 文件路径/URL：VARCHAR(512)
-- 9. MD5/哈希值：CHAR(32)
-- 10. 业务ID：VARCHAR(64)
-- 11. 枚举字段统一用VARCHAR(32)，不使用ENUM类型
-- 12. 索引命名规范：uk_{字段名}（唯一索引），idx_{字段名1}_{字段名2}（复合索引）
-- 13. 统一增加审计字段：
`create_time`   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
`update_time`   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
`delete_time`   TIMESTAMP   NULL     DEFAULT NULL COMMENT '删除时间',
`create_user`   VARCHAR(64)          DEFAULT NULL COMMENT '创建人ID',
`update_user`   VARCHAR(64)          DEFAULT NULL COMMENT '更新人ID',
`delete_user`   VARCHAR(64) NULL     DEFAULT NULL COMMENT '删除人ID',

```

## 🧪 测试要求

### 单元测试

- 每次生成代码后，必须生成对应的简单单测用例，执行并通过
- 测试要求:
    - 覆盖基本场景
    - 覆盖必要分支
    - 确保测试通过
- 测试文件位置: `src/test/java/`对应包路径

### 测试示例

```java
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    @Test
    void getUserById_Success() {
        // 准备测试数据
        String userId = "user123";

        // 执行测试
        User result = userService.getUserById(userId);

        // 验证结果
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
    }

}
```

## 🔧 代码设计原则

### 接口实现分离

- 通用类代码必须使用接口实现分离的方式
- 提高可维护性，预留替换实现的空间

```java
// 接口定义
public interface EmailService {

    void sendEmail(String to, String subject, String content);

}

// 实现类
public class SmtpEmailServiceImpl implements EmailService {

    @Override
    public void sendEmail(String to, String subject, String content) {
        // 实现细节
    }

}
```

### 依赖注入

- 使用构造函数注入
- 避免字段注入

```java

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    // 此处可以替换为Lombok的 @RequiredArgsConstructor
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

}
```

## ⚠️ 禁止事项

### 代码层面

- ❌ 禁止在Controller层直接调用Repository
- ❌ 禁止在Service层处理HTTP请求/响应
- ❌ 禁止硬编码配置值（如数据库连接、API地址）
- ❌ 禁止在业务代码中使用System.out.println()
- ❌ 禁止忽略异常处理（空catch块）

### 安全层面

- ❌ 禁止在日志中记录敏感信息（密码、身份证号、手机号等）
- ❌ 禁止使用明文存储密码
- ❌ 禁止在客户端存储敏感凭证
- ❌ 禁止使用不安全的随机数生成器（使用SecureRandom）

### 性能层面

- ❌ 禁止在循环中进行数据库查询
- ❌ 禁止在高并发场景使用synchronized过度
- ❌ 禁止不必要的对象创建（特别是在循环中）
- ❌ 禁止大事务操作（单个事务处理过多数据）

### 禁止修改目录

`org.smm.archetype.infrastructure._shared.generated`是一些框架自动生成代码的目录，禁止修改这个包下的所有文件！

## 🔄 代码生成要求

### 生成流程

1. 分析需求，确定代码位置
2. 按照规范生成代码
3. 生成对应的单元测试
4. 验证代码符合禁止事项要求

### 质量检查

- 生成的代码必须通过编译
- 单元测试必须通过
- 不违反任何禁止事项
- 符合接口实现分离原则
- 符合数据库字段规范