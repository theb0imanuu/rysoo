package com.rysoo.worker.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rysoo.docprocessing.DocumentIngestionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WorkerConfig {

    @Bean
    public DocumentIngestionService documentIngestionService() {
        return new DocumentIngestionService();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
