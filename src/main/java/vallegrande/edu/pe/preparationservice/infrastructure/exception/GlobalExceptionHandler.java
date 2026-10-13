package vallegrande.edu.pe.preparationservice.infrastructure.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import vallegrande.edu.pe.preparationservice.domain.exception.PreparationNotFoundException;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PreparationNotFoundException.class)
    public ResponseEntity<ErrorMessage> notFound(PreparationNotFoundException exception, ServerWebExchange exchange) {
        return response(HttpStatus.NOT_FOUND, exception.getMessage(), exchange);
    }

    @ExceptionHandler({IllegalArgumentException.class, WebExchangeBindException.class})
    public ResponseEntity<ErrorMessage> badRequest(Exception exception, ServerWebExchange exchange) {
        return response(HttpStatus.BAD_REQUEST, exception.getMessage(), exchange);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorMessage> conflict(DataIntegrityViolationException exception, ServerWebExchange exchange) {
        return response(HttpStatus.CONFLICT, "La operación viola una restricción de datos", exchange);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessage> unexpected(Exception exception, ServerWebExchange exchange) {
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servicio", exchange);
    }

    private ResponseEntity<ErrorMessage> response(HttpStatus status, String message, ServerWebExchange exchange) {
        return ResponseEntity.status(status).body(new ErrorMessage(status.value(), status.getReasonPhrase(), message,
                exchange.getRequest().getPath().value(), Instant.now()));
    }
}
