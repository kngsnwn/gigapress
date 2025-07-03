package com.gigapress.dynamicupdate.exception;

import java.util.Set;

public class DependencyConflictException extends RuntimeException {
    private final Set<String> conflictingComponents;
    
    public DependencyConflictException(String message, Set<String> conflictingComponents) {
        super(message);
        this.conflictingComponents = conflictingComponents;
    }
    
    public Set<String> getConflictingComponents() {
        return conflictingComponents;
    }
}
