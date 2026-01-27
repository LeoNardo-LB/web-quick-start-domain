package org.smm.archetype.adapter._example.web.dto.response;

import lombok.Getter;
import lombok.Setter;
import org.smm.archetype.app._example.dto.AddressDTO;

/**
 * 地址响应
 * @author Leonardo
 * @since 2026/1/11
 */
@Getter
@Setter
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

    /**
     * 从DTO转换
     */
    public static AddressResponse fromDTO(AddressDTO dto) {
        if (dto == null) {
            return null;
        }
        AddressResponse response = new AddressResponse();
        response.setProvince(dto.getProvince());
        response.setCity(dto.getCity());
        response.setDistrict(dto.getDistrict());
        response.setDetailAddress(dto.getDetailAddress());
        response.setPostalCode(dto.getPostalCode());
        return response;
    }

}
