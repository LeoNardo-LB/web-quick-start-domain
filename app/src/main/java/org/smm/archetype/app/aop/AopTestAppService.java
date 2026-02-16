package org.smm.archetype.app.aop;

import org.smm.archetype.infrastructure.shared.util.context.ScopedThreadContext;
import org.springframework.stereotype.Service;

/**
 * AOP 测试专用的 AppService
 * 用于验证 DomainEventCollectAspectJ 切面是否生效
 *
 * <p>注意：此类必须放在 org.smm.archetype.app 包下才能被切点表达式匹配。</p>
 */
@Service
public class AopTestAppService {

    /**
     * 检查 AOP 是否生效
     * 如果切面生效，ScopedThreadContext.getDomainEvents() 应该返回非空列表
     * @return 如果 AOP 生效返回 true，否则返回 false
     */
    public boolean isAopEnabled() {
        return ScopedThreadContext.getDomainEvents() != null;
    }

    /**
     * 获取领域事件列表的大小
     * 用于验证 AOP 上下文是否正确初始化
     * @return 领域事件列表大小，如果 AOP 未生效则抛出异常
     */
    public int getDomainEventsSize() {
        var events = ScopedThreadContext.getDomainEvents();
        if (events == null) {
            throw new IllegalStateException("AOP 未生效：ScopedThreadContext.getDomainEvents() 返回 null");
        }
        return events.size();
    }

}
