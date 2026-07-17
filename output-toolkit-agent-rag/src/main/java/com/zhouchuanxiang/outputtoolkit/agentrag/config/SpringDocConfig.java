package com.zhouchuanxiang.outputtoolkit.agentrag.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc OpenAPI（Swagger）配置
 * <p>
 * 生成在线 API 文档，可通过 http://localhost:8080/swagger-ui.html 访问，
 * 方便个人展示时直接在线调试所有接口。
 * </p>
 *
 * @author qifan
 * @since 2026-07-13
 */
@Configuration
public class SpringDocConfig {

    /**
     * 自定义 OpenAPI 文档信息
     *
     * @return OpenAPI 实例
     */
    @Bean
    public OpenAPI agentRagOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Agent RAG Demo API")
                        .description("""
                                单Agent + RAG + 工具调用演示项目

                                核心功能：
                                - 文档上传与向量化存储（Milvus）
                                - RAG 检索增强问答
                                - Agent 工具调用（时间查询 + 订单查询）
                                - SSE 流式响应 + 推理链可视化
                                - 多轮对话会话管理
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("qifan")
                                .email("qifan@example.com")));
    }
}
