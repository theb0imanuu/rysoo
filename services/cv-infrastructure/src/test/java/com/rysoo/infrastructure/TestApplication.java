package com.rysoo.infrastructure;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.rysoo.infrastructure")
@EntityScan(basePackages = "com.rysoo.infrastructure.persistence.entity")
@EnableJpaRepositories(basePackages = "com.rysoo.infrastructure.persistence.repository")
public class TestApplication {
}
