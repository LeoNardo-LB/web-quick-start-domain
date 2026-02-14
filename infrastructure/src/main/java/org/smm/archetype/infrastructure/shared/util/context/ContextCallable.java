package org.smm.archetype.infrastructure.shared.util.context;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * 包含上下文的执行器（基于ScopedValue实现）
 *
 * @param <V> 返回值类型
 */
public abstract class ContextCallable<V> implements Callable<V> {

    private final Map<String, Object> snapshot;

    public ContextCallable() {
        this.snapshot = ScopedThreadContext.snapshot();
    }

    @Override
    public V call() {
        try {
            return ScopedThreadContext.call(snapshot, this::doCall);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 运行方法
     */
    protected abstract V doCall();

}
