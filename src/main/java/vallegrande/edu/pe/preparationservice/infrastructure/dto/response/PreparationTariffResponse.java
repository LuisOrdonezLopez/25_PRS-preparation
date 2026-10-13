package vallegrande.edu.pe.preparationservice.infrastructure.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record PreparationTariffResponse(
        boolean available,
        UUID tariffId,
        UUID sacramentId,
        String sacramentName,
        BigDecimal amount,
        String currency,
        Boolean donation,
        String status,
        String message
) {
}
