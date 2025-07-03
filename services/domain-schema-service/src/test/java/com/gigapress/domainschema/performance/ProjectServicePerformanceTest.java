package com.gigapress.domainschema.performance;

import com.gigapress.domainschema.IntegrationTestBase;
import com.gigapress.domainschema.domain.analysis.dto.request.CreateProjectRequest;
import com.gigapress.domainschema.domain.analysis.service.ProjectService;
import com.gigapress.domainschema.domain.common.entity.ProjectType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectServicePerformanceTest extends IntegrationTestBase {
    
    @Autowired
    private ProjectService projectService;
    
    @Test
    void createProjects_ConcurrentRequests_ShouldHandleLoad() throws Exception {
        // Given
        int numberOfThreads = 10;
        int projectsPerThread = 5;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        List<Future<List<String>>> futures = new ArrayList<>();
        
        // When
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            Future<List<String>> future = executor.submit(() -> {
                List<String> projectIds = new ArrayList<>();
                try {
                    for (int j = 0; j < projectsPerThread; j++) {
                        CreateProjectRequest request = CreateProjectRequest.builder()
                                .name("Perf Test Project " + threadId + "-" + j)
                                .projectType(ProjectType.WEB_APPLICATION)
                                .build();
                        
                        String projectId = projectService.createProject(request).getProjectId();
                        projectIds.add(projectId);
                    }
                } finally {
                    latch.countDown();
                }
                return projectIds;
            });
            futures.add(future);
        }
        
        // Wait for all threads to complete
        latch.await(30, TimeUnit.SECONDS);
        long endTime = System.currentTimeMillis();
        
        // Then
        List<String> allProjectIds = new ArrayList<>();
        for (Future<List<String>> future : futures) {
            allProjectIds.addAll(future.get());
        }
        
        assertThat(allProjectIds).hasSize(numberOfThreads * projectsPerThread);
        
        long totalTime = endTime - startTime;
        double avgTimePerProject = (double) totalTime / (numberOfThreads * projectsPerThread);
        
        System.out.println("Performance Test Results:");
        System.out.println("Total projects created: " + allProjectIds.size());
        System.out.println("Total time: " + totalTime + "ms");
        System.out.println("Average time per project: " + avgTimePerProject + "ms");
        
        assertThat(avgTimePerProject).isLessThan(100); // Should create each project in less than 100ms
        
        executor.shutdown();
    }
}
