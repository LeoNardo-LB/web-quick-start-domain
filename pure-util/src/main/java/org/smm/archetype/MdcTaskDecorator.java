package org.smm.archetype;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;

/**
 * MDC任务装饰器
 * <p>
 * MDC任务装饰器，用于在多线程任务中传递MDC上下文。
 */
public class MdcTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        // 捕获父线程的MDC上下文
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        return () -> {
            try {
                // 在子线程恢复上下文
                if (contextMap != null) {
                    MDC.setContextMap(contextMap);
                }
                runnable.run();
            } finally {
                // 清理避免线程污染
                MDC.clear();
            }
        };
    }

}
