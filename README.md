# Introduction

*Depends* is a source code dependency extraction tool, designed to infer syntactical relations among source code entities, such as files and methods, from various programming languages. Our objective is to provide a framework that is easily extensible to support dependency extraction from different programming languages and configuration files, so that other high-level analysis tools can be built upon it, in a language-independent fashion. Sample applications include code visualization, program comprehension, code smell detection, architecture analysis, design refactoring, etc.  

Our creation of *Depends* is motivated by the observations that different vendors, such as Understand&trade;, Structure 101 &trade;, and Lattix&trade;, created their own dependency extraction tools that are packaged with their other analysis functions. To conduct new analysis, vendors and researchers must each create their own dependency extraction tools, or use the output of other tools, which are usually not designed to be reusable. 

We believe that dependency analysis of software source code is one of the most important foundations of software engineering. Given the growing number of systems built on new languages and multi-languages, the need of a flexible, extensible multi-language dependency extraction framework, with simple and unified interfaces is widely recognized. 

*Depends* is open source and free, to promote community collaboration, to avoid repetitive work, and to improve the quality of analytical tools.

# Build tips

## Create New Gradle Builds

Create a build:

```shell
> gradle init
```

Then include [utils](https://github.com/multilang-depends/utils) and [jruby-parser](https://github.com/jruby/jruby-parser) in the `/src/main/java/`, and resolve all the other dependencies.

## Generate ANTLR Recognizer

ANTLR is distributed as a Java jar file. Under the same directory of your antlr jar file, run the following from the command line:

```shell
# Download ANTLR tool for Java target
> wget https://www.antlr.org/download/antlr-4.8-complete.jar
> java -jar antlr-4.8-complete.jar yourLanguageGrammar.g4 -visitor
```

After all the language recognizer (listeners and visitors) are generated, copy them to the `extractor/<language>` folder respectively.

## Download dependencies

Retrieve all the dependency jars into `./libs` directory. Import the URLs from `dependencies.txt` file.

```shell
$ wget -i dependencies.txt -P libs
```

# How to use *Depends*

## Run with DevEco Studio

Program arguments:

```shell
java C:\Users\y50016379\DevEcoStudioProjects\apk-dependency-graph\src analyzed -d gui\result -f js -g package
```

## Download and installation

You could download the latest version of *Depends* from https://github.com/multilang-depends/depends/releases/,  
and then unzip the ```depends-*version*.tgz``` file in any directory of your computer.

*Depends* is written in java, so it could be run on any OS with a JRE or JDK envoirment (like Windows, Linux or Mac OS). 

## Run it from commmand line

Following the single responsibility principle, *Depends* is designed for the purpose of extracting dependencies only. It only provides CLI interface, without GUI. But you can convert the output of *Depends* into the GUI of other tools, such as GraphViz(http://graphviz.org/), PlantUML(http://plantuml.com/), and DV8 (https://www.archdia.com). 

You could run *Depends* in the following ways: ```depends.sh``` on Linux/Mac, ```depends.bat``` on Microsoft Windows, or  ```java -jar depends.jar```.

Note: If you encountered Out of Memory error like ```GC overhead limt exceed```, please expands
the JVM memory like follwing ```java -Xmx4g -jar depends.jar <args>```.

## Parameters

The CLI tool usage could be listed by ```depends --help```, like following:

    Usage: depends [-hms] [--auto-include] [-d=<dir>] [-g=<granularity>]
                   [-p=<namePathPattern>] [-f=<format>[,<format>...]]...
                   [-i=<includes>[,<includes>...]]... <lang> <src> <output>
          <lang>                 The language of project files: [cpp, java, ruby, python,
                                   pom]
          <src>                  The directory to be analyzed
          <output>               The output file name
          --auto-include         auto include all paths under the source path (please
                                   notice the potential side effect)
      -i, --includes=<includes>[,<includes>...]
                                 The files of searching path
      -d, --dir=<dir>            The output directory
      -f, --format=<format>[,<format>...]
                                 The output format: [json(default),xml,excel,dot,
                                   plantuml]
      -g, --granularity=<granularity>
                                 Granularity of dependency.[file(default),method,L#(the level of folder. e.g. L1=1st level folder)  ]
      -h, --help                 Display this help and exit
      -s, --strip-leading-path   Strip the leading path.
      
      -m, --map                  Output DV8 dependency map file.
      -p, --namepattern=<namePathPattern>
                                 The name path separators.[default(/),dot(.)


To run *Depends*, you need to specify 3 most important parameters: ```lang```, ```src```,```output```, as explained above. 

## Remember to specify include paths

Please note that for most programming languages, such as ```C/C++, Ruby, Maven/Gradle```, the ```--includes``` path is important for *Depends* to find the right files when conducting code dependency analysis, similar to Makefile/IDE.  Otherwise, the extracted dependencies may not be accurate. 

Do not specify include paths outside of src directory (e.g. system level include paths, or external dependencies) because *Depends* will not process them.

```--auto-include``` is a useful parameter to simplify the input of include dirs: with this parameter, *Depends* will include all sub-directories of ```src```.

For ```Java``` programs, you are not required to specify include paths, because the mapping between java file paths are explicitly stated in the import statements.

### Output

The output of *Depends* can be exported into 5 formats: json, xml, excel, dot, and plantuml. Due to the limitation of MS excel,  you can only export into a excel file if the number of elements is less than 256.)

Dot files could be used to generate graphs using GraphViz (http://graphviz.org/).

Plantuml files could be used to generate UML diagram using PlantUML (http://plantuml.com/).

Json and XML files could be used to generate Design Structure Matrices (DSMs) using DV8 (https://www.archdia.net).

The detail of json/xml format could be found [here](./doc/output_format.md).

### How many dependency types does *Depends* support?

*Depends* supports major dependency types, including:
1. Call: function/method invoke
1. Cast: type cast
1. Contain: variable/field definition
1. Create: create an instance of a certain type
1. Extend: parent-child relation
1. Implement: implemented interface
1. Import/Include: for example, java ```import```, c/c++ ```#include```, ruby ```require```.
1. Mixin: mix-in relation, for example ruby include
1. Parameter: as a parameter of a method
1. Return: returned type
1. Throw: throw exceptions
1. Use: use or set variables
1. ImplLink: the implementation link between call and the implementation of prototype.
1. Set: 

For detail of supported types, please refer to [here](./doc/dependency_types.md)

# Acknowledgement

This project is built upon the excellent work of [multilang-depends](https://github.com/multilang-depends) projects.

# 程序修改说明

## 项目构建与依赖
### Gradle 构建
将 maven 构建更改为 gradle 构建，根据原项目的 POM 文件生成对应依赖，下载对应依赖包存于 `libs` 目录下。项目依赖及其版本如下：
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

### ANTLR
需从 [官网](https://www.antlr.org/download.html) 下载 `antlr-4.8-complete.jar`，执行
```shell
java -jar antlr-4.8-complete.jar yourLanguageGrammar.g4 -visitor
```
由 `src\main\antlr4\depends\extractor\<language>'` 目录中特定语言的语法规则文件 g4 生成目标编程语言 Java 格式的解析器文件，并分别放入 `src\main\java\depends\extractor\<language>` 中。

### 子模块
本项目还依赖于原作者的 [utils](https://github.com/multilang-depends/utils) 和 [jruby-parser](https://github.com/jruby/jruby-parser) 两个项目，需将其置于 `src\main\java` 目录中。

## 输入参数
在 `src\main\java\depends\DependsCommand.java` 中添加 `--db` 参数，设置数据库配置文件的路径，例如 `"config.json"` 。并且，修改了
1. `-g, --granularity`，选择以文件（类）、包、方法或指定层数为粒度分析依赖。
1. `-f, --format` 指定输出分析结果的文件格式为 JS 或 Json 等格式。

## 输出文件格式
1. 添加 `src\main\java\depends\format\DBUtils.java` 文件，提供MySQL数据库配置、操作的各种命令。
1. 添加 `src\main\java\depends\format\javascript\JavaScriptFormatDependencyDumper.java` 文件，继承自抽象类 `AbstractFormatDependencyDumper`，实现分析结果输出为本地JS文件，同时持久化存储至数据库中。
与原项目不同， `JavaScriptFormatDependencyDumper` 生成的 `analyzed.js` 中 `dependencies` 对象结构包括起始点、终点、具有的依赖关系的类型及每一类型的出现次数，示例如下：
    ```js
    var dependencies = {links:[
        {"source": "com.huawei.screenrecorder", "dest": "com.huawei.screenrecorder.activities", "values": {"Call": 2.0, "Import": 2.0, "Use": 3.0, "Create": 2.0, "Contain": 1.0}},
        {"source": "com.huawei.screenrecorder", "dest": "com.huawei.screenrecorder.vrscreenrecorder", "values": {"Call": 15.0, "Import": 6.0, "Use": 23.0}},
        {"source": "com.huawei.screenrecorder", "dest": "com.huawei.screenrecorder.util", "values": {"Call": 460.0, "Import": 83.0, "Use": 589.0, "Contain": 11.0}},
    ]};
    ```
1. 在 `src\main\java\depends\format\AbstractFormatDependencyDumper.java` 中定义并初始化参数 `protected DBUtils db`。当设定为数据库存储模式时，进行数据库连接操作。
1. 修改 `src\main\java\depends\format` 目录下的多个文件，以传入数据库配置地址。

## 分析粒度
添加 `src\main\java\depends\generator\PackageDependencyGenerator.java`，筛选符合 `entity instanceof PackageEntity` 条件的实体，作为依赖分析的对象。depends 项目将分析对象抽象为 `Entity` 类，这个抽象类是所有实体，例如 `FileEntity` （类粒度）和 `PackageEntity` （包粒度）的父类。每一种粒度的依赖关系生成器仅接收相应粒度的 Entity 作为节点，刻画依赖关系。而 [Apk Dependency Graph](https://github.com/alexzaitsev/apk-dependency-graph) 项目的粒度识别则是基于类路径字符串解析，从中截取一定层级的包路径，将其记录为一个节点。

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
修改 `parse-compiled.js` ，添加 `level` 参数，指定寻找分组前缀时所依据的目录深度，最小层数为 1 ，并实现函数
```js
_getPrefixName: function _getPrefixName(name, level) {

    level = level < 1 ? 1 : level;

    var prefix = name.split(/[\/\.]/g);
    return prefix[Math.max(prefix.length - level, 0)];
}
```
其中，使用正则表达式适配以 `/` 或 `.` 作为类或包路径分隔符的节点名称。最后，调用该函数生成分组前缀
```js
var prefix = this._getPrefixName(name, level);
```

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

## 批量运行

添加项目根目录下的 `run-depends.sh` ，用于在计算云上执行对指定目录下的所有项目的依赖分析。

```shell
Usage: $0 -j <path/to/depends-version.jar> -i <repo/path/> -l <java|cpp|python|xml|kotlin> -o <output/path/> [-f <js(default)|json|xml|excel|detail|dot|plantuml>] -g <file|method|package> [-c <path/to/config.json>]

PARAMETER DESCRIPTION:
    -j jar-path                                        path to depends-x.x.x.jar
    -i input-path                                      path to repo to be analyzed
    -l java|cpp|python|xml|kotlin                      project language
    -o output-path                                     output path
    -f js(default)|json|xml|excel|detail|dot|plantuml  output file format
    -g file(default)|method|package|L#                 granularity
    -c json-file                                       database configuration json
```
并于 `update.sh` 中执行命令。示例如下：
```shell
$ ./run-depends.sh -j ~/workspaces/depends-dist-0.9.6/depends-0.9.6.jar -i ~/workspaces/MP_Test/ -l java -o $BASEPATH/HAWebsite/HeatAnalyzeWebsite/app/static/map/ -f js -g package -c $BASEPATH/config/config.json
```
