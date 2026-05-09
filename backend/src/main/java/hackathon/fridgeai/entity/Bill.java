package hackathon.fridgeai.entity;

import hackathon.fridgeai.enums.BillStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "bills", indexes = {
        @Index(name = "idx_bills_fridge", columnList = "fridge_id"),
        @Index(name = "idx_bills_user", columnList = "user_id"),
        @Index(name = "idx_bills_status", columnList = "status")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = { "items" }) // Chống vòng lặp vô tận
@ToString(callSuper = true, exclude = { "items" })
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Bill extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fridge_id", nullable = false)
    private Fridge fridge;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Nâng độ dài lên 512 vì link ảnh (S3, Firebase) thường rất dài
    @Column(name = "image_url", nullable = false, length = 512)
    private String imageUrl;

    /**
     * Raw JSON returned by AI vision model.
     * Sử dụng tính năng Native JSON của Hibernate 6.
     * Tự động map với cột 'json' trong MySQL.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ai_raw_json", columnDefinition = "json")
    private Map<String, Object> aiRawJson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private BillStatus status = BillStatus.PENDING;

    @Column(name = "scanned_at", nullable = false)
    @Builder.Default
    private Instant scannedAt = Instant.now();

    // --- Relationships ---

    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<BillItem> items = new ArrayList<>();
}