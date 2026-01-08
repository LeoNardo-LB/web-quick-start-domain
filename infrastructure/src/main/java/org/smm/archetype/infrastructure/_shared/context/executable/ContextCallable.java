package org.smm.archetype.infrastructure._shared.context.executable;

import org.smm.archetype.infrastructure._shared.context.Context;
import org.smm.archetype.infrastructure._shared.context.ContextHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * 包含上下文的执行器
 * @author Leonardo
 * @since 2025/12/30
 */
public abstract class ContextCallable<V> implements Callable<V> {

    private final List<Context<?>> contexts = new ArrayList<>();

    public ContextCallable() {
        contexts.addAll(ContextHolder.getAll());
    }

    @SafeVarargs
    public ContextCallable(Class<? extends Context<?>>... classes) {
        contexts.addAll(
                Arrays.stream(classes)
                        .map(ContextHolder::get)
                        .filter(Objects::nonNull)
                        .map(Context::export)
                        .toList()
        );
    }

    @Override
    public V call() {
        try {
            contexts.forEach(ContextHolder::createContext);
            return doCall();
        } finally {
            ContextHolder.clear();
        }
    }

    /**
     * 运行方法
     */
    protected abstract V doCall();

}
