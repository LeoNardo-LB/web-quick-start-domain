package org.smm.archetype.infrastructure._shared.event;

import lombok.Getter;
import org.smm.archetype.domain._shared.base.DomainEvent;
import org.springframework.context.ApplicationEvent;

/**
 * Spring事件包装类
 *
 * <p>将领域事件包装为Spring ApplicationEvent，
 * 以便使用Spring的事件机制发布和订阅。
 *
 * <p>使用Spring事件机制的优势：
 * <ul>
 *   <li>原生支持观察者模式</li>
 *   <li>支持同步和异步处理</li>
 *   <li>支持条件过滤（@EventListener的condition属性）</li>
 *   <li>支持事务绑定（@TransactionalEventListener）</li>
 * </ul>
 * @author Leonardo
 * @since 2026/01/09
 */
@Getter
public class DomainSpringEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    /**
     * 领域事件
     */
    private final DomainEvent domainEvent;

    /**
     * 构造函数
     * @param source      事件源（通常是发布者对象）
     * @param domainEvent 领域事件
     */
    public DomainSpringEvent(Object source, DomainEvent domainEvent) {
        super(source);
        this.domainEvent = domainEvent;
    }

}
