package com.karam.teamup.player;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
@OpenAPIDefinition(info = @Info())
public class PlayerServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PlayerServiceApplication.class, args);
    }
}