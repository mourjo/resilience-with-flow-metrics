package me.mourjo.conduit.nls.client.schedulers;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import me.mourjo.conduit.commons.ClientCalls;
import me.mourjo.conduit.nls.client.ClientInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestClient;


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
