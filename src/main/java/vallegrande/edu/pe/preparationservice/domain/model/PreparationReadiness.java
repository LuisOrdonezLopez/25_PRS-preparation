package vallegrande.edu.pe.preparationservice.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreparationReadiness {
    private boolean allSessionsCompleted;
    private boolean tariffAvailable;
    private boolean canProceed;
    private PreparationTariff tariff;
}
