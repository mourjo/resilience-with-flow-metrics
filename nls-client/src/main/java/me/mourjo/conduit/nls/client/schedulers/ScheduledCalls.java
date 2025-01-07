package me.mourjo.conduit.nls.client.schedulers;

import io.micrometer.core.instrument.MeterRegistry;
import me.mourjo.conduit.commons.ClientCalls;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;


@EnableScheduling
@Configuration
public class ScheduledCalls extends ClientCalls {


    public ScheduledCalls(MeterRegistry meterRegistry) {
        super(meterRegistry, "http://localhost:8080");
    }

    @Scheduled(fixedRate = 10000)
    public void scheduledCalls() {
        makeRequests();
    }
}
