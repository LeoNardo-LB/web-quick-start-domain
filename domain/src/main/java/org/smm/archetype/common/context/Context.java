package org.smm.archetype.common.context;

import cn.hutool.core.thread.NamedThreadFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.smm.archetype.common.event.Event;
import org.smm.archetype.common.event.Event.Type;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 事件上下文管理器
 * 用于在线程本地存储中管理事件，提供事件的添加、获取和清理功能。
 * 注意：使用完毕后必须调用clear()方法清理ThreadLocal，避免内存泄漏。
 * @author Leonardo
 * @since 2025/12/13
 */
public class Context {

    /**
     * 上下文事件
     */
    private static final ThreadLocal<Context> CONTEXT_EVENT = new ThreadLocal<>();

    /**
     * 定时清理过期事件
     */
    private static final ScheduledExecutorService EXPIRE_SCHEDULER =
            new ScheduledThreadPoolExecutor(2, new NamedThreadFactory("expire-context-event-clear-", true), new CallerRunsPolicy());

    private final Map<Type, LinkedList<Event<?>>> eventsMap = new LinkedHashMap<>();

    /**
     * 获取最新的事件
     * @param type 事件类型
     * @param <T>  事件数据类型
     * @return 最新事件
     */
    public static <T> Event<T> getLatest(Type type) {
        return (Event<T>) Optional.ofNullable(getThreadContext(false))
                                         .map(context -> context.getByType(type)).map(LinkedList::getFirst)
                                         .orElse(null);
    }

    /**
     * 获取所有事件
     * @param type 事件类型
     * @return 所有事件
     */
    public static LinkedList<Event<?>> getAll(Type type) {
        return Optional.ofNullable(getThreadContext(false)).map(context -> context.getByType(type)).orElse(null);
    }

    /**
     * 添加事件并执行方法，并清理事件
     * @param eSupplier 要添加的事件
     * @param callable  要执行的方法
     * @param <E>       事件数据类型
     * @param <R>       返回值类型
     * @return 方法执行结果
     */
    public static <E, R> R callThenClear(Supplier<Event<E>> eSupplier, Supplier<R> callable) {
        return wrapCallOrRun(eSupplier, callable, null);
    }

    /**
     * 添加事件并执行方法，并清理事件
     * @param eSupplier 要添加的事件
     * @param runnable  要执行的方法
     * @param <E>       事件数据类型
     * @return 方法执行结果
     */
    public static <E> void runThenClear(Supplier<Event<E>> eSupplier, Runnable runnable) {
        wrapCallOrRun(eSupplier, null, runnable);
    }

    /**
     * 添加事件并执行方法或者执行方法，并清理事件
     * @param eSupplier 要添加的事件
     * @param callable  要执行的方法
     * @param runnable  要执行的方法
     * @param <E>       事件数据类型
     * @param <R>       返回值类型
     * @return 方法执行结果
     */
    private static <E, R> R wrapCallOrRun(Supplier<Event<E>> eSupplier, Supplier<R> callable, Runnable runnable) {
        Event<?> event = eSupplier.get();
        Instant expireTime = event.getExpireTime();
        if (expireTime != null) {
            EXPIRE_SCHEDULER.schedule(() -> {
                clearEvent(event);
            }, expireTime.toEpochMilli() - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        }
        try {
            doAddEvent(event);
            if (callable != null) {
                return callable.get();
            } else if (runnable != null) {
                runnable.run();
                return null;
            } else {
                throw new IllegalArgumentException("Need callable or runnable at least");
            }
        } finally {
            if (expireTime == null) {
                clearEvent(event);
            }
        }
    }

    /**
     * 添加事件
     * @param event 要添加的事件
     */
    public static void addEvent(Event<?> event) {
        Instant expireTime = event.getExpireTime();
        if (expireTime != null) {
            EXPIRE_SCHEDULER.schedule(() -> {
                clearEvent(event);
            }, expireTime.toEpochMilli() - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        }
        doAddEvent(event);
    }

    /**
     * 清理事件
     * @param event 要清理的事件
     */
    public static void clearEvent(Event<?> event) {
        if (event == null || event.getValid().compareAndSet(true, false)) {
            return;
        }
        Type type = event.getType();
        Context context = getThreadContext(false);
        // 获取不到上下文的情况
        if (context == null) {
            CONTEXT_EVENT.remove();
            return;
        }
        // 从事件链表中移除事件
        LinkedList<Event<?>> byType = context.getByType(type);
        Optional.ofNullable(byType).ifPresent(events -> events.removeIf(e -> e.getSerialno().equals(event.getSerialno())));
        // 如果该类型事件链表为空，则从map中移除
        if (CollectionUtils.isEmpty(byType)) {
            context.eventsMap.remove(type);
        }
        // 如果map为空，则从线程本地变量中移除
        if (context.eventsMap.isEmpty()) {
            CONTEXT_EVENT.remove();
        }
    }

    /**
     * 导出当前上下文
     */
    static Context export() {
        Context currentContext = CONTEXT_EVENT.get();
        if (currentContext == null) {
            return null;
        }
        Context newContext = new Context();
        currentContext.eventsMap.forEach((type, events) -> {
            events.stream()
                    .filter(event -> event.getValid().get()) // 只复制有效的事件
                    .map(Event::copy)
                    .forEach(newContext::addNewEvent);
        });
        return newContext;
    }

    /**
     * 传入外部上下文
     */
    static void load(Context context) {
        CONTEXT_EVENT.set(context);
    }

    /**
     * 清理当前线程的事件上下文
     */
    static void clear() {
        CONTEXT_EVENT.remove();
    }

    /**
     * 添加事件到上下文头部
     * @param event 要添加的事件
     */
    private static <T> void doAddEvent(Event<T> event) {
        getThreadContext(true).addNewEvent(event);
    }

    /**
     * 获取当前线程的事件上下文
     * @param createIfNull 如果当前线程没有事件上下文，是否创建一个
     * @return 当前线程的事件上下文
     */
    private static Context getThreadContext(boolean createIfNull) {
        Context context = CONTEXT_EVENT.get();
        if (context == null && createIfNull) {
            context = new Context();
            CONTEXT_EVENT.set(context);
        }
        return context;
    }

    /**
     * 新增事件
     * @param event 要添加的事件
     */
    private void addNewEvent(Event<?> event) {
        eventsMap.computeIfAbsent(event.getType(), k -> new LinkedList<>()).addFirst(event);
    }

    /**
     * 新增事件
     * @param type 事件类型
     * @return 事件列表
     */
    private LinkedList<Event<?>> getByType(Type type) {
        return eventsMap.get(type);
    }

}