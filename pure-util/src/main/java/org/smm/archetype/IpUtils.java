package org.smm.archetype;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * IP地址工具类
 *
 * 提供获取客户端真实IP地址的功能，处理使用反向代理（如Nginx）时的IP获取问题。
 * 支持从多个HTTP头中获取IP地址，并按优先级处理，确保获取到真实的客户端IP。
 */
@Slf4j
public class IpUtils {

    /**
     * 获取客户端真实IP地址
     *
     * 当前端使用Nginx等反向代理时，不能通过request.getRemoteAddr()直接获取真实IP。
     * 本方法会依次尝试从以下HTTP头中获取IP地址：
     * 1. x-forwarded-for
     * 2. Proxy-Client-IP
     * 3. WL-Proxy-Client-IP
     * 4. HTTP_CLIENT_IP
     * 5. HTTP_X_FORWARDED_FOR
     * 6. request.getRemoteAddr()
     * @param request HttpServletRequest对象
     * @return 客户端真实IP地址，如果获取失败则返回null
     */
    public static String getIpAddr(HttpServletRequest request) {
        String unknown = "unknown";
        String ip = null;
        try {
            ip = request.getHeader("x-forwarded-for");
            if (StringUtils.isEmpty(ip) || unknown.equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (StringUtils.isEmpty(ip) || ip.isEmpty() || unknown.equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (StringUtils.isEmpty(ip) || unknown.equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_CLIENT_IP");
            }
            if (StringUtils.isEmpty(ip) || unknown.equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            }
            if (StringUtils.isEmpty(ip) || unknown.equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.error("IPUtils ERROR ", e);
        }
        return ip;
    }

}
