package me.mourjo.conduit.api;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

    @GetMapping("/hello")
    public Map<String, String> hello(){
        return Map.of("message", "hello world");
    }
}
