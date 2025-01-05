package me.mourjo.conduit.nls.client.schedulers;

import io.micrometer.core.instrument.MeterRegistry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import me.mourjo.conduit.nls.client.ClientInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestClient;

@EnableScheduling
@Configuration
public class ScheduledCalls {

    private final ExecutorService executorService;

    private final MeterRegistry meterRegistry;

    private final int CONCURRENCY = 30;

    public ScheduledCalls(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.executorService = Executors.newFixedThreadPool(CONCURRENCY);
    }

    @Scheduled(fixedRate = 100)
    public void requests() {
        for (int i = 0; i < CONCURRENCY; i++) {
            executorService.submit(this::dosomething);
        }

    }

    private void dosomething() {
        RestClient restClient = RestClient.builder()
            .requestInterceptor(new ClientInterceptor(meterRegistry))
            .baseUrl("http://localhost:8080")
            .build();

        String response = restClient.get()
            .uri("/hello")
            .retrieve()
            .body(String.class);

        System.out.println("got response: " + response);
    }

}
