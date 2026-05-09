package hackathon.fridgeai.repository;

import hackathon.fridgeai.entity.ExpiryAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpiryAlertRepository extends JpaRepository<ExpiryAlert, Long> {

    // Tìm tất cả các thông báo chưa đọc của 1 User (Dùng cho API hiện chuông thông
    // báo trên Frontend)
    List<ExpiryAlert> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);
}