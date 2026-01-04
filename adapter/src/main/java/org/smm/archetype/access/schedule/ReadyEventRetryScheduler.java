package org.smm.archetype.access.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时任务，用于重试就绪的事件
 * @author Leonardo
 * @since 2025/12/30
 */
@Component
@Slf4j
public class ReadyEventRetryScheduler {

    @Scheduled(fixedDelay = 60000)
    public void schedule() {
        log.debug("定时任务测试");
    }

}