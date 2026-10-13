package vallegrande.edu.pe.preparationservice.infrastructure.adapter.out.persistence;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @Table("preparation")
public class PreparationEntity {
    @Id private Long id;
    @Column("request_id")           private Long requestId;
    private String type;
    @Column("session_date")         private LocalDate sessionDate;
    private String status;
    @Column("attendance_status")    private String attendanceStatus;
    @Column("payment_amount")       private BigDecimal paymentAmount;
    @Column("payment_status")       private String paymentStatus;
    @Column("payment_date")         private LocalDate paymentDate;
    @Column("payment_receipt_url")  private String paymentReceiptUrl;
    @Column("exam_score")           private Integer examScore;
    @Column("exam_passed")          private Boolean examPassed;
    private String notes;
    @Column("created_at")           private LocalDateTime createdAt;
    @Column("updated_at")           private LocalDateTime updatedAt;
    @Column("tenant_id")            private String tenantId;
}
