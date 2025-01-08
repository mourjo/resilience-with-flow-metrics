package me.mourjo.api;

import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

@RestController
public class Controller {

    final ExecutorService executor = Executors.newFixedThreadPool(5);
    Random r = new Random();
    private final MeterRegistry meterRegistry;

    public Controller(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @GetMapping("/hello")
    public DeferredResult<Map<String, String>> hello() {

        return submit();
    }

    private DeferredResult<Map<String, String>> submit() {

        var result = new DeferredResult<Map<String, String>>(10000L);
        executor.submit(() -> {
            var start = Instant.now();
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
