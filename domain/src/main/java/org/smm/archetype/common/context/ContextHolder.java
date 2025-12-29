package org.smm.archetype.common.context;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 *
 * @author Leonardo
 * @since 2025/12/30
 */
public class ContextHolder {

    private static final Map<Class<?>, ThreadLocal<?>> contextMap = new ConcurrentHashMap<>();

    public static <T> void createContext(T data) {
        if (data == null) {
            return;
        }
        ThreadLocal<T> threadLocal = new ThreadLocal<>();
        threadLocal.set(data);
        contextMap.put(data.getClass(), threadLocal);
    }

    public static <T> Optional<T> get(Class<T> tClass) {
        ThreadLocal<?> threadLocal = contextMap.get(tClass);
        Object o = threadLocal.get();
        if (o == null) {
            threadLocal.remove();
        }
        return (Optional<T>) Optional.ofNullable(o);
    }

    public static <T> List<T> getAll() {
        return (List<T>) contextMap.keySet().stream().map(ContextHolder::get).filter(Optional::isPresent).map(Optional::get).toList();
    }

    public static void clear() {
        contextMap.values().forEach(ThreadLocal::remove);
    }

    public static <T> List<T> export() {
        return (List<T>) contextMap.values().stream().map(ThreadLocal::get).toList();
    }

}
