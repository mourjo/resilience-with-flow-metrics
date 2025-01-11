package me.mourjo.conduit.ls.server.api;

import static me.mourjo.conduit.commons.constants.Headers.CLIENT_REQUEST_KEY_HEADER;
import static me.mourjo.conduit.commons.constants.Headers.CLIENT_REQUEST_TS_HEADER;

import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
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

        executor = new ThreadPoolExecutor(
            5,
            5,
            0L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(50)
        );
    }

    @GetMapping("/hello")
    public DeferredResult<ResponseEntity<Map<String, String>>> hello(
        @RequestHeader(value = CLIENT_REQUEST_TS_HEADER, defaultValue = "-1") String requestTimestamp,
        @RequestHeader(value = CLIENT_REQUEST_KEY_HEADER, defaultValue = "-1") String requestId) {
        return submit(requestTimestamp, requestId);
    }

    private DeferredResult<ResponseEntity<Map<String, String>>> submit(String requestTimestamp,
        String requestId) {
        long enqueueTime = System.currentTimeMillis();
        var result = new DeferredResult<ResponseEntity<Map<String, String>>>(10000L);

        result.onTimeout(
            () -> {
                logger.error(
                    "Request(id=%s) did not finish processing in time".formatted(requestId)
                );
                result.setResult(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build());
            }
        );

        try {
            executor.execute(() -> {
                var start = Instant.now();
                long clientRequestTimestamp = Long.parseLong(requestTimestamp);
                if (clientRequestTimestamp > 0) {
                    Instant clientInstant = Instant.ofEpochMilli(clientRequestTimestamp);
                    meterRegistry.timer(
                        "conduit.http.server.processing.delay",
                        "uri", "/hello",
                        "method", "get"
                    ).record(Duration.between(clientInstant, Instant.now()));
                }

                long dequeueTime = System.currentTimeMillis();
                long queueTime = dequeueTime - enqueueTime;

                // already waited too long
                if (queueTime > 5000) {
                    logger.error(
                        "Request(id=%s) waited too long, dropping it".formatted(requestId)
                    );
                    result.setResult(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build());
                    return;
                }

                try {
                    int processingTime = propertiesFileReader.getServerProcessingTimeMillis();
                    int jitter = r.nextInt(1000);
                    Thread.sleep(processingTime + jitter);
                    result.setResult(ResponseEntity.ok(
                        Map.of(
                            "message", "Hello from LS Server!",
                            "request_id", requestId
                        )));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    meterRegistry.timer("conduit.http.server.processing.time",
                            "uri", "/hello",
                            "method", "get")
                        .record(Duration.between(start, Instant.now()));
                }
            });
        } catch (RejectedExecutionException e) {
            logger.error("Request(id=%s) could not be processed due to too many requests".formatted(
                requestId));
            result.setResult(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build());
        }

        return result;
    }
}
