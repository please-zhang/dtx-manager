package com.meets.demo.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 集成入口。
 */
@SpringBootApplication(scanBasePackages = "com.meets.demo")
public class DemoSpringBootApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoSpringBootApplication.class, args);
    }
}
