package com.dp.order_service.util;

import io.micrometer.tracing.Tracer;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.Objects;

@UtilityClass
@Slf4j
public class TraceIdUtil {

    private static final String TRACE_ID_KEY = "traceId";

    public String getTraceId() {
        return MDC.get(TRACE_ID_KEY);
    }

    public void setTraceId(String traceId) {
        MDC.put(TRACE_ID_KEY, traceId);
    }

    public void clear() {
        MDC.remove(TRACE_ID_KEY);
    }
}
