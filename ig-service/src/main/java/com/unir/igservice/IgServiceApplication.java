package com.unir.igservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = {"com.unir.common.entity"})
@EnableJpaRepositories(basePackages = {"com.unir.igservice.repository"})
public class IgServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(IgServiceApplication.class, args);
    }
}

