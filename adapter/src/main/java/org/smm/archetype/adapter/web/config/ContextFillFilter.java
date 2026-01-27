package org.smm.archetype.adapter.web.config;

import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.smm.archetype.infrastructure.bizshared.context.ContextHolder;
import org.smm.archetype.infrastructure.bizshared.context.impl.AccessContext;
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
    protected void doFilterInternal(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull FilterChain filterChain)
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
