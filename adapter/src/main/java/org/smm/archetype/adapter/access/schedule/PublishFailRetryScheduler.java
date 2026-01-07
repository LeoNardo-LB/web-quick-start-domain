package org.smm.archetype.adapter.access.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.domain._shared.component.event.EventRepository;
import org.smm.archetype.domain._shared.base.BaseEvent;
import org.smm.archetype.domain._shared.service.EventService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 定时任务，用于重试就绪的事件
 * 
 * @author Leonardo
 * @since 2025/12/30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PublishFailRetryScheduler {

    private final EventRepository eventRepository;

    private final EventService<BaseEvent<?>> eventService;

    /**
     * 每分钟执行一次，重试就绪状态的事件
     */
    @Scheduled(fixedDelay = 60000)
    public void retryReadyEvents() {
        try {
            // 查询就绪状态的事件，每次最多处理100个
            List<BaseEvent<?>> readyEvents = eventRepository.findReadyEvents(100);
            
            if (readyEvents.isEmpty()) {
                log.debug("没有待重试的事件");
                return;
            }

            log.info("开始重试就绪事件，数量：{}", readyEvents.size());

            int successCount = 0;
            int failCount = 0;

            for (BaseEvent<?> event : readyEvents) {
                try {
                    // 重新发布事件
                    eventService.publish(event);
                    
                    successCount++;
                    
                    log.debug("事件重试成功，eventId: {}", event.getId());
                } catch (Exception e) {
                    failCount++;
                    log.error("事件重试失败，eventId: {}", event.getId(), e);
                    // 失败的事件保持READY状态，等待下次重试
                }
            }

            log.info("事件重试完成，成功：{}，失败：{}", successCount, failCount);

        } catch (Exception e) {
            log.error("事件重试调度器执行异常", e);
        }
    }

}