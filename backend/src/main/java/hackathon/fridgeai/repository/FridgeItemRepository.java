package hackathon.fridgeai.repository;

import hackathon.fridgeai.entity.FridgeItem;
import hackathon.fridgeai.enums.FridgeItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FridgeItemRepository extends JpaRepository<FridgeItem, Long> {

    /**
     * Hàm này được gọi trong FridgeService.getItemsInFridge()
     * Để lấy ra toàn bộ đồ ăn đang có trong một cái tủ lạnh cụ thể.
     */
    List<FridgeItem> findByFridgeId(Long fridgeId);

    /**
     * Hàm này cực kỳ quan trọng, được gọi trong ExpiryCheckJob.
     * Spring Data JPA sẽ tự động dịch tên hàm này thành câu SQL:
     * "SELECT * FROM fridge_items WHERE status = ? AND expires_at < ?"
     */
    List<FridgeItem> findByStatusAndExpiresAtBefore(FridgeItemStatus status, LocalDate date);
}