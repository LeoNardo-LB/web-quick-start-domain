package org.smm.archetype.domain._shared.specification;

/**
 * OR组合规格
 *
 * <p>当至少一个规格满足时，此规格就满足。
 * @param <T> 被规格检查的对象类型
 * @author Leonardo
 * @since 2025/12/30
 */
public class OrSpecification<T> implements Specification<T> {

    private final Specification<T> one;
    private final Specification<T> other;

    public OrSpecification(Specification<T> one, Specification<T> other) {
        this.one = one;
        this.other = other;
    }

    @Override
    public boolean isSatisfiedBy(T candidate) {
        return one.isSatisfiedBy(candidate) || other.isSatisfiedBy(candidate);
    }

}
