package vallegrande.edu.pe.preparationservice.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/** Current amount returned by vg-ms-tarifas for a sacramental request. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreparationTariff {
    private boolean available;
    private UUID tariffId;
    private UUID sacramentId;
    private String sacramentName;
    private BigDecimal amount;
    private String currency;
    private Boolean donation;
    private String status;
    private String message;

    public static PreparationTariff unavailable(String message) {
        return PreparationTariff.builder()
                .available(false)
                .message(message)
                .build();
    }
}
