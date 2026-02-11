---
isProject: false
name: 启动器托盘工具模块
overview: 在 output-toolkit 项目中新增 output-toolkit-justopen
  模块，实现系统托盘快捷启动器，支持一键打开常用软件和网址，配置通过JSON文件管理。
todos:
- content: 创建 output-toolkit-justopen 模块目录结构和 pom.xml
  id: create-module
  status: pending
- content: 创建配置实体类：LauncherConfig、SoftwareGroup、UrlGroup 等
  id: config-entity
  status: pending
- content: 实现 ConfigLoader 配置加载器
  id: config-loader
  status: pending
- content: 实现 SoftwareLauncher 和 UrlOpener 服务类
  id: launcher-service
  status: pending
- content: 实现 SystemTrayManager 托盘管理器（支持托盘点击 +
    全局快捷键）
  id: tray-manager
  status: pending
- content: 创建 LauncherApplication 主启动类
  id: main-app
  status: pending
- content: 添加默认配置文件和托盘图标
  id: resources
  status: pending
- content: 更新父 pom.xml 添加新模块
  id: update-parent
  status: pending
---

# 系统托盘启动器模块实现计划

## 模块概述

新建 `output-toolkit-justopen`
模块，作为一个轻量级系统托盘工具，启动后驻留在系统托盘。

支持两种方式唤醒主界面：

1.  鼠标右键点击托盘图标唤醒界面\
2.  全局自定义快捷键唤醒界面（默认：`Ctrl + 1`）

快捷键支持后续扩展为可配置项。

------------------------------------------------------------------------

## 核心功能

-   系统托盘图标常驻运行
-   右键菜单显示：打开常用软件、打开常用网址、编辑配置、退出
-   支持软件分组（如：开发工具、办公软件等）
-   支持网址分组（如：工作网站、常用工具等）
-   JSON配置文件存储软件路径和网址
-   支持全局快捷键唤醒界面（默认 Ctrl+1，可扩展为可配置）

------------------------------------------------------------------------

## 技术方案

### 托盘功能

-   `java.awt.SystemTray` - 系统托盘支持
-   `java.awt.TrayIcon` - 托盘图标
-   `java.awt.PopupMenu` - 右键弹出菜单
-   `java.awt.Desktop` - 打开网址和文件
-   `ProcessBuilder` - 启动外部程序

### 全局快捷键实现方案

可选方案：

1.  **JNativeHook（推荐）**
    -   支持全局键盘监听
    -   可注册 Ctrl+1 组合键
    -   跨平台支持较好
2.  JavaFX + 自定义键盘监听（仅限应用激活时）

建议使用 **JNativeHook** 实现真正的全局快捷键。

默认快捷键：

    Ctrl + 1

后续可扩展为：

``` json
{
  "hotkey": "CTRL+1"
}
```

------------------------------------------------------------------------

## 项目结构

    output-toolkit-justopen/
    ├── pom.xml
    └── src/main/
        ├── java/com/zhouchuanxiang/outputtoolkit/justopen/
        │   ├── LauncherApplication.java      # 主启动类
        │   ├── config/
        │   │   ├── LauncherConfig.java       # 配置实体类
        │   │   ├── SoftwareGroup.java        # 软件分组
        │   │   ├── UrlGroup.java             # 网址分组
        │   │   └── ConfigLoader.java         # 配置加载器
        │   ├── service/
        │   │   ├── SoftwareLauncher.java     # 软件启动服务
        │   │   └── UrlOpener.java            # 网址打开服务
        │   ├── tray/
        │   │   └── SystemTrayManager.java    # 托盘管理器
        │   └── hotkey/
        │       └── GlobalHotkeyManager.java  # 全局快捷键管理器
        └── resources/
            ├── launcher-config.json          # 默认配置模板
            └── icon.png                       # 托盘图标

------------------------------------------------------------------------

## JSON配置文件格式

``` json
{
  "browser": "C:/Program Files/Google/Chrome/Application/chrome.exe",
  "hotkey": "CTRL+1",
  "softwareGroups": [
    {
      "name": "开发工具",
      "items": [
        { "name": "IDEA", "path": "C:/Program Files/JetBrains/IntelliJ IDEA/bin/idea64.exe" },
        { "name": "VS Code", "path": "C:/Users/xxx/AppData/Local/Programs/Microsoft VS Code/Code.exe" }
      ]
    }
  ],
  "urlGroups": [
    {
      "name": "工作网站",
      "items": [
        { "name": "GitHub", "url": "https://github.com" },
        { "name": "GitLab", "url": "https://gitlab.com" }
      ]
    }
  ]
}
```

------------------------------------------------------------------------

## 唤醒逻辑设计

### 方式一：托盘点击

-   监听 TrayIcon 鼠标事件
-   左键或右键菜单点击"打开主界面"时展示 JavaFX Stage

### 方式二：全局快捷键

-   注册 Ctrl+1 组合键
-   监听到按键后：
    -   如果窗口隐藏 → 显示
    -   如果窗口显示 → 置顶

------------------------------------------------------------------------

## 需要修改的现有文件

### 父 pom.xml

``` xml
<module>output-toolkit-justopen</module>
```

------------------------------------------------------------------------

## 新建文件清单

  文件                                说明
  ----------------------------------- ----------------
  `output-toolkit-justopen/pom.xml`   模块POM
  `LauncherApplication.java`          主启动类
  `LauncherConfig.java`               配置根实体
  `SoftwareGroup.java`                软件分组实体
  `SoftwareItem.java`                 软件项实体
  `UrlGroup.java`                     网址分组实体
  `UrlItem.java`                      网址项实体
  `ConfigLoader.java`                 配置加载服务
  `SoftwareLauncher.java`             软件启动服务
  `UrlOpener.java`                    网址打开服务
  `SystemTrayManager.java`            托盘管理核心类
  `GlobalHotkeyManager.java`          全局快捷键管理
  `launcher-config.json`              配置文件模板
  `icon.png`                          托盘图标

------------------------------------------------------------------------

## 扩展规划

-   快捷键可视化配置界面
-   多快捷键支持
-   开机自启动
-   Windows / Mac / Linux 兼容优化
