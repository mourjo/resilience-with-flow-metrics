package me.mourjo.conduit.nls.server.api;

import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Random;
import me.mourjo.conduit.commons.server.ProcessingTimeProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

    private final MeterRegistry meterRegistry;
    Random r = new Random();
    private final ProcessingTimeProvider processingTimeProvider;
    public Controller(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        processingTimeProvider = new ProcessingTimeProvider();
    }

    @GetMapping("/hello")
    public Map<String, String> hello(@RequestHeader(value = "X-Client-Request-Timestamp-Millis", defaultValue = "-1") String requestTimestamp) {
        var start = Instant.now();
        long clientRequestTimestamp = Long.parseLong(requestTimestamp);
        if (clientRequestTimestamp > 0) {
            Instant clientInstant = Instant.ofEpochMilli(clientRequestTimestamp);
            meterRegistry.timer("conduit.http.server.processing.delay",
                    "uri", "/hello",
                    "method", "get")
                .record(Duration.between(clientInstant, Instant.now()));
        }

        try {
            int processingTime = processingTimeProvider.readFromFile();
            int jitter = r.nextInt(1000);
            Thread.sleep(processingTime + jitter);
            return Map.of("message", "Hello from NLS Server!");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {

            meterRegistry.timer("conduit.http.server.processing.time",
                    "uri", "/hello",
                    "method", "get")
                .record(Duration.between(start, Instant.now()));

        }
    }
}
