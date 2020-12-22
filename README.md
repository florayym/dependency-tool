# 依赖分析工具
## 项目构建与依赖
### Gradle 构建
将 maven 构建更改为 gradle 构建，根据原项目的 POM 文件生成对应依赖，下载对应依赖包存于 `libs` 目录下。

<details><summary>具体的项目依赖及其版本</summary>

- antlr4-maven-plugin-4.7.2.jar
- antlr4-runtime-4.8.jar
- commons-io-2.6.jar
- ehcache-core-2.5.1.jar
- jackson-databind-2.9.10.5.jar
- jackson-core-2.9.10.jar
- jackson-annotations-2.9.10.jar
- junit-4.12.jar
- mockito-all-1.9.5.jar
- picocli-3.8.2.jar
- poi-3.16.jar
- poi-ooxml-3.16.jar
- slf4j-api-1.7.25.jar
- slf4j-log4j12-1.7.25.jar
- log4j-1.2.17.jar
- backport-util-concurrent.jar
- plexus-utils-3.3.0.jar
- org.eclipse.cdt.core_6.9.0.201909091953.jar
- org.eclipse.core.jobs_3.10.500.v20190620-1426.jar
- org.eclipse.core.runtime_3.14.0.v20180417-0825-4.8.0.jar
- org.eclipse.equinox.common_3.10.500.v20190815-1535.jar
- org.eclipse.equinox.preferences_3.7.500.v20190815-1535.jar
- org.eclipse.equinox.registry_3.8.500.v20190714-1850.jar
- org.eclipse.core.resources_3.13.500.v20190819-0800.jar
- org.eclipse.osgi_3.15.0.v20190830-1434.jar
- commons-collections4-4.4.jar
- jrubyparser-0.5.5-SNAPSHOT.jar
- yydebug.jar

</details>

