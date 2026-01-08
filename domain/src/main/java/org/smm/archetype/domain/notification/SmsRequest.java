package org.smm.archetype.domain.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 短信请求
 * @author Leonardo
 * @since 2026/01/09
 */
@Getter
@Setter
@Builder(setterPrefix = "set")
@NoArgsConstructor
@AllArgsConstructor
public class SmsRequest {

    /**
     * 手机号
     */
    private String phoneNumber;

    /**
     * 短信签名
     */
    private String signName;

    /**
     * 短信模板代码
     */
    private String templateCode;

    /**
     * 模板参数
     */
    private java.util.Map<String, Object> templateParam;

}
