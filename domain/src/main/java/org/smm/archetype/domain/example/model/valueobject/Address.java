package org.smm.archetype.domain.example.model.valueobject;

import lombok.Getter;
import org.smm.archetype.domain.bizshared.base.ValueObject;

import java.util.Objects;

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
     * 私有构造函数
     */
    private Address(Builder builder) {
        this.province = Objects.requireNonNull(builder.province, "省份不能为空");
        this.city = Objects.requireNonNull(builder.city, "城市不能为空");
        this.district = builder.district;
        this.detailAddress = Objects.requireNonNull(builder.detailAddress, "详细地址不能为空");
        this.postalCode = builder.postalCode;
    }

    /**
     * 创建构建器
     * @return 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

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

    /**
     * 构建器
     */
    public static class Builder {

        private String province;
        private String city;
        private String district;
        private String detailAddress;
        private String postalCode;

        public Builder province(String province) {
            this.province = province;
            return this;
        }

        public Builder city(String city) {
            this.city = city;
            return this;
        }

        public Builder district(String district) {
            this.district = district;
            return this;
        }

        public Builder detailAddress(String detailAddress) {
            this.detailAddress = detailAddress;
            return this;
        }

        public Builder postalCode(String postalCode) {
            this.postalCode = postalCode;
            return this;
        }

        /**
         * 构建地址对象
         * @return 地址值对象
         */
        public Address build() {
            return new Address(this);
        }

    }

}
