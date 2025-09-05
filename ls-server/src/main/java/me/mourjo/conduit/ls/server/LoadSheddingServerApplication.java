package me.mourjo.conduit.ls.server;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(
    title = "LS Server",
    version = "v1.0",
    description = "An example of a load shedding server"
))
public class LoadSheddingServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoadSheddingServerApplication.class, args);
    }

}
