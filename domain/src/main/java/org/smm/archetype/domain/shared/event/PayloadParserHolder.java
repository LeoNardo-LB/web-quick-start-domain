package org.smm.archetype.domain.shared.event;

/**
 * 载荷解析器持有者
 *
 * <p>用于在Domain层提供对PayloadParser实现的访问，同时保持Domain层的纯净性。
 * Infrastructure层在启动时通过 {@link #setParser(PayloadParser)} 设置具体实现。</p>
 *
 * <p>使用示例：</p>
 * <pre>
 * // Infrastructure层启动时设置
 * PayloadParserHolder.setParser(new FastJsonPayloadParser());
 *
 * // Domain层使用
 * PayloadParser parser = PayloadParserHolder.getParser();
 * MyObject obj = parser.parseObject(json, MyObject.class);
 * </pre>
 */
public final class PayloadParserHolder {

    private static volatile PayloadParser parser;

    private PayloadParserHolder() {
        // 私有构造函数，防止实例化
    }

    /**
     * 获取当前配置的PayloadParser
     *
     * @return PayloadParser实例
     * @throws IllegalStateException 如果未设置Parser
     */
    public static PayloadParser getParser() {
        if (parser == null) {
            throw new IllegalStateException("PayloadParser未初始化，请确保Infrastructure层已正确配置");
        }
        return parser;
    }

    /**
     * 设置PayloadParser实现
     *
     * <p>此方法应由Infrastructure层在应用启动时调用一次。</p>
     *
     * @param payloadParser PayloadParser实现
     */
    public static void setParser(PayloadParser payloadParser) {
        parser = payloadParser;
    }

    /**
     * 检查是否已配置Parser
     *
     * @return 如果已配置返回true
     */
    public static boolean isConfigured() {
        return parser != null;
    }

    /**
     * 重置Parser（主要用于测试）
     */
    public static void reset() {
        parser = null;
    }

}
