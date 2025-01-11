package me.mourjo.conduit.commons.client;

import static me.mourjo.conduit.commons.constants.Headers.CLIENT_REQUEST_KEY_HEADER;
import static me.mourjo.conduit.commons.constants.Headers.CLIENT_REQUEST_TS_HEADER;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import me.mourjo.conduit.commons.PropertiesFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClient;


public abstract class ClientRequests {

    protected static final Logger logger = LoggerFactory.getLogger(ClientRequests.class);
    protected final ExecutorService executorService;
    protected final MeterRegistry meterRegistry;
    protected final RestClient restClient;
    protected final PropertiesFileReader propertiesFileReader;

    protected final AtomicInteger inFlightCounter;
    protected final AtomicInteger concurrencyGauge;
    protected final AtomicInteger requestIdGenerator;

    public ClientRequests(MeterRegistry meterRegistry, String baseUrl) {
        restClient = RestClient.builder()
            .requestInterceptor(new ClientInterceptor(meterRegistry))
            .baseUrl(baseUrl)
            .build();

        propertiesFileReader = new PropertiesFileReader();
        inFlightCounter = new AtomicInteger(0);
        this.meterRegistry = meterRegistry;
        executorService = Executors.newVirtualThreadPerTaskExecutor();
        concurrencyGauge = new AtomicInteger();
        requestIdGenerator = new AtomicInteger(1);

        Gauge.builder("http.client.requests.concurrency", concurrencyGauge, AtomicInteger::get)
            .register(meterRegistry);

    }

    protected final void requestBatch() {
        int requestCount = concurrency();
        logger.info("Firing %d requests (already in flight %d)".formatted(requestCount,
            inFlightCounter.get()));
        for (int i = 0; i < requestCount; i++) {
            executorService.submit(this::fireRequest);
        }
    }

    protected int concurrency() {
        int result = propertiesFileReader.getClientConcurrency();
        concurrencyGauge.set(result);
        return result;
    }

    private void fireRequest() {
        try {
            inFlightCounter.incrementAndGet();
            String response = restClient.get()
                .uri("/hello")
                .header(CLIENT_REQUEST_TS_HEADER, String.valueOf(Instant.now().toEpochMilli()))
                .header(CLIENT_REQUEST_KEY_HEADER,
                    String.valueOf(requestIdGenerator.incrementAndGet()))
                .retrieve()
                .body(String.class);

            logger.info("Response from server: %s".formatted(response));
        } catch (Exception e) {
            logger.error("Failed to get response", e);
        } finally {
            inFlightCounter.decrementAndGet();
        }
    }
}
