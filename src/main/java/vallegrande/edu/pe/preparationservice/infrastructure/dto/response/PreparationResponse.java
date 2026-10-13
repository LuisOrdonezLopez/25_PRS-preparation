package vallegrande.edu.pe.preparationservice.infrastructure.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PreparationResponse(
        Long id,
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
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String tenantId
) {
}
