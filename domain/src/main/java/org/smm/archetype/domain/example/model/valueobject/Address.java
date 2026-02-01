package org.smm.archetype.domain.example.model.valueobject;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.smm.archetype.domain.bizshared.base.ValueObject;

/**
 * 地址值对象
 *
 * <p>特征：
 * <ul>
 *   <li>不可变性（Immutable）</li>
 *   <li>包含省、市、区、详细地址、邮编</li>
 *   <li>基于属性值的相等性</li>
 * </ul>
 * @author Leonardo
 * @since 2026/1/11
 */
@Getter
@SuperBuilder(setterPrefix = "set", builderMethodName = "ABuilder")
public class Address extends ValueObject {

    /**
     * 省份
     */
    private final String province;

    /**
     * 城市
     */
    private final String city;

    /**
     * 区县（可选）
     */
    private final String district;

    /**
     * 详细地址
     */
    private final String detailAddress;

    /**
     * 邮政编码（可选）
     */
    private final String postalCode;

    /**
     * 获取完整地址字符串
     * @return 完整地址
     */
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        sb.append(province).append(city);
        if (district != null && !district.isEmpty()) {
            sb.append(district);
        }
        sb.append(detailAddress);
        return sb.toString();
    }

    @Override
    protected Object[] equalityFields() {
        return new Object[] {province, city, district, detailAddress, postalCode};
    }

    @Override
    public String toString() {
        return getFullAddress();
    }

}
