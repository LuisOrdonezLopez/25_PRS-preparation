package vallegrande.edu.pe.preparationservice.infrastructure.mapper;

import vallegrande.edu.pe.preparationservice.domain.model.Preparation;
import vallegrande.edu.pe.preparationservice.domain.model.PreparationReadiness;
import vallegrande.edu.pe.preparationservice.domain.model.PreparationTariff;
import vallegrande.edu.pe.preparationservice.infrastructure.dto.request.PreparationRequest;
import vallegrande.edu.pe.preparationservice.infrastructure.dto.response.PreparationReadinessResponse;
import vallegrande.edu.pe.preparationservice.infrastructure.dto.response.PreparationResponse;
import vallegrande.edu.pe.preparationservice.infrastructure.dto.response.PreparationTariffResponse;

public final class PreparationDtoMapper {

    private PreparationDtoMapper() {
    }

    public static Preparation toDomain(PreparationRequest source) {
        Preparation target = new Preparation();
        target.setRequestId(source.requestId());
        target.setType(source.type());
        target.setSessionDate(source.sessionDate());
        target.setStatus(source.status());
        target.setAttendanceStatus(source.attendanceStatus());
        target.setPaymentAmount(source.paymentAmount());
        target.setPaymentStatus(source.paymentStatus());
        target.setPaymentDate(source.paymentDate());
        target.setPaymentReceiptUrl(source.paymentReceiptUrl());
        target.setExamScore(source.examScore());
        target.setExamPassed(source.examPassed());
        target.setNotes(source.notes());
        target.setTenantId(source.tenantId());
        return target;
    }

    public static PreparationResponse toResponse(Preparation source) {
        return new PreparationResponse(source.getId(), source.getRequestId(), source.getType(),
                source.getSessionDate(), source.getStatus(), source.getAttendanceStatus(),
                source.getPaymentAmount(), source.getPaymentStatus(), source.getPaymentDate(),
                source.getPaymentReceiptUrl(), source.getExamScore(), source.getExamPassed(),
                source.getNotes(), source.getCreatedAt(), source.getUpdatedAt(), source.getTenantId());
    }

    public static PreparationTariffResponse toResponse(PreparationTariff source) {
        return new PreparationTariffResponse(source.isAvailable(), source.getTariffId(), source.getSacramentId(),
                source.getSacramentName(), source.getAmount(), source.getCurrency(), source.getDonation(),
                source.getStatus(), source.getMessage());
    }

    public static PreparationReadinessResponse toResponse(PreparationReadiness source) {
        return new PreparationReadinessResponse(source.isAllSessionsCompleted(), source.isTariffAvailable(),
                source.isCanProceed(), toResponse(source.getTariff()));
    }
}
