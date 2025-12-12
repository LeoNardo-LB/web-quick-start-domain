package org.smm.archetype.domain.user;

/**
 * 通用用户上下文工具类
 *
 * 提供获取当前用户信息的静态方法，在实际项目中通常会从Spring Security或Shiro等安全框架中获取当前用户信息。
 * 当前实现为占位符，始终返回默认的系统用户ID。
 */
public class UserContext {

    /**
     * 获取当前用户ID
     *
     * 获取当前登录用户的唯一标识符。在实际项目中，此方法应从安全框架中获取真实的用户ID。
     * 当前实现为占位符，始终返回默认的系统用户ID "system"。
     * @return 用户ID字符串，默认为"system"
     */
    public static String getId() {
        return "system";
    }

}
