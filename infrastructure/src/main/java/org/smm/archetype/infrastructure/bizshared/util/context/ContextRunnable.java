package org.smm.archetype.infrastructure.bizshared.util.context;

import java.util.Map;

/**
 * 包含上下文的执行器（基于ScopedValue实现）
 * @author Leonardo
 * @since 2025/12/30
 */
public abstract class ContextRunnable implements Runnable {

    private final Map<String, Object> snapshot;

    public ContextRunnable() {
        this.snapshot = MyContext.snapshot();
    }

    @Override
    public void run() {
        MyContext.run(snapshot, this::doRun);
    }

    /**
     * 运行方法
     */
    protected abstract void doRun();

}
