package org.smm.archetype.infrastructure._shared.converter;

import org.smm.archetype.infrastructure._shared.dal.BaseDO;

/**
 * 领域对象转换器基接口
 *
 * <p>职责：定义领域对象和数据对象之间的转换契约</p>
 *
 * <h3>泛型说明</h3>
 * <ul>
 *   <li>{@code <E>} - 领域对象类型（Entity、ValueObject或AggregateRoot）</li>
 *   <li>{@code <D>} - 数据对象类型，必须继承自{@link BaseDO}</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * &#64;Mapper(componentModel = "spring")
 * public interface OrderConverter extends BaseDomainConverter<Order, OrderDO> {
 *
 *     &#64;Override
 *     Order toEntity(OrderDO dataObject);
 *
 *     &#64;Override
 *     OrderDO toDataObject(Order entity);
 * }
 * }</pre>
 * @param <E> 领域对象类型（Entity或ValueObject）
 * @param <D> 数据对象类型
 * @see org.smm.archetype.domain._shared.base.Entity
 * @see org.smm.archetype.domain._shared.base.ValueObject
 * @see org.smm.archetype.infrastructure._shared.dal.BaseDO
 */
public interface BaseDomainConverter<E, D extends BaseDO> {

    /**
     * 将数据对象转换为领域对象
     * @param dataObject 数据对象
     * @return 领域对象（Entity、ValueObject或AggregateRoot）
     */
    E toEntity(D dataObject);

    /**
     * 将领域对象转换为数据对象
     * @param entity 领域对象（Entity、ValueObject或AggregateRoot）
     * @return 数据对象
     */
    D toDataObject(E entity);

}
