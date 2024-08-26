package io.github.poshjosh.ratelimiter.tests.server;

import io.github.poshjosh.ratelimiter.tests.server.services.MessageService;
import io.github.poshjosh.ratelimiter.tests.server.util.logging.LogMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler(value = { MessageService.MessageException.class })
    protected ResponseEntity<Object> handleMessageException(
            MessageService.MessageException ex, WebRequest request) {
        return handleException(ex, request, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(value = { IllegalArgumentException.class, IllegalStateException.class })
    protected ResponseEntity<Object> handleConflict(RuntimeException ex, WebRequest request) {
        return handleException(ex, request, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(value = { Exception.class })
    protected ResponseEntity<Object> handleOtherExceptions(Exception ex, WebRequest request) {
        final HttpStatus status;
        if (ex instanceof ResponseStatusException) {
            status = ((ResponseStatusException) ex).getStatus();
        } else if (ex instanceof RestClientResponseException) {
            status = HttpStatus.valueOf(((RestClientResponseException) ex).getRawStatusCode());
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return handleException(ex, request, status);
    }

    private ResponseEntity<Object> handleException(Exception ex, WebRequest req, HttpStatus status) {
        if (log.isWarnEnabled()) {
            log.warn(ex.toString());
        }
        return handleExceptionInternal(ex, LogMessages.getAndClear(), new HttpHeaders(), status, req);
    }
}