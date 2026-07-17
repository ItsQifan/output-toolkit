package com.zhouchuanxiang.outputtoolkit.agentrag.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 页面路由控制器
 * <p>
 * 将根路径 "/" 映射到 chat.html 聊天页面。
 * </p>
 *
 * @author qifan
 * @since 2026-07-13
 */
@Controller
public class IndexController {

    /**
     * 聊天页面入口
     *
     * @return chat.html 页面
     */
    @GetMapping("/")
    public String index() {
        return "forward:/chat.html";
    }
}
