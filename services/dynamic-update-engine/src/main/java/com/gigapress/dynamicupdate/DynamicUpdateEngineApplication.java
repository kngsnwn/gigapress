package com.gigapress.dynamicupdate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableNeo4jRepositories
@EnableKafka
@EnableCaching
public class DynamicUpdateEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(DynamicUpdateEngineApplication.class, args);
    }
}
