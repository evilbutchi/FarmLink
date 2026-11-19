package com.farmlink.exception;

import java.util.Map;

public class FieldValidationException extends RuntimeException {

    private final transient Map<String, String> errors;

    public FieldValidationException(Map<String, String> errors) {
        super("Validation failed");
        this.errors = errors;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
