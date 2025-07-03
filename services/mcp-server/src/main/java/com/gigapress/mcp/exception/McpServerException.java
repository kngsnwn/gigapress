package com.gigapress.mcp.exception;

public class McpServerException extends RuntimeException {
    
    private final String errorCode;
    
    public McpServerException(String message) {
        super(message);
        this.errorCode = "MCP_ERROR";
    }
    
    public McpServerException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public McpServerException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "MCP_ERROR";
    }
    
    public McpServerException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
