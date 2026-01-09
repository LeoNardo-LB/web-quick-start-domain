package org.smm.archetype.adapter.access.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.smm.archetype.infrastructure._shared.context.ContextHolder;
import org.smm.archetype.infrastructure._shared.context.impl.AccessContext;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 *
 *
 * @author Leonardo
 * @since 2025/12/30
 */
public class ContextFillFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        AccessContext accessContext = AccessContext.builder().setUserId("ADMIN").build();
        try {
            ContextHolder.createContext(accessContext);
            filterChain.doFilter(request, response);
        } finally {
            ContextHolder.clear();
        }

    }

}
