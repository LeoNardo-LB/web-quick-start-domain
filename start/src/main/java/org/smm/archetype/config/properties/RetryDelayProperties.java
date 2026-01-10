package org.smm.archetype.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 事件重试延迟配置属性
 *
 * <p>配置事件重试时的延迟策略（指数退避）。
 * @author Leonardo
 * @since 2026/1/10
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "middleware.event.retry")
public class RetryDelayProperties {

    /**
     * 重试延迟时间列表（分钟）
     *
     * <p>索引对应重试次数：
     * <ul>
     *   <li>索引0：第1次重试延迟</li>
     *   <li>索引1：第2次重试延迟</li>
     *   <li>索引2：第3次重试延迟</li>
     *   <li>...</li>
     * </ul>
     *
     * <p>如果重试次数超过列表长度，使用最后一个值。
     * <p>默认：[1, 5, 15, 30, 60]，即1分钟、5分钟、15分钟、30分钟、60分钟
     */
    private List<Integer> delays = new ArrayList<>(List.of(1, 5, 15, 30, 60));

    /**
     * 最大重试次数
     * <p>默认：5
     */
    private Integer maxRetryTimes = 5;

    /**
     * 获取指定重试次数的延迟时间
     * @param retryTimes 当前重试次数（从0开始）
     * @return 延迟时间（分钟）
     */
    public int getDelay(int retryTimes) {
        if (delays == null || delays.isEmpty()) {
            return 1; // 默认1分钟
        }
        int index = Math.min(retryTimes, delays.size() - 1);
        return delays.get(index);
    }

}
