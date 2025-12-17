package org.smm.archetype.common.context;

import static org.smm.archetype.common.context.Context.export;

/**
 * 上下文Run
 */
public abstract class CtxRun implements Runnable {

    private final Context CONTEXT;

    public CtxRun() {
        CONTEXT = export();
    }

    @Override
    public void run() {
        try {
            Context.load(CONTEXT);
            doRun();
        } finally {
            Context.clear();
        }
    }

    /**
     * 实际执行的方法
     */
    protected abstract void doRun();

}
