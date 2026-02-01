package org.smm.archetype.adapter.web.config;

import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.infrastructure.bizshared.util.context.MyContext;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 上下文填充过滤器，设置请求上下文信息。
 */
@Slf4j
public class ContextFillFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain) {
        MyContext.runWithUserId("ADMIN", () -> {
            try {
                filterChain.doFilter(request, response);
            } catch (IOException | ServletException e) {
                log.error("Error occurred while processing request", e);
                throw new RuntimeException(e);
            }
        });
    }

}
