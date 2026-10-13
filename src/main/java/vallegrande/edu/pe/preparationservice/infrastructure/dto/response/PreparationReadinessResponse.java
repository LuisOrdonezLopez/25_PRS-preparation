package vallegrande.edu.pe.preparationservice.infrastructure.dto.response;

public record PreparationReadinessResponse(
        boolean allSessionsCompleted,
        boolean tariffAvailable,
        boolean canProceed,
        PreparationTariffResponse tariff
) {
}
