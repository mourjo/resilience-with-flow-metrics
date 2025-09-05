package me.mourjo.conduit.nls.server.api;

import static me.mourjo.conduit.commons.constants.Headers.CLIENT_REQUEST_KEY_HEADER;
import static me.mourjo.conduit.commons.constants.Headers.CLIENT_REQUEST_TS_HEADER;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Random;
import lombok.SneakyThrows;
import me.mourjo.conduit.commons.PropertiesFileReader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

    private final MeterRegistry meterRegistry;
    private final PropertiesFileReader propertiesFileReader;

    Random r = new Random();

    public Controller(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        propertiesFileReader = new PropertiesFileReader();
    }

    @Timed(value = "conduit.http.server.processing.time", extraTags = {"uri", "/hello", "method",
        "get"})
    @GetMapping("/hello")
    public Map<String, String> hello(
        @RequestHeader(value = CLIENT_REQUEST_TS_HEADER, defaultValue = "-1") String requestTimestamp,
        @RequestHeader(value = CLIENT_REQUEST_KEY_HEADER, defaultValue = "-1") String requestId) {

        return doSomeComputation(requestId, requestTimestamp);
    }

    @SneakyThrows
    private Map<String, String> doSomeComputation(String requestId, String requestTimestamp) {
        long clientRequestTimestamp = Long.parseLong(requestTimestamp);
        if (clientRequestTimestamp > 0) {
            Instant clientInstant = Instant.ofEpochMilli(clientRequestTimestamp);
            meterRegistry.timer(
                "conduit.http.server.processing.delay",
                "uri", "/hello",
                "method", "get"
            ).record(Duration.between(clientInstant, Instant.now()));
        }

        int processingTime = propertiesFileReader.getServerProcessingTimeMillis();
        int jitter = r.nextInt(1000);
        Thread.sleep(processingTime + jitter);
        return Map.of(
            "message", "Hello from NLS Server! This request took %d ms of processing.".formatted(processingTime + jitter),
            "request_id", requestId
        );
    }
}
