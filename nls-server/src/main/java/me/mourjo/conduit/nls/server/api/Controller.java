package me.mourjo.conduit.nls.server.api;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

    private final MeterRegistry meterRegistry;
    Random r = new Random();

    public Controller(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @GetMapping("/hello")
    public Map<String, String> hello() {
        var start = Instant.now();
        try {
            Thread.sleep(4000 + r.nextInt(1000));
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
