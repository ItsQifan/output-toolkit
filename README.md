<p align="center">
  <img src="https://img.shields.io/badge/version-1.0.0-blue?style=flat-square" alt="version">
  <img src="https://img.shields.io/badge/license-MIT-green?style=flat-square" alt="license">
  <img src="https://img.shields.io/badge/Java-17-orange?style=flat-square" alt="java">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen?style=flat-square" alt="spring boot">
  <img src="https://img.shields.io/badge/build-Maven%20Wrapped-C71A36?style=flat-square&logo=apachemaven" alt="maven">
  <img src="https://img.shields.io/badge/platform-Windows%20%7C%20macOS%20%7C%20Linux-lightgrey?style=flat-square" alt="platform">
</p>

<h1 align="center">🧰 output-toolkit</h1>

<p align="center">
  <strong>面向 Java 开发者的「万能工具箱」—— 一个仓库，四个利器，十倍效率</strong>
</p>

<p align="center">
  代码生成 · 加密工具 · 快捷启动器 · NL2SQL 智能查询<br>
  集成 Spring Boot 3.5.7 + Java 17 + MCP 协议，开箱即用
</p>

<p align="center">
  <a href="#-模块概览">模块概览</a> ·
  <a href="#-快速开始">快速开始</a> ·
  <a href="#-使用教程">使用教程</a> ·
  <a href="#-项目结构">项目结构</a> ·
  <a href="#-常见问题">FAQ</a> ·
  <a href="#-贡献指南">贡献指南</a>
</p>

---

## 📖 项目简介

**output-toolkit** 是一个面向 Java 开发者的综合工具箱，聚合了日常开发中高频使用的四大能力模块。无论你是写 CRUD、处理加密、快速启动工作环境，还是想用 AI 对话式查询数据库 —— 一个仓库全部搞定。

### 😫 痛点场景

| 场景 | 传统做法 | 用 output-toolkit |
|------|---------|-------------------|
| 建好表后要写 DTO/Mapper/Service | 手写 + 复制粘贴，半小时起步 | 一条建表 SQL → 3 秒全自动生成 |
| 数据库新增字段 | 逐个文件修改 DTO/VO/XML | 一键增量更新，只更新该更新的 |
| 交付加密 ZIP 给客户 | 打开压缩软件 → 设密码 → 打包 | `POST /zip/encrypt` 接口调用 |
| 每天上班打开一堆软件 | 逐个双击图标 | `Ctrl+1` 快捷键一键全开 |
| 想用 AI 查数据库 | 手动写 SQL → 复制粘贴给 AI | Claude 直接对话式查库，零摩擦 |

---

## ✨ 核心亮点

- 🧩 **模块解耦，按需选用** —— 四个独立模块，只打包你需要的即可
- ⚡ **SQL → 代码，秒级生成** —— 支持 CREATE TABLE / SELECT / INSERT / JSON 四种输入源
- 🔄 **增量更新，精准高效** —— 新增/删除字段时，只更新 DTO/VO/Mapper.xml，不动手写代码
- 🔐 **AES-256 军用级加密** —— ZIP + 字符串双加密能力，密钥可托管 KMS
- 🖥️ **桌面快捷启动器** —— 基于 JavaFX + JNativeHook，全局快捷键唤醒
- 🤖 **MCP 协议 NL2SQL** —— Claude Desktop 原生集成，对话即查询，支持 SSH 隧道 + SSL
- 🎯 **零依赖冲突** —— 统一 parent POM 管理版本，Hutool + Fastjson2 + HikariCP 稳定组合

---

## 🧭 模块概览

