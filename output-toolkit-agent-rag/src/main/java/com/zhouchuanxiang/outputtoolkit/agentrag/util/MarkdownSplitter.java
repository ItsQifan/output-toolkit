package com.zhouchuanxiang.outputtoolkit.agentrag.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Markdown / TXT 文本智能分块器
 * <p>
 * 将长文本按语义边界切分为小块，用于后续的向量化和检索。
 * 分块质量直接影响 RAG 检索效果——块太大则检索不精确，太小则丢失上下文。
 * </p>
 * <p>
 * 分块策略：
 * <ol>
 *   <li>优先按 Markdown 标题（##、###）切分，保持语义完整性</li>
 *   <li>如果某段仍超长，按段落（双换行）继续切</li>
 *   <li>如果段落还超长，按句子边界（中文句号、英文句点）切分</li>
 *   <li>块之间保留 overlap（重叠区），确保跨块信息不丢失</li>
 *   <li>代码块（```围栏）内部不切割，保持代码完整性</li>
 * </ol>
 * </p>
 *
 * @author qifan
 * @since 2026-07-13
 */
//tips: 文本分块（Chunking）是 RAG 最关键的第一步。为什么不能把整本书直接向量化？
//     因为 Embedding 模型有长度限制（通常 512-8192 token），超长的文本会被截断。
//     而且即使能塞进去，一整本书的向量太"模糊"了——它包含了太多主题，
//     检索时很难精确匹配到用户问题的相关内容。
//     所以需要把长文本切成小块，每块聚焦一个主题，就像把一本书拆成一个个段落来索引。
public class MarkdownSplitter {

    /** 默认块大小（字符数） */
    private static final int DEFAULT_CHUNK_SIZE = 800;

    /** 默认块间重叠（字符数），确保跨块信息不丢失 */
    private static final int DEFAULT_OVERLAP = 150;

    /** 代码围栏标记 */
    private static final String CODE_FENCE = "```";

    private final int chunkSize;
    private final int overlap;

    /**
     * 使用默认参数创建分块器
     */
    public MarkdownSplitter() {
        this(DEFAULT_CHUNK_SIZE, DEFAULT_OVERLAP);
    }

    /**
     * 使用自定义参数创建分块器
     *
     * @param chunkSize 块大小（字符数）
     * @param overlap   块间重叠（字符数）
     */
    public MarkdownSplitter(int chunkSize, int overlap) {
        this.chunkSize = chunkSize;
        this.overlap = overlap;
    }

    /**
     * 将文本切分为块
     *
     * @param text 原始文本
     * @return 文本块列表
     */
    public List<String> split(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        // 第一步：按 Markdown 标题切分粗粒度段落
        List<String> sections = splitByMarkdownHeadings(text);

        // 第二步：对每个段落做细粒度切分
        List<String> chunks = new ArrayList<>();
        for (String section : sections) {
            if (section.length() <= chunkSize) {
                chunks.add(section.strip());
            } else {
                // 段落超长，继续按段落和句子切分
                chunks.addAll(splitLongSection(section));
            }
        }

        return chunks;
    }

    /**
     * 按 Markdown 标题（# ## ###）切分粗粒度段落
     * <p>
     * 用正则匹配行首的 # 标记，在不破坏代码块的前提下切分。
     * </p>
     *
     * @param text 原始文本
     * @return 段落列表
     */
    private List<String> splitByMarkdownHeadings(String text) {
        List<String> sections = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inCodeBlock = false;

        for (String line : text.split("\n", -1)) {
            // 检测代码围栏边界
            if (line.trim().startsWith(CODE_FENCE)) {
                inCodeBlock = !inCodeBlock;
                current.append(line).append("\n");
                continue;
            }

            // 不在代码块内，且遇到 Markdown 标题 → 切分
            //tips: 为什么不在代码块内切分？代码块里的 # 是注释，不是标题！
            //     如果在代码块内按 ## 切分，会把一段完整的代码拆得乱七八糟。
            if (!inCodeBlock && line.matches("^#{1,4}\\s.*")) {
                // 保存当前段落
                if (!current.isEmpty()) {
                    sections.add(current.toString());
                    current = new StringBuilder();
                }
            }
            current.append(line).append("\n");
        }

        // 保存最后一个段落
        if (!current.isEmpty()) {
            sections.add(current.toString());
        }

        return sections;
    }

    /**
     * 将超长段落切分为更小的块
     * <p>
     * 优先按双换行（段落）切分，再按单换行切分，最后按句子切分。
     * 使用滑动窗口 + overlap 确保语义连贯性。
     * </p>
     *
     * @param section 超长段落
     * @return 切分后的块列表
     */
    private List<String> splitLongSection(String section) {
        List<String> chunks = new ArrayList<>();
        int start = 0;

        while (start < section.length()) {
            int end = Math.min(start + chunkSize, section.length());

            // 如果没到文本末尾，尝试在语义边界处切割
            if (end < section.length()) {
                end = findBestSplitPoint(section, start, end);
            }

            String chunk = section.substring(start, end).strip();
            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }

            // 下一个块的起始位置，减去 overlap 确保上下文重叠
            start = end - overlap;
            if (start >= section.length()) {
                break;
            }
        }

        return chunks;
    }

    /**
     * 在指定范围内找到最佳切割点
     * <p>
     * 优先级：双换行（段落边界）> 中文句号 > 英文句点 > 换行 > 逗号 > 空格
     * 在 [start, end] 区间内从后往前搜索最佳切割点。
     * </p>
     *
     * @param text  文本
     * @param start 搜索起始位置
     * @param end   默认切割位置
     * @return 最佳切割位置
     */
    private int findBestSplitPoint(String text, int start, int end) {
        // 在 end 往前 200 个字符范围内搜索
        int searchStart = Math.max(start + chunkSize / 2, end - 200);

        // 优先级1：双换行（段落边界）
        for (int i = end; i >= searchStart; i--) {
            if (i + 1 < text.length() && text.charAt(i) == '\n' && text.charAt(i + 1) == '\n') {
                return i + 2; // 切割点在双换行之后
            }
        }

        // 优先级2：中文句号
        for (int i = end; i >= searchStart; i--) {
            if (text.charAt(i) == '。') {
                return i + 1;
            }
        }

        // 优先级3：英文句点（后跟空格或换行，避免切到小数点）
        for (int i = end; i >= searchStart; i--) {
            if (text.charAt(i) == '.' && (i + 1 >= text.length() || Character.isWhitespace(text.charAt(i + 1)))) {
                return i + 1;
            }
        }

        // 优先级4：换行
        for (int i = end; i >= searchStart; i--) {
            if (text.charAt(i) == '\n') {
                return i + 1;
            }
        }

        // 优先级5：逗号
        for (int i = end; i >= searchStart; i--) {
            if (text.charAt(i) == '，' || text.charAt(i) == ',') {
                return i + 1;
            }
        }

        // 兜底：在空格处切割
        for (int i = end; i >= searchStart; i--) {
            if (Character.isWhitespace(text.charAt(i))) {
                return i + 1;
            }
        }

        // 实在找不到好的切割点，直接按固定长度切
        return end;
    }
}
