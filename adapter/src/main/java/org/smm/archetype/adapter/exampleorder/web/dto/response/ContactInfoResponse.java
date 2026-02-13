package org.smm.archetype.adapter.exampleorder.web.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * 联系信息响应
 */
@Getter
@Builder(setterPrefix = "set")
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

}