### ANTLR
需从 [官网](https://www.antlr.org/download.html) 下载 `antlr-4.8-complete.jar` ，执行
```shell
java -jar antlr-4.8-complete.jar yourLanguageGrammar.g4 -visitor
```
由 `src\main\antlr4\depends\extractor\<language>'` 目录中特定语言的语法规则文件 g4 生成目标编程语言 Java 格式的解析器文件，并分别放入 `src\main\java\depends\extractor\<language>` 中。

### 子模块
本项目还依赖于原作者的 [utils](https://github.com/multilang-depends/utils) 和 [jruby-parser](https://github.com/jruby/jruby-parser) 两个项目，需将其置于 `src\main\java` 目录中。

## 输入参数
1. 添加 `--db` 参数，设置数据库配置文件的路径，例如“ `config.json` ”。
1. 添加 `--date` 参数，设定检索数据库中特定日期的数据进行分析，例如“ `2020-08-20` ”。
1. 添加 `-l` 参数，打印文件解析日志。
1. 扩展 `-g, --granularity` ，选择以文件（类）、包、方法或指定层数为粒度分析依赖。
1. 扩展 `-f, --format` 指定输出分析结果的文件格式为 JS 或 Json 等格式。

## 输出文件格式
1. 添加 `src\main\java\depends\format\DBUtils.java` 文件，提供MySQL数据库配置、操作的各种命令。
1. 添加 `src\main\java\depends\format\javascript\JavaScriptFormatDependencyDumper.java` 文件，继承自抽象类 `AbstractFormatDependencyDumper` ，实现分析结果输出为本地JS文件，同时持久化存储至数据库中。
与原项目不同， `JavaScriptFormatDependencyDumper` 生成的 `analyzed.js` 中 `dependencies` 对象结构包括起始点、终点、具有的依赖关系的类型及每一类型的出现次数，示例如下：
    ```js
    var dependencies = {
        nodes: [
            {"name": "com.module.name1", "loc": 0, "heat": 0.0},
            {"name": "com.module.name2", "loc": 0, "heat": 0.0},
        ],
        links: [
            {"source": "com.module.name1", "dest": "com.module.name2", "values": {"Import": 1.0, "Use": 6.0}},
        ]
    };
    ```
1. 在 `src\main\java\depends\format\AbstractFormatDependencyDumper.java` 中定义并初始化参数 `protected DBUtils db`。当设定为数据库存储模式时，进行数据库连接操作。
1. 修改 `src\main\java\depends\format` 目录下的多个文件，以传入数据库配置地址。

## 分析粒度
添加 `src\main\java\depends\generator\PackageDependencyGenerator.java` ，筛选符合 `entity instanceof PackageEntity` 条件的实体，作为依赖分析的对象。depends 项目将分析对象抽象为 `Entity` 类，这个抽象类是所有实体，例如 `FileEntity` （类粒度）和 `PackageEntity` （包粒度）的父类。每一种粒度的依赖关系生成器仅接收相应粒度的 Entity 作为节点，刻画依赖关系。而 [Apk Dependency Graph](https://github.com/alexzaitsev/apk-dependency-graph) 项目的粒度识别则是基于类路径字符串解析，从中截取一定层级的包路径，将其记录为一个节点。

## 可视化模块
### gui 模块导入
使用 [ADG](https://github.com/alexzaitsev/apk-dependency-graph) 项目的 [gui](https://github.com/alexzaitsev/apk-dependency-graph/tree/master/gui) 模块进行可视化，需要进行展示的文件位于 `gui\results` 目录下。模块结构如下：
```
gui
|── results
|   └── analyzed.js
|── scripts
|   |── d3-setup-custom.js
|   |—— dependency.css
|   |—— graph-actions-select-compiled.js
|   └── parse-compiled.js
└── index.html
```

### 分组着色策略

1. 模块名称前缀

    修改 `parse-compiled.js` ，添加 `level` 参数，指定寻找分组前缀时所依据的目录深度，最小层数为 1 ，并实现 `_getPrefixName(name, level)` 方法获取前缀：
    ```js
        var prefix = name.split(/[\/\.]/g);
    ```
    其中，使用正则表达式适配以 `/` 或 `.` 作为类或包路径分隔符的节点名称。

1. 模块修改频率

    修改 `parse-compiled.js` ，添加 `_createHeatGrouping` 方法，根据当前粒度下模块的被修改的频率与幅度（ heat ）大小，使用 RGB 进行调色。

## 数据库配置添加
在项目根目录下添加 `config.json` 文件，用于添加数据库配置。结构如下：
```json
{
  "sql-account": "MySQL数据库用户名",
  "sql-password": "MySQL数据库密码",
  "sql-ip": "MySQL数据库IP地址",
  "sql-port": "MySQL服务所用端口号",
  "db-name": "目标数据库名称"
}
```

## 项目发布

在 `build.gradle` 中创建新的自动化任务，功能及使用说明如下
1. `jar` ：可将项目打包为 jar 包
1. `packageDistribution` ：可由已生成的 jar 包自动配置依赖，构建分布压缩包
1. `distributions` ：添加该插件，可执行其中的 `distZip` 任务一步打包并构建分布压缩包 `depends-dist-x.x.x.zip`
1. `unpackFiles` ：解压缩包

## 批量项目分析

添加项目根目录下的 `depends.sh` ，用于在计算云上执行对指定目录下的所有项目的依赖分析。

```shell
Usage: $0 -j <path/to/depends-version.jar> -i <repo/path/> -l <java|cpp|python|kotlin|pom|ruby|xml> -o <output/path/> [-f [js|,mysql|,json|,xml|,excel|,detail|,dot|,plantuml]] [-g <package|file|method>] [-c <path/to/config.json>] [-t <date>] [-d]

PARAMETER DESCRIPTION:
    -j jar-path                                     path to depends-x.x.x.jar
    -i input-path                                   path to repo to be analyzed
    -l language                                     project language
    -o output-path                                  output path
    -f js|,json|,xml|,excel|,detail|,dot|,plantuml  output file format
    -g package|file|method||L#                      granularity
    -c json-file                                    database configuration json
    -t date                                         analyze a specific date
    -d                                              enable parsing logging
```
并于 `update.sh` 中执行命令。示例如下：
```shell
$ ./depends.sh -i /path/to/projects/ -l java -o /output/path/ -f js,mysql -g package -c config/config.json #-d for enabling parse logging
```

## 单个项目分析

```shell
java -jar /path/to/depends-1.0.0.jar java /path/to/project/ analyzed -d /output/path/ -f js -g granularity -l
```
