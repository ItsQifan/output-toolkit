package com.zhouchuanxiang.outputtoolkit.agentrag.tool;

import com.zhouchuanxiang.outputtoolkit.agentrag.dto.ChatEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

/**
 * 时间查询工具（Spring AI 原生 Tool Calling）
 * <p>
 * 通过 {@code @Tool} 注解声明为 LLM 可调用的工具，Spring AI 自动生成
 * JSON Schema 并通过 API 的 tools 参数传给 LLM（原生 Function Calling），
 * 无需手写 Prompt 描述和 JSON 解析。
 * </p>
 * <p>
 * 使用原因：LLM 自身不知道"现在"是什么时间——它的训练数据有截止日期。
 * 通过此工具，Agent 可以获取真实的当前时间，回答时效性问题。
 * </p>
 *
 * @author qifan
 * @since 2026-07-13
 */
//tips: 原生 Function Calling vs Prompt 驱动的区别：
//     Prompt 驱动是"在 System Prompt 里求 LLM 返回 JSON"，LLM 可能不听话（格式错乱）；
//     原生方式是 API 协议层面支持——请求带 tools 参数，LLM 返回结构化的 tool_calls 字段，
//     由 Spring AI 框架自动解析、执行工具、把结果回传给 LLM，直到产出最终答案。
//     开发者只需在方法上加 @Tool 注解，可靠性和开发效率都大幅提升。
@Slf4j
@Component
public class DateTimeTool {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss (EEEE)");

    /**
     * 获取当前日期和时间
     *
     * @param timezone    时区（可选，默认 Asia/Shanghai）
     * @param toolContext Spring AI 工具上下文（框架注入，不暴露给 LLM），用于推送 SSE 工具调用事件
     * @return 格式化的当前时间描述
     */
    @Tool(name = "get_current_time",
            description = "获取当前日期和时间。当用户问题涉及时间（如'今天'、'最近一周'、'现在几点'）时调用此工具。")
    public String getCurrentTime(
            @ToolParam(description = "时区，如 Asia/Shanghai，默认系统时区", required = false) String timezone,
            ToolContext toolContext) {
        // 推送工具调用事件到前端推理链
        ToolEventSupport.notifyToolCall(toolContext, "get_current_time");

        String zone = timezone != null && !timezone.isBlank() ? timezone : "Asia/Shanghai";
        LocalDateTime now;
        try {
            now = LocalDateTime.now(ZoneId.of(zone));
        } catch (Exception e) {
            // 时区无效时降级使用系统默认时区
            log.warn("时间工具_无效时区已降级, timezone={}", zone);
            now = LocalDateTime.now();
        }

        String formattedTime = now.format(FORMATTER);
        log.info("时间工具_查询时间, timezone={}, result={}", zone, formattedTime);
        return "当前时间：" + formattedTime + "（时区：" + zone + "）";
    }

    /**
     * 工具事件推送辅助类
     * <p>
     * 从 ToolContext 中取出 SSE 事件消费者，推送工具调用事件。
     * 抽取为独立类供所有工具复用，避免每个工具重复编写取值判空逻辑。
     * </p>
     */
    public static final class ToolEventSupport {

        /** ToolContext 中 SSE 事件消费者的 Key */
        public static final String EVENT_CONSUMER_KEY = "eventConsumer";

        private ToolEventSupport() {
        }

        /**
         * 推送工具调用事件（上下文中无消费者时静默跳过，兼容非 SSE 调用场景）
         *
         * @param toolContext 工具上下文
         * @param toolName    工具名称
         */
        @SuppressWarnings("unchecked")
        public static void notifyToolCall(ToolContext toolContext, String toolName) {
            if (toolContext == null || toolContext.getContext() == null) {
                return;
            }
            Object consumer = toolContext.getContext().get(EVENT_CONSUMER_KEY);
            if (consumer instanceof Consumer<?>) {
                ((Consumer<ChatEvent>) consumer).accept(ChatEvent.toolCall(toolName, "running"));
            }
        }
    }
}
