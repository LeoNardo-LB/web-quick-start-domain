package org.smm.archetype.domain._shared.specification;

/**
 * NOT取反规格
 *
 * <p>当原始规格不满足时，此规格满足。
 * @param <T> 被规格检查的对象类型
 * @author Leonardo
 * @since 2025/12/30
 */
public class NotSpecification<T> implements Specification<T> {

    private final Specification<T> spec;

    public NotSpecification(Specification<T> spec) {
        this.spec = spec;
    }

    @Override
    public boolean isSatisfiedBy(T candidate) {
        return !spec.isSatisfiedBy(candidate);
    }

}
