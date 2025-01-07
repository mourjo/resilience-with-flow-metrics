package me.mourjo.conduit.ls.client;

import io.micrometer.core.instrument.MeterRegistry;
import me.mourjo.conduit.commons.client.ClientRequests;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;


@EnableScheduling
@Configuration
public class ScheduledRequests extends ClientRequests {


    public ScheduledRequests(MeterRegistry meterRegistry) {
        super(meterRegistry, "http://localhost:7070");
    }

    @Scheduled(fixedRate = 10000)
    public void scheduledCalls() {
        requestBatch();
    }
}
