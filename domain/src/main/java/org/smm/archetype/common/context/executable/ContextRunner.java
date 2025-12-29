package org.smm.archetype.common.context.executable;

import org.smm.archetype.common.context.ContextHolder;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 包含上下文的执行器
 * @author Leonardo
 * @since 2025/12/30
 */
public abstract class ContextRunner implements Runnable {

    private final List<Object> datas = new ArrayList<>();

    public ContextRunner() {
        datas.addAll(ContextHolder.getAll());
    }

    public ContextRunner(Class<?>... classes) {
        datas.addAll(Arrays.stream(classes).map(type -> ContextHolder.get(type).orElse(null)).filter(Objects::nonNull).toList());
    }

    @Override
    public void run() {
        try {
            for (Object data : datas) {
                ContextHolder.createContext(data);
            }
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
