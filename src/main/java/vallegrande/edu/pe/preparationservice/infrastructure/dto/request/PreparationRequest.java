package vallegrande.edu.pe.preparationservice.infrastructure.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PreparationRequest(
        Long requestId,
        String type,
        LocalDate sessionDate,
        String status,
        String attendanceStatus,
        BigDecimal paymentAmount,
        String paymentStatus,
        LocalDate paymentDate,
        String paymentReceiptUrl,
        Integer examScore,
        Boolean examPassed,
        String notes,
        String tenantId
) {
}
