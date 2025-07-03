package com.gigapress.dynamicupdate.exception;

public class ComponentNotFoundException extends RuntimeException {
    public ComponentNotFoundException(String message) {
        super(message);
    }
    
    public ComponentNotFoundException(String componentId, Throwable cause) {
        super("Component not found: " + componentId, cause);
    }
}
