package com.gigapress.domainschema.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gigapress.domainschema.IntegrationTestBase;
import com.gigapress.domainschema.domain.analysis.dto.request.*;
import com.gigapress.domainschema.domain.common.entity.ProjectType;
import com.gigapress.domainschema.domain.common.entity.RequirementPriority;
import com.gigapress.domainschema.domain.common.entity.RequirementType;
import com.gigapress.domainschema.integration.mcp.client.McpServerClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProjectLifecycleE2ETest extends IntegrationTestBase {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private McpServerClient mcpServerClient;
    
    @Test
    void completeProjectLifecycle_FromCreationToRequirements() throws Exception {
        // Step 1: Create Project
        CreateProjectRequest createRequest = CreateProjectRequest.builder()
                .name("E2E Test E-Commerce Platform")
                .description("Complete e-commerce platform with all features")
                .projectType(ProjectType.WEB_APPLICATION)
                .build();
        
        String createResponse = mockMvc.perform(post("/api/v1/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        String projectId = objectMapper.readTree(createResponse)
                .path("data")
                .path("projectId")
                .asText();
        
        // Step 2: Add Manual Requirement
        AddRequirementRequest addReqRequest = AddRequirementRequest.builder()
                .title("User Authentication")
                .description("Users must be able to register and login securely")
                .type(RequirementType.FUNCTIONAL)
                .priority(RequirementPriority.CRITICAL)
                .build();
        
        mockMvc.perform(post("/api/v1/requirements/{projectId}", projectId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addReqRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
        
        // Step 3: Analyze Natural Language Requirements
        AnalyzeRequirementsRequest analyzeRequest = AnalyzeRequirementsRequest.builder()
                .projectId(projectId)
                .naturalLanguageRequirements(
                    "The platform should support product catalog with categories. " +
                    "Customers should be able to add items to cart and checkout. " +
                    "Payment processing should support credit cards and PayPal. " +
                    "Order tracking and email notifications are required.")
                .constraints(Arrays.asList(
                    "Must be PCI compliant",
                    "Support 10,000 concurrent users",
                    "Mobile responsive design"))
                .build();
        
        // Mock MCP Server response
        Map<String, Object> mockAnalysis = createMockAnalysisResponse();
        Mockito.when(mcpServerClient.analyzeRequirements(any())).thenReturn(mockAnalysis);
        
        mockMvc.perform(post("/api/v1/requirements/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(analyzeRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.requirements").isArray());
        
        // Step 4: Verify All Requirements
        mockMvc.perform(get("/api/v1/requirements/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(5)); // 1 manual + 4 analyzed
        
        // Step 5: Verify Project Status Updated
        mockMvc.perform(get("/api/v1/projects/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DESIGNING"))
                .andExpect(jsonPath("$.data.requirementCount").value(5));
    }
    
    private Map<String, Object> createMockAnalysisResponse() {
        Map<String, Object> response = new HashMap<>();
        response.put("summary", "E-commerce platform with product catalog, cart, checkout, and order management");
        
        List<Map<String, Object>> requirements = new ArrayList<>();
        
        // Requirement 1
        Map<String, Object> req1 = new HashMap<>();
        req1.put("title", "Product Catalog");
        req1.put("description", "Support product catalog with hierarchical categories");
        req1.put("type", "FUNCTIONAL");
        req1.put("priority", "HIGH");
        req1.put("metadata", Map.of("component", "catalog"));
        requirements.add(req1);
        
        // Requirement 2
        Map<String, Object> req2 = new HashMap<>();
        req2.put("title", "Shopping Cart");
        req2.put("description", "Add items to cart with quantity management");
        req2.put("type", "FUNCTIONAL");
        req2.put("priority", "HIGH");
        req2.put("metadata", Map.of("component", "cart"));
        requirements.add(req2);
        
        // Requirement 3
        Map<String, Object> req3 = new HashMap<>();
        req3.put("title", "Payment Processing");
        req3.put("description", "Integrate credit card and PayPal payment methods");
        req3.put("type", "FUNCTIONAL");
        req3.put("priority", "CRITICAL");
        req3.put("metadata", Map.of("component", "payment", "compliance", "PCI"));
        requirements.add(req3);
        
        // Requirement 4
        Map<String, Object> req4 = new HashMap<>();
        req4.put("title", "Order Tracking");
        req4.put("description", "Track order status with email notifications");
        req4.put("type", "FUNCTIONAL");
        req4.put("priority", "MEDIUM");
        req4.put("metadata", Map.of("component", "orders"));
        requirements.add(req4);
        
        response.put("requirements", requirements);
        response.put("identifiedEntities", Arrays.asList(
            "User", "Product", "Category", "Cart", "CartItem", 
            "Order", "OrderItem", "Payment", "Notification"
        ));
        response.put("suggestedRelationships", Arrays.asList(
            "User-Cart", "Cart-CartItem", "CartItem-Product",
            "User-Order", "Order-OrderItem", "OrderItem-Product",
            "Order-Payment", "Order-Notification"
        ));
        response.put("technologyRecommendations", Map.of(
            "frontend", "React with Redux",
            "backend", "Spring Boot",
            "database", "PostgreSQL",
            "payment", "Stripe API",
            "notifications", "SendGrid"
        ));
        response.put("confidenceScore", 0.92);
        
        return response;
    }
}
