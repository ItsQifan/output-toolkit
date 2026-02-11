package com.zhouchuanxiang.outputtoolkit.justopen.fx;

import com.zhouchuanxiang.outputtoolkit.justopen.config.*;
import com.zhouchuanxiang.outputtoolkit.justopen.service.SoftwareLauncher;
import com.zhouchuanxiang.outputtoolkit.justopen.service.UrlOpener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * JavaFX 菜单窗口
 * 在屏幕中央显示快捷启动菜单
 */
public class LauncherMenuWindow {
    
    private static final Logger logger = LoggerFactory.getLogger(LauncherMenuWindow.class);
    
    private final Stage stage;
    private final SoftwareLauncher softwareLauncher;
    private final UrlOpener urlOpener;
    private final ConfigLoader configLoader;
    private final Runnable onReloadConfig;
    private final Runnable onExit;
    
    private LauncherConfig config;
    private VBox menuContainer;
    
    public LauncherMenuWindow(LauncherConfig config, ConfigLoader configLoader, 
                              Runnable onReloadConfig, Runnable onExit) {
        this.config = config;
        this.configLoader = configLoader;
        this.onReloadConfig = onReloadConfig;
        this.onExit = onExit;
        this.softwareLauncher = new SoftwareLauncher();
        this.urlOpener = new UrlOpener();
        
        // 设置浏览器路径
        if (config.getBrowser() != null && !config.getBrowser().isBlank()) {
            urlOpener.setBrowserPath(config.getBrowser());
        }
        
        // 创建窗口
        stage = new Stage();
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setAlwaysOnTop(true);
        stage.setResizable(false);
        
        // 创建菜单内容
        buildMenu();
        
        // 点击窗口外部时隐藏
        stage.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                hide();
            }
        });
    }
    
    /**
     * 构建菜单界面
     */
    private void buildMenu() {
        menuContainer = new VBox(5);
        menuContainer.setPadding(new Insets(10));
        menuContainer.setAlignment(Pos.TOP_LEFT);
        menuContainer.setStyle(
            "-fx-background-color: #ffffff;" +
            "-fx-border-color: #cccccc;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 5;" +
            "-fx-background-radius: 5;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 3);"
        );
        
        // 标题
        Label titleLabel = new Label("快捷启动器");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333333;");
        menuContainer.getChildren().add(titleLabel);
        
        // 分隔线
        menuContainer.getChildren().add(createSeparator());
        
        // 软件菜单
        addSoftwareMenu();
        
        // 网址菜单
        addUrlMenu();
        
        // 分隔线
        menuContainer.getChildren().add(createSeparator());
        
        // 功能按钮
        addFunctionButtons();
        
        Scene scene = new Scene(menuContainer);
        
        // ESC 键关闭窗口
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                hide();
            }
        });
        
        stage.setScene(scene);
    }
    
    /**
     * 添加软件菜单
     */
    private void addSoftwareMenu() {
        MenuButton softwareMenuBtn = new MenuButton("打开常用软件");
        softwareMenuBtn.setStyle(getMenuButtonStyle());
        softwareMenuBtn.setMaxWidth(Double.MAX_VALUE);
        
        List<SoftwareGroup> softwareGroups = config.getSoftwareGroups();
        if (softwareGroups != null && !softwareGroups.isEmpty()) {
            // 全部打开
            MenuItem openAll = new MenuItem("全部打开");
            openAll.setOnAction(e -> {
                hide();
                logger.info("打开所有软件");
                new Thread(() -> softwareLauncher.launchAll(softwareGroups)).start();
            });
            softwareMenuBtn.getItems().add(openAll);
            softwareMenuBtn.getItems().add(new SeparatorMenuItem());
            
            // 分组
            for (SoftwareGroup group : softwareGroups) {
                Menu groupMenu = new Menu(group.getName());
                
                // 打开分组全部
                MenuItem openGroup = new MenuItem("打开该分组全部");
                openGroup.setOnAction(e -> {
                    hide();
                    logger.info("打开软件分组: {}", group.getName());
                    new Thread(() -> softwareLauncher.launchGroup(group)).start();
                });
                groupMenu.getItems().add(openGroup);
                groupMenu.getItems().add(new SeparatorMenuItem());
                
                // 分组内的软件
                if (group.getItems() != null) {
                    for (SoftwareItem item : group.getItems()) {
                        MenuItem menuItem = new MenuItem(item.getName());
                        menuItem.setOnAction(e -> {
                            hide();
                            logger.info("启动软件: {}", item.getName());
                            new Thread(() -> softwareLauncher.launch(item)).start();
                        });
                        groupMenu.getItems().add(menuItem);
                    }
                }
                
                softwareMenuBtn.getItems().add(groupMenu);
            }
        } else {
            MenuItem noConfig = new MenuItem("(无配置)");
            noConfig.setDisable(true);
            softwareMenuBtn.getItems().add(noConfig);
        }
        
        menuContainer.getChildren().add(softwareMenuBtn);
    }
    
    /**
     * 添加网址菜单
     */
    private void addUrlMenu() {
        MenuButton urlMenuBtn = new MenuButton("打开常用网址");
        urlMenuBtn.setStyle(getMenuButtonStyle());
        urlMenuBtn.setMaxWidth(Double.MAX_VALUE);
        
        List<UrlGroup> urlGroups = config.getUrlGroups();
        if (urlGroups != null && !urlGroups.isEmpty()) {
            // 全部打开
            MenuItem openAll = new MenuItem("全部打开");
            openAll.setOnAction(e -> {
                hide();
                logger.info("打开所有网址");
                new Thread(() -> urlOpener.openAll(urlGroups)).start();
            });
            urlMenuBtn.getItems().add(openAll);
            urlMenuBtn.getItems().add(new SeparatorMenuItem());
            
            // 分组
            for (UrlGroup group : urlGroups) {
                Menu groupMenu = new Menu(group.getName());
                
                // 打开分组全部
                MenuItem openGroup = new MenuItem("打开该分组全部");
                openGroup.setOnAction(e -> {
                    hide();
                    logger.info("打开网址分组: {}", group.getName());
                    new Thread(() -> urlOpener.openGroup(group)).start();
                });
                groupMenu.getItems().add(openGroup);
                groupMenu.getItems().add(new SeparatorMenuItem());
                
                // 分组内的网址
                if (group.getItems() != null) {
                    for (UrlItem item : group.getItems()) {
                        MenuItem menuItem = new MenuItem(item.getName());
                        menuItem.setOnAction(e -> {
                            hide();
                            logger.info("打开网址: {}", item.getName());
                            new Thread(() -> urlOpener.open(item)).start();
                        });
                        groupMenu.getItems().add(menuItem);
                    }
                }
                
                urlMenuBtn.getItems().add(groupMenu);
            }
        } else {
            MenuItem noConfig = new MenuItem("(无配置)");
            noConfig.setDisable(true);
            urlMenuBtn.getItems().add(noConfig);
        }
        
        menuContainer.getChildren().add(urlMenuBtn);
    }
    
    /**
     * 添加功能按钮
     */
    private void addFunctionButtons() {
        // 编辑配置
        Button editConfigBtn = new Button("编辑配置");
        editConfigBtn.setStyle(getButtonStyle());
        editConfigBtn.setMaxWidth(Double.MAX_VALUE);
        editConfigBtn.setOnAction(e -> {
            hide();
            logger.info("打开配置文件编辑器");
            configLoader.openConfigInEditor();
        });
        menuContainer.getChildren().add(editConfigBtn);
        
        // 重新加载配置
        Button reloadConfigBtn = new Button("重新加载配置");
        reloadConfigBtn.setStyle(getButtonStyle());
        reloadConfigBtn.setMaxWidth(Double.MAX_VALUE);
        reloadConfigBtn.setOnAction(e -> {
            hide();
            logger.info("重新加载配置");
            onReloadConfig.run();
        });
        menuContainer.getChildren().add(reloadConfigBtn);
        
        // 分隔线
        menuContainer.getChildren().add(createSeparator());
        
        // 退出
        Button exitBtn = new Button("退出");
        exitBtn.setStyle(getButtonStyle() + "-fx-text-fill: #e74c3c;");
        exitBtn.setMaxWidth(Double.MAX_VALUE);
        exitBtn.setOnAction(e -> {
            logger.info("退出程序");
            onExit.run();
        });
        menuContainer.getChildren().add(exitBtn);
    }
    
    /**
     * 创建分隔线
     */
    private Separator createSeparator() {
        Separator separator = new Separator();
        separator.setPadding(new Insets(5, 0, 5, 0));
        return separator;
    }
    
    /**
     * 获取菜单按钮样式
     */
    private String getMenuButtonStyle() {
        return "-fx-background-color: #f8f9fa;" +
               "-fx-border-color: #dee2e6;" +
               "-fx-border-radius: 3;" +
               "-fx-background-radius: 3;" +
               "-fx-padding: 8 12;" +
               "-fx-font-size: 13px;" +
               "-fx-cursor: hand;";
    }
    
    /**
     * 获取按钮样式
     */
    private String getButtonStyle() {
        return "-fx-background-color: transparent;" +
               "-fx-border-color: transparent;" +
               "-fx-padding: 8 12;" +
               "-fx-font-size: 13px;" +
               "-fx-cursor: hand;" +
               "-fx-alignment: center-left;";
    }
    
    /**
     * 在屏幕中央显示窗口
     */
    public void showAtCenter() {
        // 获取主屏幕尺寸
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        
        // 先显示以获取窗口尺寸
        stage.show();
        
        // 计算中央位置
        double x = (screenBounds.getWidth() - stage.getWidth()) / 2 + screenBounds.getMinX();
        double y = (screenBounds.getHeight() - stage.getHeight()) / 2 + screenBounds.getMinY();
        
        stage.setX(x);
        stage.setY(y);
        
        // 请求焦点
        stage.requestFocus();
        
        logger.debug("Menu shown at center: ({}, {})", x, y);
    }
    
    /**
     * 隐藏窗口
     */
    public void hide() {
        stage.hide();
    }
    
    /**
     * 窗口是否显示中
     */
    public boolean isShowing() {
        return stage.isShowing();
    }
    
    /**
     * 更新配置
     */
    public void updateConfig(LauncherConfig newConfig) {
        this.config = newConfig;
        
        // 更新浏览器路径
        if (config.getBrowser() != null && !config.getBrowser().isBlank()) {
            urlOpener.setBrowserPath(config.getBrowser());
        } else {
            urlOpener.setBrowserPath(null);
        }
        
        // 重建菜单
        menuContainer.getChildren().clear();
        buildMenu();
    }
}
