package org.smm.archetype.app.exampleorder.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 地址数据传输对象。
 */
@Getter
@Builder(setterPrefix = "set")
public class AddressDTO {

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 区/县
     */
    private String district;

    /**
     * 详细地址
     */
    private String detailAddress;

    /**
     * 邮编
     */
    private String postalCode;

}
