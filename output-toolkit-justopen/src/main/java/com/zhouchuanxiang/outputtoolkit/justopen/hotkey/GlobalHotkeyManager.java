package com.zhouchuanxiang.outputtoolkit.justopen.hotkey;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

/**
 * 全局快捷键管理器
 * 使用 JNativeHook 实现全局键盘监听
 */
public class GlobalHotkeyManager implements NativeKeyListener {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalHotkeyManager.class);
    
    /**
     * 当前按下的按键集合
     */
    private final Set<Integer> pressedKeys = new HashSet<>();
    
    /**
     * 配置的快捷键（如 "CTRL+1"）
     */
    private String hotkeyConfig;
    
    /**
     * 快捷键触发时的回调
     */
    private Runnable hotkeyCallback;
    
    /**
     * 是否已注册
     */
    private boolean registered = false;
    
    public GlobalHotkeyManager() {
        // 禁用 JNativeHook 的日志输出
        java.util.logging.Logger jnhLogger = java.util.logging.Logger.getLogger(GlobalScreen.class.getPackage().getName());
        jnhLogger.setLevel(Level.OFF);
        jnhLogger.setUseParentHandlers(false);
    }
    
    /**
     * 设置快捷键配置
     *
     * @param hotkeyConfig 快捷键配置字符串，如 "CTRL+1"
     */
    public void setHotkeyConfig(String hotkeyConfig) {
        this.hotkeyConfig = hotkeyConfig;
        logger.info("设置全局快捷键: {}", hotkeyConfig);
    }
    
    /**
     * 设置快捷键触发时的回调
     */
    public void setHotkeyCallback(Runnable callback) {
        this.hotkeyCallback = callback;
    }
    
    /**
     * 注册全局快捷键监听
     */
    public void register() {
        if (registered) {
            logger.warn("全局快捷键已注册，跳过重复注册");
            return;
        }
        
        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(this);
            registered = true;
            logger.info("全局快捷键注册成功");
        } catch (NativeHookException e) {
            logger.error("注册全局快捷键失败: {}", e.getMessage());
        }
    }
    
    /**
     * 注销全局快捷键监听
     */
    public void unregister() {
        if (!registered) {
            return;
        }
        
        try {
            GlobalScreen.removeNativeKeyListener(this);
            GlobalScreen.unregisterNativeHook();
            registered = false;
            logger.info("全局快捷键注销成功");
        } catch (NativeHookException e) {
            logger.error("注销全局快捷键失败: {}", e.getMessage());
        }
    }
    
    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        pressedKeys.add(e.getKeyCode());
        checkHotkey();
    }
    
    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        pressedKeys.remove(e.getKeyCode());
    }
    
    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {
        // 不处理
    }
    
    /**
     * 检查当前按下的按键是否匹配配置的快捷键
     */
    private void checkHotkey() {
        if (hotkeyConfig == null || hotkeyCallback == null) {
            return;
        }
        
        if (matchHotkey()) {
            logger.debug("快捷键触发: {}", hotkeyConfig);
            // 在新线程中执行回调，避免阻塞键盘监听
            new Thread(() -> {
                try {
                    hotkeyCallback.run();
                } catch (Exception e) {
                    logger.error("执行快捷键回调失败: {}", e.getMessage());
                }
            }).start();
        }
    }
    
    /**
     * 检查当前按键是否匹配配置的快捷键
     */
    private boolean matchHotkey() {
        String[] parts = hotkeyConfig.toUpperCase().split("\\+");
        
        boolean needCtrl = false;
        boolean needAlt = false;
        boolean needShift = false;
        int mainKeyCode = -1;
        
        for (String part : parts) {
            part = part.trim();
            switch (part) {
                case "CTRL":
                case "CONTROL":
                    needCtrl = true;
                    break;
                case "ALT":
                    needAlt = true;
                    break;
                case "SHIFT":
                    needShift = true;
                    break;
                default:
                    mainKeyCode = getKeyCode(part);
                    break;
            }
        }
        
        // 检查修饰键
        boolean ctrlPressed = pressedKeys.contains(NativeKeyEvent.VC_CONTROL) 
                || pressedKeys.contains(NativeKeyEvent.VC_CONTROL);
        boolean altPressed = pressedKeys.contains(NativeKeyEvent.VC_ALT);
        boolean shiftPressed = pressedKeys.contains(NativeKeyEvent.VC_SHIFT);
        
        if (needCtrl != ctrlPressed) return false;
        if (needAlt != altPressed) return false;
        if (needShift != shiftPressed) return false;
        
        // 检查主键
        return mainKeyCode != -1 && pressedKeys.contains(mainKeyCode);
    }
    
    /**
     * 将按键名称转换为 NativeKeyEvent 的键码
     */
    private int getKeyCode(String keyName) {
        return switch (keyName) {
            case "0" -> NativeKeyEvent.VC_0;
            case "1" -> NativeKeyEvent.VC_1;
            case "2" -> NativeKeyEvent.VC_2;
            case "3" -> NativeKeyEvent.VC_3;
            case "4" -> NativeKeyEvent.VC_4;
            case "5" -> NativeKeyEvent.VC_5;
            case "6" -> NativeKeyEvent.VC_6;
            case "7" -> NativeKeyEvent.VC_7;
            case "8" -> NativeKeyEvent.VC_8;
            case "9" -> NativeKeyEvent.VC_9;
            case "A" -> NativeKeyEvent.VC_A;
            case "B" -> NativeKeyEvent.VC_B;
            case "C" -> NativeKeyEvent.VC_C;
            case "D" -> NativeKeyEvent.VC_D;
            case "E" -> NativeKeyEvent.VC_E;
            case "F" -> NativeKeyEvent.VC_F;
            case "G" -> NativeKeyEvent.VC_G;
            case "H" -> NativeKeyEvent.VC_H;
            case "I" -> NativeKeyEvent.VC_I;
            case "J" -> NativeKeyEvent.VC_J;
            case "K" -> NativeKeyEvent.VC_K;
            case "L" -> NativeKeyEvent.VC_L;
            case "M" -> NativeKeyEvent.VC_M;
            case "N" -> NativeKeyEvent.VC_N;
            case "O" -> NativeKeyEvent.VC_O;
            case "P" -> NativeKeyEvent.VC_P;
            case "Q" -> NativeKeyEvent.VC_Q;
            case "R" -> NativeKeyEvent.VC_R;
            case "S" -> NativeKeyEvent.VC_S;
            case "T" -> NativeKeyEvent.VC_T;
            case "U" -> NativeKeyEvent.VC_U;
            case "V" -> NativeKeyEvent.VC_V;
            case "W" -> NativeKeyEvent.VC_W;
            case "X" -> NativeKeyEvent.VC_X;
            case "Y" -> NativeKeyEvent.VC_Y;
            case "Z" -> NativeKeyEvent.VC_Z;
            case "F1" -> NativeKeyEvent.VC_F1;
            case "F2" -> NativeKeyEvent.VC_F2;
            case "F3" -> NativeKeyEvent.VC_F3;
            case "F4" -> NativeKeyEvent.VC_F4;
            case "F5" -> NativeKeyEvent.VC_F5;
            case "F6" -> NativeKeyEvent.VC_F6;
            case "F7" -> NativeKeyEvent.VC_F7;
            case "F8" -> NativeKeyEvent.VC_F8;
            case "F9" -> NativeKeyEvent.VC_F9;
            case "F10" -> NativeKeyEvent.VC_F10;
            case "F11" -> NativeKeyEvent.VC_F11;
            case "F12" -> NativeKeyEvent.VC_F12;
            case "SPACE" -> NativeKeyEvent.VC_SPACE;
            case "ENTER" -> NativeKeyEvent.VC_ENTER;
            case "TAB" -> NativeKeyEvent.VC_TAB;
            case "ESCAPE", "ESC" -> NativeKeyEvent.VC_ESCAPE;
            case "BACKSPACE" -> NativeKeyEvent.VC_BACKSPACE;
            case "DELETE" -> NativeKeyEvent.VC_DELETE;
            case "INSERT" -> NativeKeyEvent.VC_INSERT;
            case "HOME" -> NativeKeyEvent.VC_HOME;
            case "END" -> NativeKeyEvent.VC_END;
            case "PAGEUP" -> NativeKeyEvent.VC_PAGE_UP;
            case "PAGEDOWN" -> NativeKeyEvent.VC_PAGE_DOWN;
            case "UP" -> NativeKeyEvent.VC_UP;
            case "DOWN" -> NativeKeyEvent.VC_DOWN;
            case "LEFT" -> NativeKeyEvent.VC_LEFT;
            case "RIGHT" -> NativeKeyEvent.VC_RIGHT;
            default -> -1;
        };
    }
}
