package com.gigapress.domainschema;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableKafka
@EnableCaching
@EnableAsync
@EnableScheduling
public class DomainSchemaServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DomainSchemaServiceApplication.class, args);
    }
}
