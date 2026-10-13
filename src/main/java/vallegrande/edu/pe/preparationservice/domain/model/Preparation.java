package vallegrande.edu.pe.preparationservice.domain.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Preparation {
    private Long id;
    private Long requestId;
    private String type;                  // TITULAR | SPONSORS
    private LocalDate sessionDate;
    private String status;                // PENDING | COMPLETED | ABSENT
    private String attendanceStatus;      // PRESENT | ABSENT | EXCUSED
    private BigDecimal paymentAmount;
    private String paymentStatus;         // PENDING | PAID | PARTIAL
    private LocalDate paymentDate;
    private String paymentReceiptUrl;
    private Integer examScore;            // 0-100 para Confirmación
    private Boolean examPassed;           // true si aprobó el examen
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String tenantId;
}
