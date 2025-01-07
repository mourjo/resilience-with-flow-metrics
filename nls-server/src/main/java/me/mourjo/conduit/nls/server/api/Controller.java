package me.mourjo.conduit.nls.server.api;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

    Random r = new Random();

    @GetMapping("/hello")
    public Map<String, String> hello() {
        try {
            Thread.sleep(4000 + r.nextInt(1000));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return Map.of("message", "Hello from NLS Server!");
    }
}
