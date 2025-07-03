package com.gigapress.dynamicupdate.health;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class Neo4jHealthIndicator implements HealthIndicator {
    
    private final Driver neo4jDriver;
    
    public Neo4jHealthIndicator(Driver neo4jDriver) {
        this.neo4jDriver = neo4jDriver;
    }
    
    @Override
    public Health health() {
        try (Session session = neo4jDriver.session()) {
            session.run("RETURN 1").consume();
            return Health.up()
                    .withDetail("database", "Neo4j")
                    .withDetail("status", "Connected")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("database", "Neo4j")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
