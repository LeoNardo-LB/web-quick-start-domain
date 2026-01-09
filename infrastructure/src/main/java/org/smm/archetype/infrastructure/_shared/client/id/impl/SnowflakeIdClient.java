package org.smm.archetype.infrastructure._shared.client.id.impl;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain._shared.client.IdClient;
import org.smm.archetype.infrastructure._shared.client.id.AbstractIdClient;
import org.springframework.beans.factory.annotation.Value;

/**
 * Snowflake ID 生成服务实现
 *
 * <p>基于 Twitter Snowflake 算法的分布式 ID 生成器。
 *
 * <p>ID 结构（64位 Long）：
 * <ul>
 *   <li>1位：符号位（始终为0）</li>
 *   <li>41位：时间戳（毫秒级，可用69年）</li>
 *   <li>5位：数据中心 ID（0-31）</li>
 *   <li>5位：机器 ID（0-31）</li>
 *   <li>12位：序列号（0-4095，每毫秒可生成4096个ID）</li>
 * </ul>
 *
 * <p>配置参数（通过 application.yaml 配置）：
 * <ul>
 *   <li>id.snowflake.datacenter-id：数据中心 ID（默认0）</li>
 *   <li>id.snowflake.worker-id：机器 ID（默认0）</li>
 *   <li>id.snowflake.epoch：起始时间戳（默认2026-01-01 00:00:00）</li>
 * </ul>
 * @author Leonardo
 * @since 2026/01/09
 */
@Slf4j
@Getter
public class SnowflakeIdClient extends AbstractIdClient {

    /**
     * 起始时间戳（2026-01-01 00:00:00 UTC）
     */
    private static final long DEFAULT_EPOCH = 1735660800000L;

    /**
     * 数据中心 ID 位数
     */
    private static final long DATACENTER_ID_BITS = 5L;

    /**
     * 机器 ID 位数
     */
    private static final long WORKER_ID_BITS = 5L;

    /**
     * 序列号位数
     */
    private static final long SEQUENCE_BITS = 12L;

    /**
     * 最大数据中心 ID（31）
     */
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);

    /**
     * 最大机器 ID（31）
     */
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);

    /**
     * 序列号最大值（4095）
     */
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

    /**
     * 机器 ID 左移位数（12位）
     */
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;

    /**
     * 数据中心 ID 左移位数（12+5=17位）
     */
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

    /**
     * 时间戳左移位数（12+5+5=22位）
     */
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;

    /**
     * 起始时间戳（从配置读取）
     */
    private final long epoch = System.currentTimeMillis();

    /**
     * 数据中心 ID（从配置读取）
     */
    @Value("${id.snowflake.datacenter-id:0}")
    private long datacenterId;

    /**
     * 机器 ID（从配置读取）
     */
    @Value("${id.snowflake.worker-id:0}")
    private long workerId;

    /**
     * 序列号
     */
    private long sequence = 0L;

    /**
     * 上次生成 ID 的时间戳
     */
    private long lastTimestamp = -1L;

    /**
     * 初始化方法（在构造后调用）
     * @throws IllegalArgumentException 当参数超出范围时抛出
     */
    @jakarta.annotation.PostConstruct
    public void init() {
        // 参数校验
        if (datacenterId < 0 || datacenterId > MAX_DATACENTER_ID) {
            throw new IllegalArgumentException(
                    String.format("Datacenter ID must be between 0 and %d", MAX_DATACENTER_ID));
        }
        if (workerId < 0 || workerId > MAX_WORKER_ID) {
            throw new IllegalArgumentException(
                    String.format("Worker ID must be between 0 and %d", MAX_WORKER_ID));
        }
        if (epoch < 0 || epoch > System.currentTimeMillis()) {
            throw new IllegalArgumentException("Epoch must be in the past");
        }

        log.info("SnowflakeIdClient initialized: datacenterId={}, workerId={}, epoch={}",
                datacenterId, workerId, epoch);
    }

    /**
     * 生成 ID（具体实现）
     * @param type ID 类型（Snowflake 算法不区分类型，但保留接口兼容性）
     * @return 64位 Long 类型的 ID 字符串
     */
    @Override
    protected synchronized String doGenerateId(IdClient.Type type) {
        long timestamp = getCurrentTimestamp();

        // 时钟回拨检查
        if (timestamp < lastTimestamp) {
            long offset = lastTimestamp - timestamp;
            log.error("Clock moved backwards! Refusing to generate ID for {} milliseconds", offset);
            throw new RuntimeException(
                    String.format("Clock moved backwards. Refusing to generate ID for %d milliseconds", offset));
        }

        // 同一毫秒内，序列号自增
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            // 序列号溢出，等待下一毫秒
            if (sequence == 0) {
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            // 新的毫秒，序列号重置
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        // 生成 ID
        long id = ((timestamp - epoch) << TIMESTAMP_SHIFT)
                          | (datacenterId << DATACENTER_ID_SHIFT)
                          | (workerId << WORKER_ID_SHIFT)
                          | sequence;

        return String.valueOf(id);
    }

    /**
     * 获取当前时间戳
     * @return 当前时间戳（毫秒）
     */
    protected long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

    /**
     * 等待下一毫秒
     * @param lastTimestamp 上次生成 ID 的时间戳
     * @return 新的时间戳
     */
    private long waitNextMillis(long lastTimestamp) {
        long timestamp = getCurrentTimestamp();
        while (timestamp <= lastTimestamp) {
            timestamp = getCurrentTimestamp();
        }
        return timestamp;
    }

}
