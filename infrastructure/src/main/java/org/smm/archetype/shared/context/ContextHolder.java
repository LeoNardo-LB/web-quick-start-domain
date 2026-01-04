package org.smm.archetype.shared.context;

import org.smm.archetype.shared.context.impl.Context;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 *
 * @author Leonardo
 * @since 2025/12/30
 */
public class ContextHolder {

    private static final Map<Class<Context<?>>, ThreadLocal<Context<?>>> contextMap = new ConcurrentHashMap<>();

    public static <T extends Context<?>> void createContext(T context) {
        if (context == null) {
            return;
        }
        ThreadLocal<Context<?>> threadLocal = new ThreadLocal<>();
        threadLocal.set(context);
        contextMap.put((Class<Context<?>>) context.getClass(), threadLocal);
    }

    public static <T extends Context<?>> T get(Class<T> tClass) {
        ThreadLocal<Context<?>> threadLocal = contextMap.get(tClass);
        Context<?> context = threadLocal.get();
        if (context == null) {
            threadLocal.remove();
        }
        return (T) context;
    }

    public static <T extends Context<?>> List<T> getAll() {
        return (List<T>) contextMap.keySet().stream().map(ContextHolder::get).filter(Objects::nonNull).toList();
    }

    public static void clear() {
        contextMap.values().forEach(ThreadLocal::remove);
    }

    public static <T extends Context<?>> List<T> export() {
        return (List<T>) contextMap.values().stream().map(ThreadLocal::get).toList();
    }

}
