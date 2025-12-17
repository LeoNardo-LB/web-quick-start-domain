package org.smm.archetype.common.context;

import java.util.concurrent.Callable;

import static org.smm.archetype.common.context.Context.export;

/**
 * 上下文Call
 */
public abstract class CtxCall<T> implements Callable<T> {

    private final Context CONTEXT;

    public CtxCall() {
        CONTEXT = export();
    }

    @Override
    public T call() {
        try {
            Context.load(CONTEXT);
            return doCall();
        } finally {
            Context.clear();
        }
    }

    /**
     * 实际执行的方法
     */
    protected abstract T doCall();

}