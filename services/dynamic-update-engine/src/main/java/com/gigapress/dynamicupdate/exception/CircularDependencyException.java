package com.gigapress.dynamicupdate.exception;

public class CircularDependencyException extends RuntimeException {
    private final String sourceComponentId;
    private final String targetComponentId;
    
    public CircularDependencyException(String sourceComponentId, String targetComponentId) {
        super(String.format("Circular dependency detected between %s and %s", 
                sourceComponentId, targetComponentId));
        this.sourceComponentId = sourceComponentId;
        this.targetComponentId = targetComponentId;
    }
    
    public String getSourceComponentId() {
        return sourceComponentId;
    }
    
    public String getTargetComponentId() {
        return targetComponentId;
    }
}
