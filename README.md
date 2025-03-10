# API_attach



## Java API获取 

- 输入： 给定一个 Java 服务，项目地址为 https://github.com/javaweb-rasp/javaweb-vuln 
- 输出： 通过 JavaAgent 技术实现对该服务中 Java API 的获取（需要能获取GET和POST请求参数），并将所获取的 API 信息以 JSON 格式写入本地文件。



## 项目启动

运行这个项目，你可以按照以下步骤操作：

1. 首先构建整个项目：
```bash
mvn clean install
```

2. 运行测试模块并附加 agent：
```bash
java -javaagent:api-agent/target/api-agent-1.0.0-SNAPSHOT.jar -jar 目标程序.jar
```
3. 在目标文件生成在监控程序的``\api-output\api-collection.json`中

这个项目可以用于 API 文档自动生成、API 监控等场景。如果你有关于具体实现或使用的问题，欢迎继续询问。

