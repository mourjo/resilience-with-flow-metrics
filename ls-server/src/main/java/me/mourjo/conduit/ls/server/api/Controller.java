package me.mourjo.conduit.ls.server.api;

import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

@RestController
public class Controller {

    final ExecutorService executor = Executors.newFixedThreadPool(5);
    private final MeterRegistry meterRegistry;
    Random r = new Random();

    public Controller(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @GetMapping("/hello")
    public DeferredResult<Map<String, String>> hello(@RequestHeader(value = "X-Client-Request-Timestamp-Millis", defaultValue = "-1") String requestTimestamp) {

        return submit(requestTimestamp);
    }

    private DeferredResult<Map<String, String>> submit(String requestTimestamp) {

        var result = new DeferredResult<Map<String, String>>(10000L);
        executor.submit(() -> {
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
                Thread.sleep(4000 + r.nextInt(1000));
                result.setResult(Map.of("message", "Hello from LS Server!"));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                meterRegistry.timer("conduit.http.server.processing.time",
                        "uri", "/hello",
                        "method", "get")
                    .record(Duration.between(start, Instant.now()));
            }

        });

        return result;
    }
}
