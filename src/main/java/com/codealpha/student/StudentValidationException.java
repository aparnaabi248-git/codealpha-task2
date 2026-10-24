package com.codealpha.student;

/**
 * Custom runtime exception thrown when student validation fails.
 */
public class StudentValidationException extends RuntimeException {
    public StudentValidationException(String message) {
        super(message);
    }
}
