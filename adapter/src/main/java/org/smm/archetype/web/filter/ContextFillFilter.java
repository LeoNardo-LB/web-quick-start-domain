package org.smm.archetype.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.smm.archetype.common.context.ContextHolder;
import org.smm.archetype.dto.UserAuthInfo;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 *
 *
 * @author Leonardo
 * @since 2025/12/30
 */
@Component
public class ContextFillFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        UserAuthInfo data = new UserAuthInfo();
        data.setUserId("admin12312");
        try {
            ContextHolder.createContext(data);
            filterChain.doFilter(request, response);
        } finally {
            ContextHolder.clear();
        }

    }

}
