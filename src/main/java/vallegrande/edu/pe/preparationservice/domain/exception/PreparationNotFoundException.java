package vallegrande.edu.pe.preparationservice.domain.exception;

public class PreparationNotFoundException extends RuntimeException {
    public PreparationNotFoundException(Long id) {
        super("Preparation session not found: " + id);
    }
}
