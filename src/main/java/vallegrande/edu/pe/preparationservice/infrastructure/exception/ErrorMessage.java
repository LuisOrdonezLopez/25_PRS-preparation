package vallegrande.edu.pe.preparationservice.infrastructure.exception;

import java.time.Instant;

public record ErrorMessage(int status, String error, String message, String path, Instant timestamp) {
}
