package hackathon.fridgeai.job;

import hackathon.fridgeai.entity.*;
import hackathon.fridgeai.enums.AlertType;
import hackathon.fridgeai.enums.EventType;
import hackathon.fridgeai.enums.FridgeItemStatus;
import hackathon.fridgeai.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j // Dùng để in log hệ thống
public class ExpiryCheckJob {

    private final FridgeItemRepository fridgeItemRepository;
    private final GamificationLogRepository gamificationLogRepository;
    private final ExpiryAlertRepository expiryAlertRepository;
    private final UserRepository userRepository;

    // Lấy mức phạt từ application.yml, nếu không có mặc định là trừ 10 điểm
    @Value("${app.gamification.penalty-per-expired-item:10}")
    private Integer penaltyPoints;

    /**
     * Cron biểu thức "0 1 0 * * ?" nghĩa là: Chạy vào lúc 00:01:00 mỗi ngày.
     * 
     * @Transactional đảm bảo nếu đang trừ điểm bị lỗi DB thì nó sẽ Rollback, không
     *                ai bị trừ oan.
     */
    @Scheduled(cron = "0 1 0 * * ?")
    @Transactional
    public void checkAndProcessExpiredItems() {
        log.info("Bắt đầu Cron Job: Quét thực phẩm hết hạn trong Tủ Lạnh...");

        LocalDate today = LocalDate.now();

        // 1. Tìm tất cả đồ ăn đang tươi (FRESH) nhưng ngày hết hạn < hôm nay
        List<FridgeItem> expiredItems = fridgeItemRepository
                .findByStatusAndExpiresAtBefore(FridgeItemStatus.FRESH, today);

        if (expiredItems.isEmpty()) {
            log.info("Tuyệt vời! Không có thực phẩm nào bị hỏng trong hôm nay.");
            return;
        }

        int processedCount = 0;

        for (FridgeItem item : expiredItems) {
            // 2. Đổi trạng thái món đồ thành ĐÃ HỎNG (EXPIRED)
            item.setStatus(FridgeItemStatus.EXPIRED);

            User user = item.getAddedBy(); // Người đã thêm món này vào (Chủ sở hữu)

            // 3. Tính tiền phạt (Bằng đúng giá mua món đồ đó)
            BigDecimal penaltyMoney = item.getPurchasePrice() != null ? item.getPurchasePrice() : BigDecimal.ZERO;

            // 4. Ghi Log Gamification (Lưu lịch sử bị phạt)
            GamificationLog penaltyLog = GamificationLog.builder()
                    .user(user)
                    .fridgeItem(item)
                    .eventType(EventType.ITEM_EXPIRED)
                    .pointsDelta(-penaltyPoints) // Trừ điểm (Số âm)
                    .walletDelta(penaltyMoney.negate()) // Trừ tiền ảo (Chuyển thành số âm)
                    .build();
            if (penaltyLog != null) {
                gamificationLogRepository.save(penaltyLog);
            }

            // 5. Cập nhật lại Ví Tổng của User
            user.setTotalPoints((user.getTotalPoints() != null ? user.getTotalPoints() : 0) - penaltyPoints);

            BigDecimal currentBalance = user.getWalletBalance() != null ? user.getWalletBalance() : BigDecimal.ZERO;
            user.setWalletBalance(currentBalance.subtract(penaltyMoney));
            userRepository.save(user);

            // 6. Tạo Thông báo (Alert) để khi mở App lên user sẽ thấy
            ExpiryAlert alert = ExpiryAlert.builder()
                    .fridgeItem(item)
                    .user(user)
                    .alertType(AlertType.EXPIRED) // Đổi từ LOW_STOCK sang EXPIRED vì đồ ăn đã hỏng
                    .message("Rất tiếc! Món " + item.getProduct().getName() + " của bạn đã hỏng. Bạn bị trừ "
                            + penaltyPoints + " EXP.")
                    .build();
            if (alert != null) {
                expiryAlertRepository.save(alert);
            }

            processedCount++;
        }

        // Lưu lại hàng loạt các trạng thái món đồ vừa đổi
        fridgeItemRepository.saveAll(expiredItems);

        log.info("✅ Đã quét xong. Tổng số sản phẩm bị hỏng và đã xử lý phạt: {}", processedCount);
    }
}