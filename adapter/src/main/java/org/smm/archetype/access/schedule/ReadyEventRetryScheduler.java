package org.smm.archetype.access.schedule;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时任务，用于重试就绪的事件
 * @author Leonardo
 * @since 2025/12/30
 */
@Component
@RequiredArgsConstructor
public class ReadyEventRetryScheduler {

    @Scheduled(fixedDelay = 50000)
    public void retry() {
        // todo 待完善
    }

}