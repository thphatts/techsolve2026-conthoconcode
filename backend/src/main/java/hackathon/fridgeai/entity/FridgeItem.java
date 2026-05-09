package hackathon.fridgeai.entity;

import hackathon.fridgeai.enums.FridgeItemStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "fridge_items", indexes = {
        @Index(name = "idx_fridge_items_status", columnList = "fridge_id, status"),
        @Index(name = "idx_fridge_items_expires", columnList = "expires_at"),
        @Index(name = "idx_fridge_items_product", columnList = "product_id")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = { "alerts", "gamificationLogs", "fridge", "product", "addedBy" })
@ToString(callSuper = true, exclude = { "alerts", "gamificationLogs", "fridge", "product", "addedBy" })
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class FridgeItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fridge_id", nullable = false)
    private Fridge fridge;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "added_by", nullable = false)
    private User addedBy;

    @Column(nullable = false)
    @Builder.Default
    private Double quantity = 1.0;

    @Column(name = "purchase_price", precision = 10, scale = 2)
    private BigDecimal purchasePrice;

    @Column(name = "purchased_at", nullable = false)
    @Builder.Default
    private LocalDate purchasedAt = LocalDate.now();

    @Column(name = "expires_at", nullable = false)
    private LocalDate expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private FridgeItemStatus status = FridgeItemStatus.FRESH;

    // --- Relationships ---

    @OneToMany(mappedBy = "fridgeItem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ExpiryAlert> alerts = new ArrayList<>();

    @OneToMany(mappedBy = "fridgeItem", fetch = FetchType.LAZY)
    @Builder.Default
    private List<GamificationLog> gamificationLogs = new ArrayList<>();

    // --- Domain helpers ---

    public long getDaysUntilExpiry() {
        // FIX: Dùng ChronoUnit để tính tổng số ngày chính xác thay vì getDays()
        return ChronoUnit.DAYS.between(LocalDate.now(), expiresAt);
    }

    public boolean isExpired() {
        return expiresAt.isBefore(LocalDate.now());
    }
}