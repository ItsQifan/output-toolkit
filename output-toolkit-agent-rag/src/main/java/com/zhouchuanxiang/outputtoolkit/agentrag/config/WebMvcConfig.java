package com.zhouchuanxiang.outputtoolkit.agentrag.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 * <p>
 * 配置跨域访问和静态资源映射，确保前端聊天页面能正常访问后端 API。
 * </p>
 *
 * @author qifan
 * @since 2026-07-13
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 配置 CORS 跨域访问
     * <p>
     * 允许前端页面（任意来源）访问 API，开发调试阶段开放所有来源。
     * 生产环境应限制为具体域名。
     * </p>
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    /**
     * 配置静态资源路径
     * <p>
     * 将 classpath:/static/ 映射为根路径，
     * 浏览器访问 http://localhost:8080/ 即打开 chat.html。
     * </p>
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }
}
