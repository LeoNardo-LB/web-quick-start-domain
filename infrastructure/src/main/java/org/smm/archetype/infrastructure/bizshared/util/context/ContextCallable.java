package org.smm.archetype.infrastructure.bizshared.util.context;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * 包含上下文的执行器（基于ScopedValue实现）


 */
public abstract class ContextCallable<V> implements Callable<V> {

    private final Map<String, Object> snapshot;

    public ContextCallable() {
        this.snapshot = MyContext.snapshot();
    }

    @Override
    public V call() {
        try {
            return MyContext.call(snapshot, this::doCall);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 运行方法
     */
    protected abstract V doCall();

}
