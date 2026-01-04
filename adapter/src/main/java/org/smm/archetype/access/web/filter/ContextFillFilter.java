package org.smm.archetype.access.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.smm.archetype.shared.base.BaseEvent.Source;
import org.smm.archetype.shared.context.ContextHolder;
import org.smm.archetype.shared.context.impl.AccessContext;
import org.smm.archetype.shared.context.impl.AccessContext.AccessContextBuilder;
import org.smm.archetype.common.event.AccessEvent;
import org.smm.archetype.common.event.EventFactory;
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
        AccessEvent accessEvent = EventFactory.createAccessEvent(Source.ACCESS_WEB, "ADMIN");
        AccessContextBuilder<?, ?> builder = AccessContext.builder();
        builder.setEvent(accessEvent);
        try {
            ContextHolder.createContext(builder.build());
            filterChain.doFilter(request, response);
        } finally {
            ContextHolder.clear();
        }

    }

}
