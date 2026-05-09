package hackathon.fridgeai.entity;

import hackathon.fridgeai.enums.EventType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "gamification_logs", indexes = {
        @Index(name = "idx_gamification_user", columnList = "user_id")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = { "user", "fridgeItem" })
@ToString(callSuper = true, exclude = { "user", "fridgeItem" })
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder // <--- BẮT BUỘC PHẢI CÓ ĐỂ DÙNG .builder()
public class GamificationLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fridge_item_id", nullable = false)
    private FridgeItem fridgeItem; // <--- TÊN BIẾN NÀY QUYẾT ĐỊNH LỆNH .fridgeItem()

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private EventType eventType;

    @Column(name = "points_delta", nullable = false)
    private Integer pointsDelta;

    @Column(name = "wallet_delta", precision = 10, scale = 2)
    private BigDecimal walletDelta;
}