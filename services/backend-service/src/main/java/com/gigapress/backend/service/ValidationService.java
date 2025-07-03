package com.gigapress.backend.service;

import com.gigapress.backend.dto.ApiSpecification;
import com.gigapress.backend.dto.BusinessLogicRequest;
import com.gigapress.backend.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ValidationService {

    public void validateApiSpecification(ApiSpecification spec) {
        List<String> errors = new ArrayList<>();
        
        if (spec.getApiName() == null || spec.getApiName().isEmpty()) {
            errors.add("API name is required");
        }
        
        if (spec.getEntityName() == null || spec.getEntityName().isEmpty()) {
            errors.add("Entity name is required");
        }
        
        if (spec.getPackageName() == null || spec.getPackageName().isEmpty()) {
            errors.add("Package name is required");
        }
        
        if (!errors.isEmpty()) {
            throw new ValidationException("API specification validation failed", errors);
        }
    }

    public void validateBusinessLogicRequest(BusinessLogicRequest request) {
        List<String> errors = new ArrayList<>();
        
        if (request.getEntityName() == null || request.getEntityName().isEmpty()) {
            errors.add("Entity name is required");
        }
        
        if (request.getPatternType() == null) {
            errors.add("Pattern type is required");
        }
        
        if (request.getFields() == null || request.getFields().isEmpty()) {
            errors.add("At least one field is required");
        }
        
        // Validate fields
        if (request.getFields() != null) {
            for (int i = 0; i < request.getFields().size(); i++) {
                BusinessLogicRequest.FieldDefinition field = request.getFields().get(i);
                if (field.getName() == null || field.getName().isEmpty()) {
                    errors.add("Field name is required for field at index " + i);
                }
                if (field.getType() == null || field.getType().isEmpty()) {
                    errors.add("Field type is required for field " + field.getName());
                }
            }
        }
        
        if (!errors.isEmpty()) {
            throw new ValidationException("Business logic request validation failed", errors);
        }
    }
}
