# Archetype 使用指南

> **Maven Archetype** - 快速生成基于 DDD 架构的 Java 项目骨架

---

## 创建 Archetype（从现有项目）

### 前置要求

- **JDK**: 25 或更高版本
- **Maven**: 3.8 或更高版本

### 创建步骤

**重要**：需要在 archetype 项目根目录下执行命令

#### Windows PowerShell

```powershell
# 1. 进入项目根目录
cd <你的项目目录>\web-quick-start-domain

# 2. 从现有项目创建 archetype
mvn archetype:create-from-project -s "C:\Users\Administrator\scoop\apps\maven\current\conf\settings.xml"
```

> 如果不行的话可以尝试使用IDEA中archetype插件的能力

---

## 安装到本地仓库

### 安装步骤

**重要**：需要进入生成的 archetype 目录执行命令

#### Windows PowerShell

```powershell
# 1. 进入生成的 archetype 目录
cd <你的项目目录>\web-quick-start-domain\_output\archetype

# 2. 安装到本地仓库（跳过测试）
mvn clean install -DskipTests

# 3. 验证安装成功
mvn archetype:generate -DarchetypeCatalog=local
# 查看输出中是否包含 org.smm.archetype:web-quick-start-domain
```

**Linux Bash 替换**（将 ` ` 替换为 `\`）：

```bash
mvn clean install -DskipTests
mvn archetype:generate -DarchetypeCatalog=local
```

---

## 使用 Archetype 生成新项目

### 方式一：命令行参数（推荐）

**重要**：需要在目标目录下执行命令（即你希望生成项目的父目录）

#### Windows PowerShell

```powershell
# 1. 进入目标目录（项目生成的父目录）
cd D:\Develop\code\mine\projects

# 2. 生成新项目
mvn archetype:generate `
  -DarchetypeCatalog=local `
  -DarchetypeGroupId=org.smm.archetype `
  -DarchetypeArtifactId=web-quick-start-domain `
  -DarchetypeVersion=1.0.0 `
  -DgroupId=com.yourcompany `
  -DartifactId=your-project `
  -Dpackage=com.yourcompany.yourproject `
  -Dversion=1.0.0-SNAPSHOT `
  -DinteractiveMode=false
```

当然也可以使用交互式安装

```bash
mvn archetype:generate -DarchetypeCatalog=local
```

**Linux Bash 替换**（将 ` ` 替换为 `\`）：

```bash
mvn archetype:generate \
  -DarchetypeCatalog=local \
  -DarchetypeGroupId=org.smm.archetype \
  -DarchetypeArtifactId=web-quick-start-domain \
  -DarchetypeVersion=1.0.0 \
  -DgroupId=com.yourcompany \
  -DartifactId=your-project \
  -Dpackage=com.yourcompany.yourproject \
  -Dversion=1.0.0-SNAPSHOT \
  -DinteractiveMode=false
```

### 方式二：交互式生成

**重要**：需要在目标目录下执行命令

#### Windows PowerShell

```powershell
# 1. 进入目标目录
cd D:\Develop\code\mine\projects

# 2. 启动交互式生成
mvn archetype:generate

# 3. 搜索并选择 archetype
# 输入 "org.smm.archetype" 或直接选择编号

# 4. 按照提示输入项目信息：
#    - groupId: 组织 ID（如 com.example）
#    - artifactId: 项目 ID（如 my-awesome-project）
#    - version: 版本号（默认 1.0.0-SNAPSHOT）
#    - package: Java 包名（默认与 groupId 相同）
```

### 方式三：IDEA 集成

1. **File** → **New** → **Project**
2. 选择 **Maven Archetype**
3. 点击 **Add Archetype**，输入：
    - GroupId: `org.smm.archetype`
    - ArtifactId: `web-quick-start-domain`
    - Version: `1.0.0`
4. 选择刚添加的 Archetype
5. 填写项目信息（GroupId、ArtifactId 等）
6. 点击 **Create**

---

## 生成项目后的操作

### 快速启动

**重要**：需要在生成的项目根目录下执行命令

#### Windows PowerShell

```powershell
# 1. 进入生成的项目目录
cd D:\Develop\code\mine\projects\your-project

