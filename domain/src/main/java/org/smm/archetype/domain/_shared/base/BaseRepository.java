package org.smm.archetype.domain._shared.base;

/**
 *
 *
 * @author Leonardo
 * @since 2025/12/30
 */
public interface BaseRepository<T extends BaseModel> {

    default void insert(T model) {
        throw new UnsupportedOperationException("Not Implement");
    }

    default int update(T model) {
        throw new UnsupportedOperationException("Not Implement");
    }

    default int delete(T model) {
        throw new UnsupportedOperationException("Not Implement");
    }

    default T selectOne(T model) {
        throw new UnsupportedOperationException("Not Implement");
    }

    default PageModel<T> selectPage(T model) {
        throw new UnsupportedOperationException("Not Implement");
    }

}