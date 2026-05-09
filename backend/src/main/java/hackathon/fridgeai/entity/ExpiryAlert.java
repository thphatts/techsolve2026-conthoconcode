package hackathon.fridgeai.entity;

import hackathon.fridgeai.enums.AlertType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "expiry_alerts", indexes = {
                @Index(name = "idx_expiry_alerts_user", columnList = "user_id, is_read")
})
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ExpiryAlert extends BaseEntity {

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "fridge_item_id", nullable = false)
        private FridgeItem fridgeItem;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "user_id", nullable = false)
        private User user; // Người sẽ nhận thông báo trên App

        @Enumerated(EnumType.STRING)
        @Column(name = "alert_type", nullable = false, length = 30)
        private AlertType alertType;

        @Column(name = "message", nullable = false, length = 255)
        private String message; // Nội dung gửi cho user (VD: "Thịt bò của bạn đã hỏng, bạn bị trừ 10 điểm!")

        @Column(name = "is_read", nullable = false)
        @Builder.Default
        private Boolean isRead = false; // Khi frontend lấy thông báo xong có thể gọi API đổi cái này thành true để ẩn
                                        // đi
}