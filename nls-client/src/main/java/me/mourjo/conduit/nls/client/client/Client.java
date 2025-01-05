package me.mourjo.conduit.nls.client.client;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestClient;

@EnableScheduling
@Configuration
public class Client {

    @Scheduled(fixedRate = 100)
    public void requests() {
        RestClient restClient = RestClient.builder()
            .baseUrl("http://localhost:8080")
            .build();

        String response = restClient.get()
            .uri("/hello")
            .retrieve()
            .body(String.class);

        System.out.println("got response: " + response);

    }

}
