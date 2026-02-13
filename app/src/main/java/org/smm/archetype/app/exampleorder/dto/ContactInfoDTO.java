package org.smm.archetype.app.exampleorder.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 联系信息数据传输对象。
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
