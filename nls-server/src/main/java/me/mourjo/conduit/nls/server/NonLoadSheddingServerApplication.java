package me.mourjo.conduit.nls.server;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(
    title = "NLS Server",
    version = "v1.0",
    description = "An example of a non-load shedding server"
))
public class NonLoadSheddingServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(NonLoadSheddingServerApplication.class, args);
    }

}
