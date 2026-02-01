package org.smm.archetype.infrastructure.bizshared.util.context;

import org.smm.archetype.domain.bizshared.event.Event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * 线程上下文持有者（基于ScopedValue实现）
 * @author Leonardo
 * @since 2025/12/30
 */
public class MyContext {

    private static final ScopedValue<Map<String, Object>> CONTEXT_SCOPE = ScopedValue.newInstance();

    private static final String KEY_USER_ID       = "userId";
    private static final String KEY_DOMAIN_EVENTS = "domainEvents";

    // ============ 对外 API ============

    public static String getUserId() {
        return get(KEY_USER_ID);
    }

    public static void runWithUserId(String userId, Runnable runnable) {
        run(Map.of(KEY_USER_ID, userId), runnable);
    }

    public static List<Event<?>> getDomainEvents() {
        return get(KEY_DOMAIN_EVENTS);
    }

    public static <V> V callWithDomainEvents(List<Event<?>> events, Callable<V> callable) {
        return call(Map.of(KEY_DOMAIN_EVENTS, events), callable);
    }

    // ============ 包内 API（供 ContextRunnable/ContextCallable 使用） ============

    @SuppressWarnings("unchecked")
    static <T> T get(String key) {
        return (T) CONTEXT_SCOPE.orElse(Map.of()).get(key);
    }

    static Map<String, Object> snapshot() {
        return new HashMap<>(CONTEXT_SCOPE.orElse(Map.of()));
    }

    static void run(Map<String, Object> ctx, Runnable runnable) {
        ScopedValue.where(CONTEXT_SCOPE, ctx).run(runnable);
    }

    static <V> V call(Map<String, Object> ctx, Callable<V> callable) {
        try {
            return ScopedValue.where(CONTEXT_SCOPE, ctx).call(callable::call);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
