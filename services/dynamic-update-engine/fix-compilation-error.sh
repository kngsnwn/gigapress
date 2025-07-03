#!/bin/bash

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}🔧 Fixing compilation errors...${NC}"

# Fix GlobalExceptionHandler.java
echo -e "${YELLOW}📝 Fixing GlobalExceptionHandler type mismatch...${NC}"

cat > src/main/java/com/gigapress/dynamicupdate/exception/GlobalExceptionHandler.java << 'EOF'
package com.gigapress.dynamicupdate.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.KafkaException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ComponentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleComponentNotFound(
            ComponentNotFoundException ex, WebRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Component Not Found")
                .message(ex.getMessage())
                .path(request.getDescription(false))
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(CircularDependencyException.class)
    public ResponseEntity<ErrorResponse> handleCircularDependency(
            CircularDependencyException ex, WebRequest request) {
        Map<String, Object> details = new HashMap<>();
        details.put("sourceComponent", ex.getSourceComponentId());
        details.put("targetComponent", ex.getTargetComponentId());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Circular Dependency Detected")
                .message(ex.getMessage())
                .path(request.getDescription(false))
                .details(details)
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }
    
    @ExceptionHandler(DependencyConflictException.class)
    public ResponseEntity<ErrorResponse> handleDependencyConflict(
            DependencyConflictException ex, WebRequest request) {
        Map<String, Object> details = new HashMap<>();
        details.put("conflictingComponents", ex.getConflictingComponents());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Dependency Conflict")
                .message(ex.getMessage())
                .path(request.getDescription(false))
                .details(details)
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, Object> validationErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Invalid request parameters")
                .path(request.getDescription(false))
                .details(validationErrors)
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(KafkaException.class)
    public ResponseEntity<ErrorResponse> handleKafkaException(
            KafkaException ex, WebRequest request) {
        log.error("Kafka error occurred", ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .error("Message Processing Error")
                .message("Unable to process message queue operation")
                .path(request.getDescription(false))
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, WebRequest request) {
        log.error("Unexpected error occurred", ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred")
                .path(request.getDescription(false))
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
EOF

echo -e "${GREEN}✅ GlobalExceptionHandler fixed!${NC}"

# Also check and fix any import issues in other files
echo -e "${YELLOW}📝 Checking other potential issues...${NC}"

# Fix missing imports in UpdateEventListener if needed
if [ -f "src/main/java/com/gigapress/dynamicupdate/event/UpdateEventListener.java" ]; then
    # Check if imports are missing
    if ! grep -q "import java.time.LocalDateTime;" src/main/java/com/gigapress/dynamicupdate/event/UpdateEventListener.java; then
        sed -i '1a\
import java.time.LocalDateTime;\
import java.util.List;\
import java.util.Map;' src/main/java/com/gigapress/dynamicupdate/event/UpdateEventListener.java
    fi
fi

# Fix missing validation imports in DTOs
if [ -f "src/main/java/com/gigapress/dynamicupdate/dto/ComponentRequest.java" ]; then
    if ! grep -q "import javax.validation.constraints.NotBlank;" src/main/java/com/gigapress/dynamicupdate/dto/ComponentRequest.java; then
        sed -i '1a\
import javax.validation.constraints.NotBlank;\
import javax.validation.constraints.NotNull;' src/main/java/com/gigapress/dynamicupdate/dto/ComponentRequest.java
    fi
fi

# Fix missing imports in controller
if [ -f "src/main/java/com/gigapress/dynamicupdate/controller/ComponentController.java" ]; then
    if ! grep -q "import java.util.List;" src/main/java/com/gigapress/dynamicupdate/controller/ComponentController.java; then
        sed -i '/package com.gigapress.dynamicupdate.controller;/a\
\
import java.util.List;\
import java.util.Map;\
import java.util.Set;' src/main/java/com/gigapress/dynamicupdate/controller/ComponentController.java
    fi
fi

echo -e "${GREEN}✅ All compilation issues should be fixed!${NC}"
echo ""
echo "🚀 Now try building again:"
echo "  ./gradlew build"