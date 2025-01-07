package me.mourjo.api;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

@RestController
public class Controller {

    final ExecutorService executor = Executors.newFixedThreadPool(5);
    Random r = new Random();

    @GetMapping("/hello")
    public DeferredResult<Map<String, String>> hello(){

        return submit();
    }

    private DeferredResult<Map<String, String>> submit(){
        var result = new DeferredResult<Map<String, String>>();
        executor.submit(() -> {
            try {
                Thread.sleep(4000 + r.nextInt(1000));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            result.setResult(Map.of("message", "Hello from LS Server!"));
        });

        return result;
    }
}
