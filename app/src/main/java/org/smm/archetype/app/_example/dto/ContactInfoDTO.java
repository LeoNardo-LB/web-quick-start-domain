package org.smm.archetype.app._example.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * 联系信息DTO
 * @author Leonardo
 * @since 2026/1/11
 */
@Getter
@Builder(setterPrefix = "set")
public class ContactInfoDTO {

    /**
     * 联系人姓名
     */
    private String contactName;

    /**
     * 联系人电话
     */
    private String contactPhone;

    /**
     * 联系人邮箱
     */
    private String contactEmail;

}
