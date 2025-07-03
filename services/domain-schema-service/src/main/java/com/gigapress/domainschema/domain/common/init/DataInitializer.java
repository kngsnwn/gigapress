package com.gigapress.domainschema.domain.common.init;

import com.gigapress.domainschema.domain.common.entity.ProjectType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DataInitializer implements CommandLineRunner {
    
    @Override
    public void run(String... args) throws Exception {
        log.info("Initializing development data...");
        
        // Initialize code templates
        initializeCodeTemplates();
        
        // Initialize sample projects (optional)
        // initializeSampleProjects();
        
        log.info("Development data initialization completed");
    }
    
    private void initializeCodeTemplates() {
        log.info("Initializing code templates...");
        // Template initialization will be implemented in the next step
    }
}
