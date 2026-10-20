package com.rysoo.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.rysoo"})
@EntityScan(basePackages = {"com.rysoo.infrastructure.persistence.entity"})
@EnableJpaRepositories(basePackages = {"com.rysoo.infrastructure.persistence.repository"})
public class RysooApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(RysooApiApplication.class, args);
    }
}
