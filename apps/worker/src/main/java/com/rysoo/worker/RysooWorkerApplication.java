package com.rysoo.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.rysoo")
@EntityScan(basePackages = "com.rysoo.infrastructure.persistence.entity")
@EnableJpaRepositories(basePackages = "com.rysoo.infrastructure.persistence.repository")
@EnableScheduling
public class RysooWorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(RysooWorkerApplication.class, args);
    }
}
