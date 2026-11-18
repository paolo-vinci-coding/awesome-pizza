package com.awesomepizza.web;

import com.awesomepizza.exception.InvalidOrderStateException;
import com.awesomepizza.exception.OrderNotFoundException;
import com.awesomepizza.exception.PizzaNotFoundException;
import com.awesomepizza.exception.PizzaioloBusyException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ProblemDetail handleOrderNotFound(OrderNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Order not found", ex.getMessage());
    }

    @ExceptionHandler(PizzaNotFoundException.class)
    public ProblemDetail handlePizzaNotFound(PizzaNotFoundException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid pizza", ex.getMessage());
    }

    @ExceptionHandler({InvalidOrderStateException.class, PizzaioloBusyException.class})
    public ProblemDetail handleConflict(RuntimeException ex) {
        return problem(HttpStatus.CONFLICT, "Operation not allowed", ex.getMessage());
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ProblemDetail handleConcurrentUpdate(ObjectOptimisticLockingFailureException ex) {
        return problem(HttpStatus.CONFLICT, "Concurrent update",
                "The order was modified by another request, please retry");
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Invalid request",
                "One or more fields are invalid");
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        problem.setProperty("errors", errors);
        return ResponseEntity.badRequest().body(problem);
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }
}
