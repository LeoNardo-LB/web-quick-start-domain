package org.smm.archetype.infrastructure.shared.util.context;

import org.smm.archetype.domain.shared.event.Event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * 线程上下文持有者（基于ScopedValue实现）
 * 支持 userId、traceId、domainEvents 等上下文信息传递
 */
public class ScopedThreadContext {

    private static final ScopedValue<Map<String, Object>> CONTEXT_SCOPE = ScopedValue.newInstance();

    private static final String KEY_USER_ID       = "userId";
    private static final String KEY_TRACE_ID      = "traceId";
    private static final String KEY_DOMAIN_EVENTS = "domainEvents";

    // ============ 对外 API ============

    public static String getUserId() {
        return get(KEY_USER_ID);
    }

    public static void runWithUserId(String userId, Runnable runnable) {
        run(Map.of(KEY_USER_ID, userId), runnable);
    }

    /**
     * 获取当前 traceId
     * @return traceId，如果不存在则返回 null
     */
    public static String getTraceId() {
        return get(KEY_TRACE_ID);
    }

    /**
     * 在指定 traceId 下执行任务
     * @param traceId 请求追踪ID
     * @param runnable 要执行的任务
     */
    public static void runWithTraceId(String traceId, Runnable runnable) {
        Map<String, Object> ctx = new HashMap<>(CONTEXT_SCOPE.orElse(Map.of()));
        ctx.put(KEY_TRACE_ID, traceId);
        run(ctx, runnable);
    }

    /**
     * 在指定上下文（userId + traceId）下执行任务
     * @param userId 用户ID
     * @param traceId 请求追踪ID
     * @param runnable 要执行的任务
     */
    public static void runWithContext(String userId, String traceId, Runnable runnable) {
        Map<String, Object> ctx = new HashMap<>();
        ctx.put(KEY_USER_ID, userId);
        ctx.put(KEY_TRACE_ID, traceId);
        run(ctx, runnable);
    }

    /**
     * 生成新的 traceId
     * @return UUID 格式的 traceId
     */
    public static String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
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
