package com.dp.order_service.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    private static final String TRACE_ID_KEY = "traceId";
    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    @Bean
    RequestInterceptor feignTraceInterceptor(){
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                String traceId = MDC.get(TRACE_ID_KEY);
                if (traceId != null && !traceId.isBlank()) {
                    template.header(TRACE_ID_HEADER, traceId);
                }
            }
        };
    }
}
