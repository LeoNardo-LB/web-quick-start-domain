package org.smm.archetype.infrastructure.shared.util.context;

import java.util.Map;

/**
 * 包含上下文的执行器（基于ScopedValue实现）


 */
public abstract class ContextRunnable implements Runnable {

    private final Map<String, Object> snapshot;

    public ContextRunnable() {
        this.snapshot = ScopedThreadContext.snapshot();
    }

    @Override
    public void run() {
        ScopedThreadContext.run(snapshot, this::doRun);
    }

    /**
     * 运行方法
     */
    protected abstract void doRun();

}
