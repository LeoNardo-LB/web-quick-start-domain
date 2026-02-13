package org.smm.archetype.adapter.web.config;

import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.infrastructure.shared.util.context.ScopedThreadContext;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 上下文填充过滤器，设置请求上下文信息。
 * 自动为每个请求生成 traceId 用于追踪。
 */
@Slf4j
public class ContextFillFilter extends OncePerRequestFilter {

    /**
     * 请求头中的 traceId 键名（用于从上游服务传递）
     */
    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain) {
        // 获取或生成 traceId
        String traceId = resolveTraceId(request);
        
        // 设置响应头中的 traceId
        response.setHeader(TRACE_ID_HEADER, traceId);
        
        ScopedThreadContext.runWithContext("ADMIN", traceId, () -> {
            try {
                filterChain.doFilter(request, response);
            } catch (IOException | ServletException e) {
                log.error("Error occurred while processing request", e);
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 解析 traceId，优先从请求头获取，否则生成新的
     * @param request HTTP 请求
     * @return traceId
     */
    private String resolveTraceId(HttpServletRequest request) {
        // 优先从请求头获取（支持分布式追踪）
        String traceId = request.getHeader(TRACE_ID_HEADER);
        if (traceId == null || traceId.isEmpty()) {
            // 生成新的 traceId
            traceId = ScopedThreadContext.generateTraceId();
        }
        return traceId;
    }

}
