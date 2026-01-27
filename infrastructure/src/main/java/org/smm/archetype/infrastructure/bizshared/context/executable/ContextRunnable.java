package org.smm.archetype.infrastructure.bizshared.context.executable;

import org.smm.archetype.infrastructure.bizshared.context.Context;
import org.smm.archetype.infrastructure.bizshared.context.ContextHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 包含上下文的执行器
 * @author Leonardo
 * @since 2025/12/30
 */
public abstract class ContextRunnable implements Runnable {

    private final List<Context<?>> contexts = new ArrayList<>();

    public ContextRunnable() {
        contexts.addAll(ContextHolder.getAll());
    }

    @SafeVarargs
    public ContextRunnable(Class<? extends Context<?>>... classes) {
        contexts.addAll(
                Arrays.stream(classes)
                        .map(ContextHolder::get)
                        .filter(Objects::nonNull)
                        .map(Context::export)
                        .toList()
        );
    }

    @Override
    public void run() {
        try {
            contexts.forEach(ContextHolder::createContext);
            doRun();
        } finally {
            ContextHolder.clear();
        }
    }

    /**
     * 运行方法
     */
    protected abstract void doRun();

}
