package com.dp.user_service.config;


import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class FeignTraceInterceptor implements RequestInterceptor {
    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    @Override
    public void apply(RequestTemplate template) {
        // Read current traceId from MDC
        String traceId = MDC.get("traceId");

        if (traceId != null) {
            // Inject it into outgoing Feign request header
            template.header(TRACE_ID_HEADER, traceId);
        }
    }
}