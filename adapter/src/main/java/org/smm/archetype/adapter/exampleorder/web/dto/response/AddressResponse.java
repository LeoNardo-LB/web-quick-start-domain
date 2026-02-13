package org.smm.archetype.adapter.exampleorder.web.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * 地址响应
 */
@Getter
@Builder(setterPrefix = "set")
public class AddressResponse {

    /**
     * 省
     */
    private String province;

    /**
     * 市
     */
    private String city;

    /**
     * 区
     */
    private String district;

    /**
     * 详细地址
     */
    private String detailAddress;

    /**
     * 邮政编码
     */
    private String postalCode;

}
