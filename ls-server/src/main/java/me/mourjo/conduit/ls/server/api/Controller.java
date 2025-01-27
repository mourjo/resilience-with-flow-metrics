package me.mourjo.conduit.ls.server.api;

import static me.mourjo.conduit.commons.constants.Headers.CLIENT_REQUEST_KEY_HEADER;
import static me.mourjo.conduit.commons.constants.Headers.CLIENT_REQUEST_TS_HEADER;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import me.mourjo.conduit.commons.PropertiesFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

@RestController
public class Controller {

    protected static final Logger logger = LoggerFactory.getLogger(Controller.class);

    private final Executor executor;

    private final MeterRegistry meterRegistry;
    private final PropertiesFileReader propertiesFileReader;
    Random r = new Random();

    public Controller(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        propertiesFileReader = new PropertiesFileReader();

        var queue = new LinkedBlockingQueue<Runnable>(30);

        Gauge.builder("http.server.queue.size", queue, Collection::size)
            .register(meterRegistry);

        executor = new ThreadPoolExecutor(
            5,              // core pool size
            5,              // minimum pool size
            0L,             // keep alive
            TimeUnit.MILLISECONDS,
            queue
        );
    }

    private static ResponseEntity<Map<String, String>> tooManyRequests() {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
    }

    private static ResponseEntity<Map<String, String>> gatewayTimeout() {
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).build();
    }

    private static void timeoutHandler(String requestId,
        DeferredResult<ResponseEntity<Map<String, String>>> result) {
        logger.error("Request(id={}) did not finish processing in time", requestId);
        result.setResult(gatewayTimeout());
    }

    @GetMapping("/hello")
    public DeferredResult<ResponseEntity<Map<String, String>>> hello(
        @RequestHeader(value = CLIENT_REQUEST_TS_HEADER, defaultValue = "-1") String requestTimestamp,
        @RequestHeader(value = CLIENT_REQUEST_KEY_HEADER, defaultValue = "-1") String requestId) {
        return handoffRequest(requestTimestamp, requestId);
    }

    private DeferredResult<ResponseEntity<Map<String, String>>> handoffRequest(
        String requestTimestamp,
        String requestId) {

        long enqueueTime = System.currentTimeMillis();

        var result = new DeferredResult<ResponseEntity<Map<String, String>>>(10_000L);
        result.onTimeout(() -> timeoutHandler(requestId, result));

        try {
            executor.execute(() -> {
                measureDelay(requestTimestamp);

                long dequeueTime = System.currentTimeMillis();
                if (dequeueTime - enqueueTime > 5000) {
                    result.setResult(tooManyRequests());
                    return;
                }

                var response = doSomeComputation(requestId, requestTimestamp);
                result.setResult(ResponseEntity.ok(response));
            });
        } catch (RejectedExecutionException e) {
            logger.error("Request(id={}) could not be processed due to too many requests",
                requestId);
            result.setResult(tooManyRequests());
        }

        return result;
    }

    @SneakyThrows
    private Map<String, String> doSomeComputation(String requestId, String requestTimestamp) {
        var start = Instant.now();
        try {
            int processingTime = propertiesFileReader.getServerProcessingTimeMillis();
            int jitter = r.nextInt(1000);
            Thread.sleep(processingTime + jitter);
            return Map.of(
                "message", "Hello from LS Server!",
                "request_id", requestId
            );

        } finally {
            meterRegistry.timer(
                "conduit.http.server.processing.time",
                "uri", "/hello",
                "method", "get"
            ).record(Duration.between(start, Instant.now()));
        }
    }

    private void measureDelay(String requestTimestamp) {
        long clientRequestTimestamp = Long.parseLong(requestTimestamp);
        if (clientRequestTimestamp > 0) {
            Instant clientInstant = Instant.ofEpochMilli(clientRequestTimestamp);
            meterRegistry.timer(
                "conduit.http.server.processing.delay",
                "uri", "/hello",
                "method", "get"
            ).record(Duration.between(clientInstant, Instant.now()));
        }
    }
}
