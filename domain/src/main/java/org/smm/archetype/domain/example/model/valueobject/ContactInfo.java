package org.smm.archetype.domain.example.model.valueobject;

import lombok.Getter;
import org.smm.archetype.domain.bizshared.base.ValueObject;

import java.util.Objects;
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

    /**
     * 私有构造函数
     */
    private ContactInfo(Builder builder) {
        this.contactName = Objects.requireNonNull(builder.contactName, "联系人姓名不能为空");
        this.contactPhone = Objects.requireNonNull(builder.contactPhone, "联系人电话不能为空");
        this.contactEmail = builder.contactEmail;

        // 验证电话格式
        if (!PHONE_PATTERN.matcher(this.contactPhone).matches()) {
            throw new IllegalArgumentException("电话号码格式不正确");
        }

        // 验证邮箱格式（如果提供）
        if (this.contactEmail != null && !EMAIL_PATTERN.matcher(this.contactEmail).matches()) {
            throw new IllegalArgumentException("邮箱格式不正确");
        }
    }

    /**
     * 创建构建器
     * @return 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 检查是否提供了邮箱
     * @return 提供了返回true
     */
    public boolean hasEmail() {
        return contactEmail != null && !contactEmail.isEmpty();
    }

    @Override
    protected Object[] equalityFields() {
        return new Object[] {contactName, contactPhone, contactEmail};
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", contactName, contactPhone);
    }

    /**
     * 构建器
     */
    public static class Builder {

        private String contactName;
        private String contactPhone;
        private String contactEmail;

        public Builder contactName(String contactName) {
            this.contactName = contactName;
            return this;
        }

        public Builder contactPhone(String contactPhone) {
            this.contactPhone = contactPhone;
            return this;
        }

        public Builder contactEmail(String contactEmail) {
            this.contactEmail = contactEmail;
            return this;
        }

        /**
         * 构建联系信息对象
         * @return 联系信息值对象
         */
        public ContactInfo build() {
            return new ContactInfo(this);
        }

    }

}