# 2. 编译项目（跳过测试）
mvn clean compile -DskipTests

# 3. 启动应用
mvn spring-boot:run -pl start
```

**Linux Bash 替换**（将 ` ` 替换为 `\`）：

```bash
mvn clean compile -DskipTests
mvn spring-boot:run -pl start
```

### 访问应用

应用启动后，访问：

- **应用首页**: http://localhost:9102/quickstart
- **API 文档（Swagger）**: http://localhost:9102/quickstart/openapi-doc.html
- **健康检查**: http://localhost:9102/quickstart/actuator/health

---

## 生成的项目结构

```
your-project/
├── domain/              # 领域层
├── app/                 # 应用层
├── infrastructure/      # 基础设施层
├── adapter/             # 接口层
├── start/               # 启动模块
├── test/                # 测试模块
├── .specify/            # 项目规范目录
├── README.md            # 项目说明
└── pom.xml              # Maven 配置
```

---

## Archetype 信息

| 项目              | 值                        |
|-----------------|--------------------------|
| **GroupId**     | `org.smm.archetype`      |
| **ArtifactId**  | `web-quick-start-domain` |
| **Version**     | `1.0.0`                  |
| **Spring Boot** | 4.0.2                    |
| **Java**        | 25                       |

---

## 常见问题

### Q: 创建 archetype 失败，提示 settings.xml 不存在？

**错误信息**：

```
[ERROR] The specified user settings file does not exist: C:\Users\Administrator\.m2\settings.xml
```

**原因**：`maven-archetype-plugin` 在创建 archetype 时会启动一个新的 Maven 进程，尝试读取用户 settings.xml，但文件不存在导致失败。

**解决方案：使用全局 settings.xml**

如果不需要用户级配置，可以指定使用全局 settings.xml：

#### Windows PowerShell

```powershell
cd <你的项目目录>\web-quick-start-domain

# 使用全局 settings.xml 创建 archetype
mvn clean archetype:create-from-project -f -DskipTests -Dmaven.userSettings="C:\Users\Administrator\scoop\apps\maven\current\conf\settings.xml"
```

**Linux Bash 替换**（将 ` ` 替换为 `\`）：

```bash
mvn archetype:create-from-project -DskipTests -Dmaven.userSettings="/path/to/maven/conf/settings.xml"
```

**注意**：将 `C:\Users\Administrator\scoop\apps\maven\current\conf\settings.xml` 替换为你实际的 Maven 全局配置路径。

### Q: 创建 archetype 失败怎么办？

确保在项目根目录下执行命令，并且已安装 JDK 25 和 Maven 3.8+。

### Q: 安装 archetype 后找不到？

检查是否进入了正确的目录：`target/generated-sources/archetype`

### Q: 生成项目后编译失败？

检查生成的 `pom.xml` 中的 Java 版本是否与本地 JDK 版本一致。

### Q: Windows PowerShell 命令太长？

将长命令复制到 `.ps1` 脚本文件中执行，或使用反引号 ` ` 进行换行。

---

## 目录说明

执行命令时需要切换到以下目录：

| 操作               | 目录路径               | 说明                                   |
|------------------|--------------------|--------------------------------------|
| **创建 archetype** | `项目根目录`            | `web-quick-start-domain`             |
| **安装 archetype** | `生成的 archetype 目录` | `target/generated-sources/archetype` |
| **生成新项目**        | **目标目录**           | 你希望生成项目的父目录                          |
| **启动新项目**        | **生成的项目根目录**       | `your-project`                       |

---

**文档版本**: v1.0
**最后更新**: 2026-02-03
