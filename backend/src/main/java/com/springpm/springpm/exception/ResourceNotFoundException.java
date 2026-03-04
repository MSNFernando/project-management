package com.springpm.springpm.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public static ResourceNotFoundException ofType(Class<?> type, String id) {
        return new ResourceNotFoundException(type.getSimpleName() + " not found with id: " + id);
    }
}
