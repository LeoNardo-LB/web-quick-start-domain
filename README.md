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

##### 3、本地安装骨架

进入上一步生成的target文件目录，具体路径：`<你的项目根目录>\target\generated-sources\archetype`

```shell
cd  .\_output\archetype\
```

然后执行`mvn install`命令安装骨架到本地maven仓库。安装完成后，会输出本地骨架的路径，并在本地仓库中创建一个骨架坐标文件。坐标文件记录着本地骨架对于本地仓库的相对路径。

###### 错误信息
如果出现下面错误，则需要手动进入`archetype-metadata.xml`文件修改各个模块的`dir`目录（此项目中是加上`app/`前缀）
```bash
[ERROR] ResourceManager: unable to find resource 'archetype-resources/testing/pom.xml' in any resource loader.
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  0.728 s
[INFO] Finished at: 2025-07-16T02:21:46+08:00
[INFO] ------------------------------------------------------------------------
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-archetype-plugin:3.4.0:integration-test (default-integration-test) on project web-quick-start-domain:
[ERROR] Archetype IT 'basic' failed: Error merging velocity templates: Unable to find resource 'archetype-resources/testing/pom.xml'
[ERROR] -> [Help 1]
[ERROR]
[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.
[ERROR] Re-run Maven using the -X switch to enable full debug logging.
[ERROR]
[ERROR] For more information about the errors and possible solutions, please read the following articles:
[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/MojoExecutionException
```
`archetype-metadata.xml`位置
```shell
target/generated-sources/archetype/src/main/resources/META-INF/maven/archetype-metadata.xml
```


**安装成功输出的内容**

```shell
[INFO] --- install:3.1.1:install (default-install) @ habit-reminder-portal ---
[INFO] Installing E:\Dev\code\scaffolding\nexa-app-archetype\target\generated-sources\archetype\pom.xml to E:\Dev\software\apache-maven-3.9.6\local_reposity\org\ssm\start\habit-reminder-portal\1.0.0\habit-reminder-portal-1.0.0.pom
[INFO] Installing E:\Dev\code\scaffolding\nexa-app-archetype\target\generated-sources\archetype\target\habit-reminder-portal-1.0.0.jar to E:\Dev\software\apache-maven-3.9.6\local_reposity\org\ssm\start\habit-reminder-portal\1.0.0\habit-reminder-portal-1.0.0.jar
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
            <description>Parent pom providing dependency and plugin management for applications built with Maven</description>
        </archetype>
    </archetypes>
</archetype-catalog>
```

## 使用骨架

有两种创建骨架的方式

1. 在IDEA中通过界面创建骨架

2. 通过命令行指定骨架并创建项目

### 通过IDEA界面创建骨架

在IDEA中指定骨架文件`archetype-catalog.xml`的位置（一般在本地maven仓库的根目录下），然后在新建项目时指定使用这个骨架文件，此时就会出现骨架配置文件中所有可用的骨架。

### 通过命令行创建骨架（推荐）

```shell
mvn archetype:generate -DarchetypeCatalog=local
    -DarchetypeGroupId=<骨架的groupId>
    -DarchetypeArtifactId=<骨架的artifactId>
    -DarchetypeVersion=<骨架的版本号>
    -DgroupId=<新项目的groupId>
    -DartifactId=<新项目的artifactId>
    -Dversion=<新项目的version>
```

比如在windows终端中创建：

```shell
mvn archetype:generate -DarchetypeCatalog=local ^
  -DinteractiveMode=false ^
  -DarchetypeGroupId=org.smm.archetype ^
  -DarchetypeArtifactId=web-quick-start-domain ^
  -DarchetypeVersion=1.0.0 ^
  -DgroupId=org.ssm ^
  -DartifactId=web-demo ^
  -Dversion=1.0.0-SNAPSHOT
```

> 注意`^`后不能有任意其他字符，否则会被windwos终端识别成下一行导致失败

## 骨架配置信息

```shell
archetype.groupId=org.smm.archetype
archetype.artifactId=web-quick-start-domain
archetype.version=1.0.0
# 排除的文件
excludePatterns=**/.idea/**,**/target/*,logs/**,modules/**,**/*.iml,**/logs,**/logs/*,**/logs/**,README.md,data_h2/**
```

### 有可能的错误

##### The specified user settings file does not exist: C:\Users\Administrator\.m2\settings.xml

如果显示找不到settings位置，可以使用`-s`手动指定settings文件的位置
