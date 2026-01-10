package org.smm.archetype.infrastructure._shared.context;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 线程上下文持有者
 * @author Leonardo
 * @since 2025/12/30
 */
public class ContextHolder {

    private static final Map<Class<?>, ThreadLocal<? extends Context<?>>> contextMap = new ConcurrentHashMap<>();

    public static <T extends Context<?>> void createContext(T context) {
        if (context == null) {
            return;
        }
        ThreadLocal<T> threadLocal = new ThreadLocal<>();
        threadLocal.set(context);
        contextMap.put(context.getClass(), threadLocal);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Context<?>> T get(Class<T> tClass) {
        ThreadLocal<? extends Context<?>> threadLocal = contextMap.get(tClass);
        if (threadLocal == null) {
            return null;
        }
        Context<?> context = threadLocal.get();
        if (context == null) {
            threadLocal.remove();
            return null;
        }
        return (T) context;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Context<?>> List<T> getAll() {
        return contextMap.keySet().stream()
                       .map(clazz -> {
                           Class<T> tClass = (Class<T>) clazz;
                           return get(tClass);
                       })
                       .filter(Objects::nonNull)
                       .toList();
    }

    public static void clear() {
        contextMap.values().forEach(ThreadLocal::remove);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Context<?>> List<T> export() {
        return contextMap.values().stream()
                       .map(threadLocal -> (T) threadLocal.get())
                       .filter(Objects::nonNull)
                       .toList();
    }

}
