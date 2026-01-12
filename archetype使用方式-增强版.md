# 骨架使用方法

## 本地安装骨架

##### 1、引入骨架构建插件

在项目的根pom中引入构架骨架插件

```xml

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-archetype-plugin</artifactId>
            <version>3.4.0</version>
        </plugin>
    </plugins>
</build>
```

##### 2、创建骨架文件

在项目根目录下执行如下命令

```shell
mvn archetype:create-from-project -s <你的maven的setting.xml文件位置>
```

执行完毕后，会发现项目中多了一个`target`文件夹，此文件夹记录骨架的相关配置信息

##### 3、配置 .md 文件变量替换 ⭐

进入上一步生成的target文件目录，具体路径：`<你的项目根目录>\target\generated-sources\archetype`

```shell
cd target\generated-sources\archetype
```

**关键步骤**：修改 `src/main/resources/META-INF/maven/archetype-metadata.xml` 文件，在 `<fileSets>` 部分添加以下配置：

```xml
<fileSets>
    <!-- 其他 fileSet 配置... -->

    <!-- Markdown 文档文件 - 启用变量替换 -->
    <fileSet filtered="true" packaged="false">
        <directory></directory>
        <includes>
            <include>**/*.md</include>
        </includes>
    </fileSet>

    <!-- 其他 fileSet 配置... -->
</fileSets>
```

**配置说明**：

- `filtered="true"`：启用变量替换（关键！）
- `packaged="false"`：不放入包路径（文档通常在根目录）
- `<directory></directory>`：应用到根目录
- `<include>**/*.md</include>`：包含所有 .md 文件

##### 4、本地安装骨架

配置完成后，执行`mvn install`命令安装骨架到本地maven仓库。安装完成后，会输出本地骨架的路径，并在本地仓库中创建一个骨架坐标文件。坐标文件记录着本地骨架对于本地仓库的相对路径。

```shell
mvn install
```

**安装成功输出的内容**

```shell
[INFO] --- install:3.1.1:install (default-install) @ web-quick-start-domain ---
[INFO] Installing D:\Dev\code\archetype\web-quick-start-domain\target\generated-sources\archetype\pom.xml to D:\Dev\software\apache-maven-3.9.6\local_reposity\org\smm\archetype\web-quick-start-domain\1.0.0\web-quick-start-domain-1.0.0.pom
[INFO] Installing D:\Dev\code\archetype\web-quick-start-domain\target\generated-sources\archetype\target\web-quick-start-domain-1.0.0.jar to D:\Dev\software\apache-maven-3.9.6\local_reposity\org\smm\archetype\web-quick-start-domain\1.0.0\web-quick-start-domain-1.0.0.jar
```

**坐标文件：本地仓库/archetype-catalog.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<archetype-catalog
        xsi:schemaLocation="https://maven.apache.org/plugins/maven-archetype-plugin/archetype-catalog/1.0.0 https://maven.apache.org/xsd/archetype-catalog-1.0.0.xsd"
        xmlns="https://maven.apache.org/plugins/maven-archetype-plugin/archetype-catalog/1.0.0"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <archetypes>
        <archetype>
            <groupId>org.smm.archetype</groupId>
            <artifactId>web-quick-start-domain</artifactId>
            <version>1.0.0</version>
            <description>DDD Web quick start archetype - Domain Driven Design project template</description>
        </archetype>
    </archetypes>
</archetype-catalog>
```

---

## 在 .md 文档中使用变量 ⭐

配置好 `filtered="true"` 后，你可以在 .md 文档中使用以下 Velocity 变量：

### 常用变量列表

| 变量名                      | 说明            | 示例值                  |
|--------------------------|---------------|----------------------|
| `${groupId}`             | 项目 groupId    | `org.example`        |
| `${artifactId}`          | 项目 artifactId | `my-project`         |
| `${version}`             | 项目版本          | `1.0.0-SNAPSHOT`     |
| `${package}`             | 包路径（点分隔）      | `org.example.domain` |
| `${packageInPathFormat}` | 包路径（斜杠分隔）     | `org/example/domain` |

### 在 .md 中使用变量示例

#### 示例1：包路径替换

```markdown
## 领域层

领域层的包路径为：`${package}`

对应的物理路径为：`src/main/java/${packageInPathFormat}`
```

**生成后**（假设 groupId=org.example, artifactId=my-app）：

```markdown
## 领域层

领域层的包路径为：`org.example`

对应的物理路径为：`src/main/java/org/example`
```

#### 示例2：聚合根示例代码

```markdown
### 创建聚合根

在 `${packageInPathFormat}/domain/model` 目录下创建聚合根：

\`\`\`java
package ${package}.domain.model;

import org.smm.archetype.domain.aggregate.AggregateRoot;

public class ${artifactId}Aggr extends AggregateRoot {
    // 聚合根实现
}
\`\`\`
```

#### 示例3：文件路径引用

```markdown
### 配置文件

- 主配置：`src/main/resources/application.yml`
- 领域层：`src/main/java/${packageInPathFormat}/domain/`
- 应用层：`src/main/java/${packageInPathFormat}/app/`
```

#### 示例4：完整文档示例

