package me.mourjo.conduit.commons;

import io.micrometer.core.instrument.MeterRegistry;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

public class ClientInterceptor implements ClientHttpRequestInterceptor {

    protected final MeterRegistry meterRegistry;

    public ClientInterceptor(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
        ClientHttpRequestExecution execution) throws IOException {
        String uri = request.getURI().toString();
        String method = request.getMethod().name();
        Instant start = Instant.now();

        try {
            meterRegistry.counter("http.client.requests.initiated", "uri", uri,
                "method", method).increment(1d);

            ClientHttpResponse response = execution.execute(request, body);

            meterRegistry.timer("http.client.requests",
                    "uri", uri,
                    "method", method,
                    "status", response.getStatusText())
                .record(Duration.between(start, Instant.now()));

            return response;
        } catch (IOException e) {
            meterRegistry.timer("http.client.requests",
                    "uri", uri,
                    "method", method,
                    "status", "error")
                .record(Duration.between(start, Instant.now()));
            throw e;
        } finally {
            meterRegistry.counter("http.client.requests.completed", "uri", uri,
                "method", method).increment(1d);
        }
    }
}
