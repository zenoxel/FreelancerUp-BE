package com.FreelancerUp.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(HttpStatus.CONFLICT)
public class ConflictException extends RuntimeException {

    private final String resource;
    private final String field;
    private final Object value;

    public ConflictException(String message) {
        super(message);
        this.resource = null;
        this.field = null;
        this.value = null;
    }

    public ConflictException(String resource, String field, Object value) {
        super(String.format("%s already exists with %s : '%s'", resource, field, value));
        this.resource = resource;
        this.field = field;
        this.value = value;
    }
}