```markdown
# ${artifactId} 项目文档

## 项目信息

- **GroupId**: ${groupId}
- **ArtifactId**: ${artifactId}
- **Version**: ${version}
- **Package**: ${package}

## 目录结构

\`\`\`
${artifactId}/
├── src/main/java/${packageInPathFormat}/
│   ├── domain/
│   ├── app/
│   ├── adapter/
│   └── infrastructure/
└── src/main/resources/
\`\`\`

## 快速开始

### 编译项目

\`\`\`bash
mvn clean compile
\`\`\`

### 运行测试

\`\`\`bash
mvn test
\`\`\`

## 开发指南

### 领域层开发

在 `${packageInPathFormat}/domain` 包下创建领域模型：

\`\`\`java
package ${package}.domain.model;

public class ExampleEntity {
    // 实体实现
}
\`\`\`
```

---

## 使用骨架

有两种创建骨架的方式

1. 在IDEA中通过界面创建骨架

2. 通过命令行指定骨架并创建项目

### 通过IDEA界面创建骨架

在IDEA中指定骨架文件`archetype-catalog.xml`的位置（一般在本地maven仓库的根目录下），然后在新建项目时指定使用这个骨架文件，此时就会出现骨架配置文件中所有可用的骨架。

### 通过命令行创建骨架（推荐）

```shell
mvn archetype:generate -DarchetypeCatalog=local ^
  -DinteractiveMode=false ^
  -DarchetypeGroupId=org.smm.archetype ^
  -DarchetypeArtifactId=web-quick-start-domain ^
  -DarchetypeVersion=1.0.0 ^
  -DgroupId=org.example ^
  -DartifactId=my-demo-project ^
  -Dversion=1.0.0-SNAPSHOT
```

> 注意`^`后不能有任意其他字符，否则会被windows终端识别成下一行导致失败

---

## 骨架配置信息

```shell
archetype.groupId=org.smm.archetype
archetype.artifactId=web-quick-start-domain
archetype.version=1.0.0
# 排除的文件
excludePatterns=**/.idea/**,**/target/*,logs/**,modules/**,**/*.iml,**/logs,**/logs/*,**/logs/**,README.md,data_h2/**,_output/**,_notes/**
```

---

## 常见问题 FAQ

### Q1: .md 文件中的变量没有被替换怎么办？

**A**: 检查 `archetype-metadata.xml` 中的配置：

1. 确认 `<fileSet filtered="true">` 已设置
2. 确认 `<include>**/*.md</include>` 已添加
3. 重新运行 `mvn install` 安装骨架

### Q2: 生成项目时提示找不到 archetype？

**A**: 检查以下几点：

1. 确认已运行 `mvn install` 安装骨架到本地仓库
2. 确认 `-DarchetypeCatalog=local` 参数已设置
3. 确认 groupId、artifactId、version 与骨架配置一致

### Q3: 某些文件不想被变量替换怎么办？

**A**: 在 `archetype-metadata.xml` 中为这些文件单独配置 `filtered="false"`：

```xml
<fileSet filtered="false" packaged="false">
    <directory></directory>
    <includes>
        <include>**/LICENSE</include>
        <include>**/NOTICE</include>
    </includes>
</fileSet>
```

### Q4: ${package} 和 ${packageInPathFormat} 有什么区别？

**A**:

- `${package}`：点分隔的包名，如 `org.example.domain`
- `${packageInPathFormat}`：斜杠分隔的包路径，如 `org/example/domain`

根据场景选择使用：

- 在 Java 代码的 package 声明中使用 `${package}`
- 在文件路径、目录结构描述中使用 `${packageInPathFormat}`

### Q5: 如何调试 archetype 生成过程？

**A**: 使用 `-X` 参数启用调试模式：

```shell
mvn archetype:generate -X -DarchetypeCatalog=local ...
```

这会输出详细的日志，包括：

- 变量替换过程
- 文件过滤情况
- 模板处理详情

---

## 高级技巧

### 1. 条件内容生成

在 .md 文档中使用 Velocity 条件语句：

```markdown
# 项目配置

#if( ${databaseType} == "mysql" )
## MySQL 配置

数据库驱动：`mysql-connector-j`
#elseif( ${databaseType} == "postgresql" )
## PostgreSQL 配置

数据库驱动：`postgresql`
#end
```

### 2. 循环生成列表

```markdown
## 模块列表

#foreach( $module in $modules )
- ${module}
#end
```

### 3. 自定义变量

在生成项目时传递自定义变量：

```shell
mvn archetype:generate \
  -DarchetypeCatalog=local \
  -DarchetypeGroupId=org.smm.archetype \
  -DarchetypeArtifactId=web-quick-start-domain \
  -DarchetypeVersion=1.0.0 \
  -DgroupId=org.example \
  -DartifactId=my-project \
  -DcustomVar=customValue
```

在 .md 文档中使用：

```markdown
自定义变量值：${customVar}
```

---

## 验证变量替换效果

生成项目后，检查以下内容：

1. ✅ README.md 中的 groupId、artifactId 已替换
2. ✅ 包路径 `${package}` 已替换为实际包名
3. ✅ 文件路径 `${packageInPathFormat}` 已替换为实际路径
4. ✅ 代码示例中的包名已正确替换

---

## 有可能的错误

##### The specified user settings file does not exist: C:\Users\Administrator\.m2\settings.xml

如果显示找不到settings位置，可以使用`-s`手动指定settings文件的位置

```shell
mvn archetype:create-from-project -s "D:\path\to\settings.xml"
```

##### 找不到生成的 archetype

检查以下位置：

1. 本地 Maven 仓库根目录的 `archetype-catalog.xml` 文件
2. 确认骨架已正确安装到本地仓库

查看已安装的骨架：

```shell
cat ~/.m2/repository/archetype-catalog.xml
```
