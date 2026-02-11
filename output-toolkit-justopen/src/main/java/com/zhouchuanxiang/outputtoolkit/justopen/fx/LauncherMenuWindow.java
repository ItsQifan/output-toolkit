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
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.input.MouseButton;
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
        menuContainer = new VBox(15);
        menuContainer.setPadding(new Insets(20));
        menuContainer.setAlignment(Pos.TOP_CENTER);
        menuContainer.setStyle(
            "-fx-background-color: #ffffff;" +
            "-fx-border-color: #cccccc;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 5;" +
            "-fx-background-radius: 5;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 3);"
        );
        
        // 标题
        Label titleLabel = new Label("Just-Open");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333333;");
        menuContainer.getChildren().add(titleLabel);
        
        // 软件卡片网格
        List<SoftwareGroup> softwareGroups = config.getSoftwareGroups();
        if (softwareGroups != null && !softwareGroups.isEmpty()) {
            GridPane softwareGrid = createCardsGrid();
            int col = 0, row = 0;
            for (SoftwareGroup group : softwareGroups) {
                VBox card = createSoftwareGroupCard(group);
                softwareGrid.add(card, col, row);
                col++;
                if (col >= 3) { // 3列布局
                    col = 0;
                    row++;
                }
            }
            menuContainer.getChildren().add(softwareGrid);
        }
        
        // 网址卡片网格
        List<UrlGroup> urlGroups = config.getUrlGroups();
        if (urlGroups != null && !urlGroups.isEmpty()) {
            GridPane urlGrid = createCardsGrid();
            int col = 0, row = 0;
            for (UrlGroup group : urlGroups) {
                VBox card = createUrlGroupCard(group);
                urlGrid.add(card, col, row);
                col++;
                if (col >= 3) { // 3列布局
                    col = 0;
                    row++;
                }
            }
            menuContainer.getChildren().add(urlGrid);
        }
        
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
     * 创建卡片网格容器
     */
    private GridPane createCardsGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);
        return grid;
    }
    
    
    /**
     * 添加功能按钮
     */
    private void addFunctionButtons() {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        
        // 编辑配置
        Button editConfigBtn = new Button("编辑配置");
        editConfigBtn.setStyle(getFunctionButtonStyle());
        editConfigBtn.setOnAction(e -> {
            hide();
            logger.info("打开配置文件编辑器");
            configLoader.openConfigInEditor();
        });
        
        // 重新加载配置
        Button reloadConfigBtn = new Button("重新加载");
        reloadConfigBtn.setStyle(getFunctionButtonStyle());
        reloadConfigBtn.setOnAction(e -> {
            hide();
            logger.info("重新加载配置");
            onReloadConfig.run();
        });
        
        // 退出
        Button exitBtn = new Button("退出");
        exitBtn.setStyle(getFunctionButtonStyle() + "-fx-text-fill: #e74c3c;");
        exitBtn.setOnAction(e -> {
            logger.info("退出程序");
            onExit.run();
        });
        
        buttonBox.getChildren().addAll(editConfigBtn, reloadConfigBtn, exitBtn);
        menuContainer.getChildren().add(buttonBox);
    }
    
    /**
     * 获取功能按钮样式
     */
    private String getFunctionButtonStyle() {
        return "-fx-background-color: #f8f9fa;" +
               "-fx-border-color: #dee2e6;" +
               "-fx-border-radius: 5;" +
               "-fx-background-radius: 5;" +
               "-fx-padding: 8 16;" +
               "-fx-font-size: 13px;" +
               "-fx-cursor: hand;";
    }
    
    /**
     * 创建软件分组卡片
     */
    private VBox createSoftwareGroupCard(SoftwareGroup group) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.TOP_LEFT);
        card.setPrefWidth(180);
        card.setMinHeight(120);
        card.setStyle(getCardStyle());
        
        // 分组名称
        Label groupName = new Label(group.getName());
        groupName.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333333;");
        card.getChildren().add(groupName);
        
        // 程序列表
        if (group.getItems() != null && !group.getItems().isEmpty()) {
            VBox itemsList = new VBox(3);
            int count = 0;
            for (SoftwareItem item : group.getItems()) {
                if (count >= 5) { // 最多显示5个
                    Label moreLabel = new Label("...");
                    moreLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #999999;");
                    itemsList.getChildren().add(moreLabel);
                    break;
                }
                Label itemLabel = new Label("• " + item.getName());
                itemLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");
                itemsList.getChildren().add(itemLabel);
                count++;
            }
            card.getChildren().add(itemsList);
        }
        
        // 鼠标悬停效果
        card.setOnMouseEntered(e -> card.setStyle(getCardHoverStyle()));
        card.setOnMouseExited(e -> card.setStyle(getCardStyle()));
        
        // 左键点击：打开分组全部
        card.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                hide();
                logger.info("打开软件分组: {}", group.getName());
                new Thread(() -> softwareLauncher.launchGroup(group)).start();
            } else if (e.getButton() == MouseButton.SECONDARY) {
                // 右键：显示上下文菜单
                showSoftwareContextMenu(card, group, e.getScreenX(), e.getScreenY());
            }
        });
        
        return card;
    }
    
    /**
     * 创建网址分组卡片
     */
    private VBox createUrlGroupCard(UrlGroup group) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.TOP_LEFT);
        card.setPrefWidth(180);
        card.setMinHeight(120);
        card.setStyle(getCardStyle());
        
        // 分组名称
        Label groupName = new Label(group.getName());
        groupName.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333333;");
        card.getChildren().add(groupName);
        
        // 网址列表
        if (group.getItems() != null && !group.getItems().isEmpty()) {
            VBox itemsList = new VBox(3);
            int count = 0;
            for (UrlItem item : group.getItems()) {
                if (count >= 5) { // 最多显示5个
                    Label moreLabel = new Label("...");
                    moreLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #999999;");
                    itemsList.getChildren().add(moreLabel);
                    break;
                }
                Label itemLabel = new Label("• " + item.getName());
                itemLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");
                itemsList.getChildren().add(itemLabel);
                count++;
            }
            card.getChildren().add(itemsList);
        }
        
        // 鼠标悬停效果
        card.setOnMouseEntered(e -> card.setStyle(getCardHoverStyle()));
        card.setOnMouseExited(e -> card.setStyle(getCardStyle()));
        
        // 左键点击：打开分组全部
        card.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                hide();
                logger.info("打开网址分组: {}", group.getName());
                new Thread(() -> urlOpener.openGroup(group)).start();
            } else if (e.getButton() == MouseButton.SECONDARY) {
                // 右键：显示上下文菜单
                showUrlContextMenu(card, group, e.getScreenX(), e.getScreenY());
            }
        });
        
        return card;
    }
    
    /**
     * 显示软件分组的右键菜单
     */
    private void showSoftwareContextMenu(VBox card, SoftwareGroup group, double x, double y) {
        ContextMenu contextMenu = new ContextMenu();
        
        if (group.getItems() != null) {
            for (SoftwareItem item : group.getItems()) {
                MenuItem menuItem = new MenuItem(item.getName());
                menuItem.setOnAction(e -> {
                    hide();
                    logger.info("启动软件: {}", item.getName());
                    new Thread(() -> softwareLauncher.launch(item)).start();
                });
                contextMenu.getItems().add(menuItem);
            }
        }
        
        contextMenu.show(card, x, y);
    }
    
    /**
     * 显示网址分组的右键菜单
     */
    private void showUrlContextMenu(VBox card, UrlGroup group, double x, double y) {
        ContextMenu contextMenu = new ContextMenu();
        
        if (group.getItems() != null) {
            for (UrlItem item : group.getItems()) {
                MenuItem menuItem = new MenuItem(item.getName());
                menuItem.setOnAction(e -> {
                    hide();
                    logger.info("打开网址: {}", item.getName());
                    new Thread(() -> urlOpener.open(item)).start();
                });
                contextMenu.getItems().add(menuItem);
            }
        }
        
        contextMenu.show(card, x, y);
    }
    
    /**
     * 获取卡片默认样式
     */
    private String getCardStyle() {
        return "-fx-background-color: #f8f9fa;" +
               "-fx-border-color: #dee2e6;" +
               "-fx-border-radius: 8;" +
               "-fx-background-radius: 8;" +
               "-fx-cursor: hand;" +
               "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);";
    }
    
    /**
     * 获取卡片悬停样式
     */
    private String getCardHoverStyle() {
        return "-fx-background-color: #e9ecef;" +
               "-fx-border-color: #adb5bd;" +
               "-fx-border-radius: 8;" +
               "-fx-background-radius: 8;" +
               "-fx-cursor: hand;" +
               "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0, 0, 3);";
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
