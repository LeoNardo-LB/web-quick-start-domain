package org.smm.archetype.domain.example.model.valueobject;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.smm.archetype.domain.bizshared.base.ValueObject;

import java.util.regex.Pattern;

/**
 * 联系信息值对象
 *
 * <p>特征：
 * <ul>
 *   <li>不可变性（Immutable）</li>
 *   <li>包含姓名、电话、邮箱</li>
 *   <li>验证联系信息格式</li>
 * </ul>
 * @author Leonardo
 * @since 2026/1/11
 */
@Getter
@SuperBuilder(setterPrefix = "set")
public class ContactInfo extends ValueObject {

    /**
     * 手机号正则表达式
     */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    /**
     * 邮箱正则表达式
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    /**
     * 联系人姓名
     */
    private final String contactName;

    /**
     * 联系人电话
     */
    private final String contactPhone;

    /**
     * 联系人邮箱（可选）
     */
    private final String contactEmail;

    @Override
    protected Object[] equalityFields() {
        return new Object[] {contactName, contactPhone, contactEmail};
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", contactName, contactPhone);
    }

}
