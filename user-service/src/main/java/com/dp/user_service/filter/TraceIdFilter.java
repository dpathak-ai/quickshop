package com.dp.user_service.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(1)                   // runs before everything else
@Slf4j
public class TraceIdFilter implements Filter {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String TRACE_ID_MDC_KEY = "traceId";

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            // Check if traceId came from an upstream service
            String traceId = httpRequest.getHeader(TRACE_ID_HEADER);

            // If no upstream traceId — generate a new one
            if (traceId == null || traceId.isBlank()) {
                traceId = generateTraceId();
            }

            // Put traceId into MDC — now ALL log lines in this thread include it
            MDC.put(TRACE_ID_MDC_KEY, traceId);

            // Pass traceId back in response header so caller can reference it
            httpResponse.setHeader(TRACE_ID_HEADER, traceId);

            log.debug("TraceId set: {}", traceId);

            chain.doFilter(request, response);

        } finally {
            // ALWAYS clear MDC after request completes
            // Prevents traceId leaking into next request on same thread
            MDC.clear();
        }
    }

    private String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}