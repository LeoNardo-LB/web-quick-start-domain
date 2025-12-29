package org.smm.archetype.common.context.executable;

import org.smm.archetype.common.context.ContextHolder;

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
public abstract class ContextCaller<T> implements Callable<T> {

    private final List<Object> datas = new ArrayList<>();

    public ContextCaller() {
        datas.addAll(ContextHolder.getAll());
    }

    public ContextCaller(Class<?>... classes) {
        datas.addAll(Arrays.stream(classes).map(type -> ContextHolder.get(type).orElse(null)).filter(Objects::nonNull).toList());
    }

    @Override
    public T call() throws Exception {
        try {
            for (Object data : datas) {
                ContextHolder.createContext(data);
            }
            return doCall();
        } finally {
            ContextHolder.clear();
        }
    }

    /**
     * 运行方法
     */
    protected abstract T doCall();

}
