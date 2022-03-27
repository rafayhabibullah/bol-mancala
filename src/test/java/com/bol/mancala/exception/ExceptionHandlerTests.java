package com.bol.mancala.exception;

import com.bol.mancala.exception.handler.ExceptionHelper;
import com.bol.mancala.exception.model.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExceptionHandlerTests {

    ExceptionHelper exceptionHelper;

    @BeforeEach
    void setUp() {
        exceptionHelper = new ExceptionHelper();
    }

    @Test
    void testHandleEntityNotFoundException() {
        EntityNotFoundException e = new EntityNotFoundException("game not found");
        ResponseEntity<Object> errorResponse = exceptionHelper.handleEntityNotFoundException(e);
        assertEquals(HttpStatus.NOT_FOUND, errorResponse.getStatusCode());
    }

    @Test
    void testHandleConstraintViolationException() {
        Set<? extends ConstraintViolation<?>> constraintViolations = new HashSet<>();
        String message = "sow.pitIndex: must be greater than or equal to 1";
        ConstraintViolationException e = new ConstraintViolationException(message, constraintViolations);
        ResponseEntity<Object> errorResponse = exceptionHelper.handleConstraintViolationException(e);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, errorResponse.getStatusCode());
    }

    @Test
    void testhandleMissingServletRequestParameterException() {
        MissingServletRequestParameterException e = new MissingServletRequestParameterException("firstPlayerName", "String");
        ResponseEntity<Object> errorResponse = exceptionHelper.handleMissingServletRequestParameterException(e);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, errorResponse.getStatusCode());
    }

    @Test
    void testHandleException() {
        ResponseEntity<Object> errorResponse = exceptionHelper.handleException(new NullPointerException());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, errorResponse.getStatusCode());
    }
}
