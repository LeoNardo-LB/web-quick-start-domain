package org.smm.archetype.adapter._example.web.dto.response;

import lombok.Getter;
import lombok.Setter;
import org.smm.archetype.app._example.dto.ContactInfoDTO;

/**
 * 联系信息响应
 * @author Leonardo
 * @since 2026/1/11
 */
@Getter
@Setter
public class ContactInfoResponse {

    /**
     * 联系人姓名
     */
    private String contactName;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 联系邮箱
     */
    private String contactEmail;

    /**
     * 从DTO转换
     */
    public static ContactInfoResponse fromDTO(ContactInfoDTO dto) {
        if (dto == null) {
            return null;
        }
        ContactInfoResponse response = new ContactInfoResponse();
        response.setContactName(dto.getContactName());
        response.setContactPhone(dto.getContactPhone());
        response.setContactEmail(dto.getContactEmail());
        return response;
    }

}
