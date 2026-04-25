package com.dp.order_service.config;

import io.micrometer.tracing.handler.DefaultTracingObservationHandler;
import io.micrometer.tracing.otel.bridge.OtelCurrentTraceContext;
import io.micrometer.tracing.otel.bridge.OtelTracer;
import io.micrometer.tracing.otel.bridge.Slf4JEventListener;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TracingConfig {

    @Value("${management.otlp.tracing.endpoint:http://localhost:4318/v1/traces}")
    private String otlpEndpoint;

    @Value("${management.tracing.sampling.probability:1.0}")
    private double samplingProbability;

    @Value("${spring.application.name:unknown-service}")
    private String applicationName;

    @Bean
    public OtlpHttpSpanExporter otlpHttpSpanExporter() {
        return OtlpHttpSpanExporter.builder()
                .setEndpoint(otlpEndpoint)
                .build();
    }

    @Bean
    public SdkTracerProvider sdkTracerProvider(OtlpHttpSpanExporter spanExporter) {
        Resource resource = Resource.getDefault().merge(
                Resource.create(Attributes.of(AttributeKey.stringKey("service.name"), applicationName)));

        Sampler sampler = samplingProbability >= 1.0
                ? Sampler.alwaysOn()
                : Sampler.traceIdRatioBased(samplingProbability);

        return SdkTracerProvider.builder()
                .addSpanProcessor(BatchSpanProcessor.builder(spanExporter).build())
                .setSampler(sampler)
                .setResource(resource)
                .build();
    }

    @Bean
    public OpenTelemetry openTelemetry(SdkTracerProvider sdkTracerProvider) {
        return OpenTelemetrySdk.builder()
                .setTracerProvider(sdkTracerProvider)
                .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                .build();
    }

    @Bean
    public OtelCurrentTraceContext otelCurrentTraceContext() {
        return new OtelCurrentTraceContext();
    }

    @Bean
    public Slf4JEventListener slf4JEventListener() {
        return new Slf4JEventListener();
    }

    @Bean
    public OtelTracer micrometerOtelTracer(OpenTelemetry openTelemetry,
                                           OtelCurrentTraceContext otelCurrentTraceContext,
                                           Slf4JEventListener slf4JEventListener) {
        io.opentelemetry.api.trace.Tracer otelApiTracer =
                openTelemetry.getTracer("io.micrometer.micrometer-tracing");
        return new OtelTracer(otelApiTracer, otelCurrentTraceContext,
                event -> slf4JEventListener.onEvent(event));
    }

    @Bean
    public DefaultTracingObservationHandler defaultTracingObservationHandler(OtelTracer micrometerOtelTracer) {
        return new DefaultTracingObservationHandler(micrometerOtelTracer);
    }
}
