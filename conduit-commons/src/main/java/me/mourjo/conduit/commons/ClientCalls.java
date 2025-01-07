package me.mourjo.conduit.commons;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClient;


public abstract class ClientCalls {

    protected final ExecutorService executorService;

    protected final MeterRegistry meterRegistry;
    protected final int DEFAULT_CONCURRENCY = 1;
    protected final String CONCURRENCY_SETTING_FILE_PATH = "../common_concurrent_requests.txt";

    protected static final Logger logger = LoggerFactory.getLogger(ClientCalls.class);
    protected final RestClient restClient;

    protected final AtomicInteger inflightCounter;
    protected final AtomicInteger concurrencyGauge;


    public ClientCalls(MeterRegistry meterRegistry, String baseUrl) {
        this.restClient = RestClient.builder()
            .requestInterceptor(new ClientInterceptor(meterRegistry))
            .baseUrl(baseUrl)
            .build();

        this.inflightCounter = new AtomicInteger(0);

        this.meterRegistry = meterRegistry;
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();
        concurrencyGauge = new AtomicInteger();

        Gauge.builder("http.client.requests.concurrency", concurrencyGauge, AtomicInteger::get)
            .description("A custom gauge example")
            .register(meterRegistry);

    }

    protected final void makeRequests() {
        int requestCount = concurrency();
        logger.info("Firing %d requests (already in flight %d)".formatted(requestCount,
            inflightCounter.get()));
        for (int i = 0; i < requestCount; i++) {
            executorService.submit(this::fireRequest);
        }
    }

    protected int concurrency() {
        Scanner scanner = null;
        int result = DEFAULT_CONCURRENCY;
        try {
            File file = Paths.get(CONCURRENCY_SETTING_FILE_PATH).toFile();
            scanner = new Scanner(file);

            if (scanner.hasNextInt()) {
                result = scanner.nextInt();
            }

        } catch (FileNotFoundException e) {
            logger.error("File not found: " + CONCURRENCY_SETTING_FILE_PATH);
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
        concurrencyGauge.set(result);
        return result;
    }

    private void fireRequest() {
        try {
            inflightCounter.incrementAndGet();
            String response = restClient.get()
                .uri("/hello")
                .retrieve()
                .body(String.class);

            logger.info("Response from server: %s".formatted(response));
        } catch (Exception e) {
            logger.error("Failed to get response", e);
        } finally {
            inflightCounter.decrementAndGet();
        }
    }
}
