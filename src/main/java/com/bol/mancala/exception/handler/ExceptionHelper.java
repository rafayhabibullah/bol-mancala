package com.bol.mancala.exception.handler;

import com.bol.mancala.exception.model.EntityNotFoundException;
import com.bol.mancala.exception.model.ExceptionDetail;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.validation.ConstraintViolationException;

@ControllerAdvice
public class ExceptionHelper {
    @ExceptionHandler(value = {EntityNotFoundException.class})
    public ResponseEntity<Object> handleEntityNotFoundException(EntityNotFoundException ex) {
        return new ResponseEntity<>(new ExceptionDetail(HttpStatus.NOT_FOUND, ex), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = {ConstraintViolationException.class})
    public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex) {
        return new ResponseEntity<>(new ExceptionDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(value = {MissingServletRequestParameterException.class})
    public ResponseEntity<Object> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        return new ResponseEntity<>(new ExceptionDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<Object> handleException(Exception ex) {
        return new ResponseEntity<>(new ExceptionDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