| 模块 | 一句话介绍 | 核心能力 |
|------|-----------|----------|
| [🧬 codegenerator](#-codegenerator-代码生成器) | SQL/JSON → Java 代码全自动生成 | DDL 解析 · 模板引擎 · 增量更新 · 命名策略 |
| [🔐 crypto](#-crypto-加密工具箱) | 文件/字符串加解密一站搞定 | ZIP AES-256 · 字符串 AES-CBC · 密码保护 |
| [🚀 justopen](#-justopen-快捷启动器) | 桌面快捷启动器，一键唤醒工作环境 | 托盘常驻 · 全局热键 · 软件/网址分组 |
| [🗄️ mcp-nl2sql](#-mcp-nl2sql-自然语言查库) | Claude 对话式 MySQL 查询 MCP 服务 | 8 个 MCP 工具 · SSH 隧道 · STDIO/SSE · 多库模式 · SQL 注入防护 |

---

## 🛠️ 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | **17** | 运行环境 |
| Spring Boot | **3.5.7** | 应用框架 |
| Maven | **3.9+** | 构建工具（内置 Maven Wrapper） |
| FreeMarker | — | 代码模板引擎（codegenerator） |
| JSqlParser | **5.3** | SQL 语法解析（codegenerator） |
| zip4j | **2.11.5** | ZIP 文件加密（crypto） |
| JavaFX | — | 桌面 GUI（justopen） |
| JNativeHook | — | 全局键盘监听（justopen） |
| HikariCP | — | 数据库连接池（mcp-nl2sql） |
| Spring AI MCP | — | MCP 协议支持（mcp-nl2sql） |
| Hutool | **5.8.25** | 通用工具库 |
| Fastjson2 | **2.0.58** | JSON 解析 |

---

## 📦 快速开始

### 前置要求

- **JDK 17+**
- **Maven 3.9+**（或用项目自带 `mvnw`）
- （仅 justopen）Windows/macOS/Linux 桌面环境
- （仅 mcp-nl2sql）可访问的 MySQL 实例

### 克隆 & 编译

```bash
# 克隆仓库
git clone https://github.com/your-org/output-toolkit.git
cd output-toolkit

# 编译全部模块（跳过测试）
./mvnw clean package -DskipTests
```

### 一键启动各模块

```bash
# ---- codegenerator ----
cd output-toolkit-codegenerator
../mvnw spring-boot:run

# ---- crypto ----
cd output-toolkit-crypto
../mvnw spring-boot:run

# ---- justopen ----
cd output-toolkit-justopen
../mvnw spring-boot:run

# ---- mcp-nl2sql ----
cd output-toolkit-mcp-nl2sql
../mvnw spring-boot:run
```

---

## 🚀 使用教程

### 🧬 codegenerator 代码生成器

#### 最简入门：一条建表 SQL → 完整 CRUD 代码

**Step 1** —— 编辑 XML 配置文件 `src/main/resources/generator-config.xml`：

```xml
<tableConfig>
    <genSwitch>true</genSwitch>
    <model>sql</model>
    <authorName>your-name</authorName>
    <sql>
        CREATE TABLE `t_user` (
            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
            `username` varchar(64) NOT NULL COMMENT '用户名',
            `email` varchar(128) COMMENT '邮箱',
            `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
            PRIMARY KEY (`id`)
        ) COMMENT='用户表';
    </sql>
    <!-- 自定义后缀 -->
    <dtoSuffix>DTO</dtoSuffix>
    <mapperSuffix>Mapper</mapperSuffix>
    <managerSuffix>Manager</managerSuffix>
    <!-- 输出路径 -->
    <dtoLocalPath>./generated/dto</dtoLocalPath>
    <mapperLocalPath>./generated/mapper</mapperLocalPath>
</tableConfig>
```

**Step 2** —— 调用接口：

```bash
# 一步生成（推荐，默认端口 8223）
curl -X POST http://localhost:8223/xmlGen/generateXmlAndDoGenerate
```

#### 进阶：增量更新（数据库新增/删除字段）

```bash
# 修改 XML 中的建表语句后，增量更新（只更新 DTO/VO/Mapper.xml，不动 Controller）
curl -X POST http://localhost:8223/xmlGen/generateXmlAndIncrementalUpdate
```

#### 支持的输入源

| 模式 | 说明 | `model` 值 |
|------|------|-----------|
| DDL 建表语句 | `CREATE TABLE ...` | `sql` |
| SELECT 查询 | `SELECT a, b FROM ...` | `sql` |
| INSERT 语句 | `INSERT INTO ... VALUES ...` | `sql` |
| JSON 对象 | `{"name":"xxx","age":18}` | `json` |

#### API 端点一览

```bash
POST /xmlGen/generateXml                  # 仅生成 XML 配置文件
POST /xmlGen/doGenerate                   # 根据 XML 执行代码生成
POST /xmlGen/generateXmlAndDoGenerate     # 一步到位（推荐）
POST /xmlGen/incrementalUpdate            # 增量更新
POST /xmlGen/generateXmlAndIncrementalUpdate  # 生成 XML + 增量更新
```

---

### 🔐 crypto 加密工具箱

#### ZIP 文件加密 & RunClient CLI

除了 REST API，也提供独立 CLI 工具 `RunClient`，无需启动 Spring 容器即可使用：

```bash
# 直接运行 CLI（交互式加密/解密）
cd output-toolkit-crypto
java -cp target/output-toolkit-crypto-1.0.0.jar com.zhouchuanxiang.outputtoolkit.crypto.RunClient
```

```bash
# 或通过 REST API 调用
curl -X POST http://localhost:8080/zip/encrypt \
  -H "Content-Type: application/json" \
  -d '{
    "sourcePath": "/path/to/source",
    "targetZipPath": "/path/to/output.zip",
    "password": "your-strong-password"
  }'

# 解密 ZIP 文件
curl -X POST http://localhost:8080/zip/decrypt \
  -H "Content-Type: application/json" \
  -d '{
    "zipFilePath": "/path/to/encrypted.zip",
    "destDir": "/path/to/extract/",
    "password": "your-strong-password"
  }'
```

#### AES 字符串加密

```java
// 编程式调用
String encrypted = AESUtil.encrypt("hello world");
String decrypted = AESUtil.decrypt(encrypted);
```

> ⚠️ 生产环境请将密钥托管到 KMS（如 AWS KMS / HashiCorp Vault），替换代码中的硬编码密钥。

---

### 🚀 justopen 快捷启动器

#### 功能说明

启动后常驻系统托盘，通过**全局快捷键**唤醒菜单，一键打开配置好的软件和网址。

#### 配置文件 `launcher-config.json`

程序首次运行后会自动在 `~/.output-toolkit-justopen/launcher-config.json` 创建默认配置（含记事本、计算器、VS Code、IDEA 等常用软件示例），可直接编辑该文件自定义。

```json
{
  "browser": "C:/Program Files/Google/Chrome/Application/chrome.exe",
  "hotkey": "CTRL+1",
  "softwareGroups": [
    {
      "name": "💻 开发工具",
      "items": [
        { "name": "IntelliJ IDEA", "path": "C:/Program Files/JetBrains/IntelliJ IDEA/bin/idea64.exe" },
        { "name": "VS Code", "path": "C:/Users/xxx/AppData/Local/Programs/Microsoft VS Code/Code.exe" },
        { "name": "DBeaver", "path": "C:/Program Files/DBeaver/dbeaver.exe" }
      ]
    },
    {
      "name": "📝 办公软件",
      "items": [
        { "name": "Notion", "path": "C:/Users/xxx/AppData/Local/Programs/Notion/Notion.exe" }
      ]
    }
  ],
  "urlGroups": [
    {
      "name": "🌐 工作网站",
      "items": [
        { "name": "GitHub", "url": "https://github.com" },
        { "name": "Jira", "url": "https://your-org.atlassian.net" }
      ]
    }
  ]
}
```

#### 使用方式

1. 启动程序 → 自动最小化到系统托盘
2. 按下 `Ctrl+1`（默认）→ 菜单在屏幕中央弹出
3. 点击软件名 → 自动打开；点击网址 → 浏览器打开
4. 右键托盘图标 → 编辑配置 / 重载 / 退出

---

### 🗄️ mcp-nl2sql 自然语言查库

#### 工作原理

```
用户自然语言 → Claude Desktop → MCP 协议 → mcp-nl2sql → MySQL
                    ↑                                    │
                    └────── 查询结果 ←─────────────────────┘
```

#### 配置 Claude Desktop

编辑 Claude Desktop 配置文件（位置见 [Claude 官方文档](https://docs.anthropic.com/en/docs/claude-code/mcp)）：

```json
{
  "mcpServers": {
    "mcp-nl2sql": {
      "command": "java",
      "args": ["-jar", "output-toolkit-mcp-nl2sql-1.0.0.jar"],
      "env": {
        "MYSQL_HOST": "localhost",
        "MYSQL_PORT": "3306",
        "MYSQL_USER": "root",
        "MYSQL_PASSWORD": "your_password",
        "MYSQL_DATABASE": "your_database",
        "MYSQL_INSERT_ENABLED":"true",
        "MYSQL_UPDATE_ENABLED":"true",
        "MYSQL_DELETE_ENABLED":"true"
      }
    }
  }
}
```

#### 提供的 MCP 工具（8 个）

**核心查询工具（3 个）**

| 工具 | 功能 | 示例 |
|------|------|------|
| `execute_sql` | 执行任意 SQL（SELECT/INSERT/UPDATE/DELETE） | `SELECT * FROM users LIMIT 10` |
| `get_schema_info` | 查询表结构（列名/类型/注释），支持 `db.table` 跨库 | `get_schema_info("mydb.users")` |
| `get_table_sample` | 获取表数据样本（前 N 行，最多 20） | `get_table_sample("users", limit=5)` |

**资源探索工具（4 个）**

| 工具 | 功能 |
|------|------|
| `listResources` | 列出所有可访问的表（单库模式）或数据库（多库模式） |
| `readResource` | 读取完整表数据（`mysql://{table}/data`，LIMIT 100）或库级表列表 |
| `exploreDatabase` | 返回系统性的 4 步数据库探索指导流程 |
| `analyzeTable` | 返回针对指定表的 3 步分析指导流程 |

#### 两种运行模式

| 模式 | 适用场景 | 配置 |
|------|---------|------|
| **STDIO**（推荐） | Claude Desktop 本地使用 | `stdio: true` |
| **SSE** | 远程访问 / Web 客户端 | `protocol: SSE, port: 8000` |

#### SSH 隧道连接

```yaml
# application.yml 中启用 SSH 隧道
mysql:
  ssh:
    enabled: true
    host: your-bastion-host
    port: 22
    user: your-ssh-user
    # 支持密码或密钥认证
```

#### 安全特性

- ✅ **SQL 注入防护** —— 标识符白名单校验（`IdentifierValidator`）
- ✅ **DML 开关控制** —— INSERT/UPDATE/DELETE 可独立禁用
- ✅ **多库/单库模式** —— 自由切换，权限隔离

---

## 📁 项目结构

```
output-toolkit/                              # 🏠 父工程（Spring Boot 3.5.7 + Java 17）
├── pom.xml                                  #   Parent POM，统一版本管理
├── mvnw / mvnw.cmd                          #   Maven Wrapper
│
├── output-toolkit-codegenerator/            # 🧬 代码生成器
│   └── src/main/
│       ├── java/.../codegenerator/
│       │   ├── controller/                  #   REST API 接口
│       │   ├── service/                     #   业务逻辑（SQL 解析、模板渲染、增量更新）
│       │   ├── entity/                      #   DTO / 配置实体
│       │   ├── enums/                       #   枚举定义
│       │   ├── util/                        #   工具类（FreeMarker、表解析、类型映射）
│       │   └── xmlconfig/                   #   XML 配置模型
│       └── resources/
│           ├── generator-config.xml         #   代码生成配置文件
│           └── templates/                   #   FreeMarker 模板文件
│
├── output-toolkit-crypto/                   # 🔐 加密工具箱
│   └── src/main/
│       └── java/.../crypto/
│           ├── controller/                  #   REST API 接口
│           ├── manager/                     #   加解密业务编排
│           └── util/                        #   AES / ZIP 加解密工具类
│
├── output-toolkit-justopen/                 # 🚀 快捷启动器
│   └── src/main/
│       └── java/.../justopen/
│           ├── config/                      #   配置模型 & 加载器
│           ├── fx/                          #   JavaFX 窗口 UI
│           ├── hotkey/                      #   全局热键管理
│           ├── service/                     #   软件启动 & 网址打开
│           └── LauncherApplication.java     #   主入口
│
└── output-toolkit-mcp-nl2sql/               # 🗄️ MCP NL2SQL 服务
    └── src/main/
        ├── java/.../mcp/nl2sql/
        │   ├── config/                      #   数据库 / SSH / 传输配置
        │   ├── db/                          #   连接工厂 & 查询结果模型
        │   ├── mcp/                         #   MCP 工具定义 & 资源 & Prompts
        │   ├── security/                    #   SQL 注入防护
        │   ├── service/                     #   查询执行 & Schema 查询
        │   ├── transport/                   #   STDIO / SSE 传输模式
        │   └── tunnel/                      #   SSH 隧道管理——未实现
        └── resources/
            └── application.yml              #   数据库 & MCP 配置
```

---

## 📝 配置参数说明

### codegenerator 核心配置 (`generator-config.xml`)

| 参数 | 说明 | 默认值 |
|------|------|--------|
| `genSwitch` | 是否启用该表生成 | `true` |
| `model` | 输入源类型 (`sql` / `json`) | `sql` |
| `sql` | 建表/SELECT/INSERT SQL 语句 | — |
| `authorName` | 代码 `@author` 注解值 | — |
| `dtoSuffix` | DTO 类后缀 | `DTO` |
| `mapperSuffix` | Mapper 类后缀 | `Mapper` |
| `sqlIgnorePrefix` | 建表语句表名忽略前缀 | — |
| `dtoLocalPath` | DTO 输出路径 | — |

### mcp-nl2sql 环境变量

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `MYSQL_HOST` | 数据库地址 | `localhost` |
| `MYSQL_PORT` | 数据库端口 | `3306` |
| `MYSQL_USER` | 数据库用户 | `root` |
| `MYSQL_PASSWORD` | 数据库密码 | `root` |
| `MYSQL_DATABASE` | 数据库名（空=多库模式） | — |
| `MYSQL_INSERT_ENABLED` | 是否允许 INSERT | `true` |
| `MYSQL_UPDATE_ENABLED` | 是否允许 UPDATE | `true` |
| `MYSQL_DELETE_ENABLED` | 是否允许 DELETE | `true` |

---

## ❓ 常见问题 FAQ

<details>
<summary><strong>Q1: codegenerator 支持哪些数据库的建表语句？</strong></summary>

主要支持 **MySQL** 语法，对 PostgreSQL / Oracle 的 `COMMENT ON COLUMN` 风格注释也有部分兼容。如果遇到解析失败，建议使用标准 MySQL DDL 格式。
</details>

<details>
<summary><strong>Q2: 增量更新会覆盖我手写的 Controller 和 Manager 吗？</strong></summary>

**不会。** 增量更新策略是刻意设计过的：只更新 DTO / VO / Mapper.xml，**跳过** Controller 和 Manager。你手写的业务逻辑完全安全。
</details>

<details>
<summary><strong>Q3: mcp-nl2sql 如何防止 SQL 注入？</strong></summary>

对于 `get_schema_info` 和 `get_table_sample` 工具的表名参数，系统通过 `IdentifierValidator` 进行严格的白名单校验（只允许字母、数字、下划线），拒绝任何特殊字符。`execute_sql` 工具则依赖调用方（Claude）的 SQL 生成质量。
</details>

<details>
<summary><strong>Q4: justopen 必须安装 JavaFX 吗？</strong></summary>

JDK 17+ 的某些发行版（如 Oracle JDK）需额外引入 JavaFX。推荐使用 **Azul Zulu JDK**（内置 JavaFX）或通过 Maven 引入 `javafx-controls` 依赖。
</details>

<details>
<summary><strong>Q5: mcp-nl2sql 多库模式下如何切换数据库？</strong></summary>

使用 `database.table` 格式指定表名即可，如 `get_schema_info("mydb.users")`。底层 JDBC 连接不绑定特定数据库。
</details>

---

## 📌 更新日志

### v1.0.0 (2025-11-17)

- 🎉 初始发布
- 🧬 **代码生成器**：SQL/JSON 解析 + FreeMarker 模板（50+ 模板）代码生成
- 🔐 加密工具箱：ZIP AES-256 加解密 + AES 字符串加解密
- 🚀 快捷启动器：JavaFX 托盘应用 + JNativeHook 全局热键

### 后续更新

- ✨ **增量更新** —— 支持字段新增/删除后只更新 DTO/VO/Mapper.xml
- ✨ **justopen UI 简化** —— 重构菜单窗口，简化操作流程
- ✨ **mcp-nl2sql 模块** —— MCP 协议 MySQL 查询服务，支持 STDIO/SSE 双模式
- ✨ **SSH 隧道** —— mcp-nl2sql 支持通过 SSH 堡垒机连接数据库
- ✨ **DML 控制开关** —— INSERT/UPDATE/DELETE 可独立禁用
- ✨ **测试模板** —— codegenerator 新增测试代码生成模板

---

## 🤝 贡献指南

我们欢迎一切形式的贡献！无论是提交 Issue、改进文档、还是提交 PR。

### 开发环境搭建

```bash
# 1. Fork 并克隆仓库
git clone https://github.com/your-org/output-toolkit.git
cd output-toolkit

# 2. 编译验证
./mvnw clean compile

# 3. 运行测试
./mvnw test
```

### 提交规范

- **分支命名**: `feature/模块名-功能描述` 或 `fix/模块名-问题描述`
- **Commit Message**: 使用中文，格式 `模块名 简短描述`（如 `mcp-nl2sql 新增连接池配置`）
- **代码风格**: 遵循 [阿里巴巴 Java 开发手册](https://github.com/alibaba/p3c)
- **注释要求**: 类、公共方法必须有 JavaDoc 注释
- **PR 描述**: 说明改动内容、影响范围、测试情况

### 设计模式使用

本项目积极采用 GoF 设计模式，提交代码时：
- 如使用了设计模式，请在类注释中附上「使用原因 → 模式收益 → 角色类结构」三段说明
- 如不适合使用设计模式，请说明原因和替代优化方案

---

## 📄 开源协议

本项目采用 **MIT License** 开源协议。详见 [LICENSE](./LICENSE) 文件。

> 简而言之：你可以自由使用、修改、分发本项目代码，只需保留原始版权声明。

---

## ⭐ 支持项目

如果这个工具包帮你省了时间，不妨给个 **Star** ⭐ 鼓励一下！

- 🐛 **Bug 反馈**: [GitHub Issues](https://github.com/your-org/output-toolkit/issues)
- 💡 **功能建议**: [GitHub Discussions](https://github.com/your-org/output-toolkit/discussions)
- 📧 **联系作者**: zhouchuanxiang@example.com

---

<p align="center">
  <sub>Made with ❤️ by <a href="https://github.com/zhouchuanxiang">zhouchuanxiang</a> & contributors</sub>
</p>
