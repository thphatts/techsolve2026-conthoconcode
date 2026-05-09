package hackathon.fridgeai.repository;

import hackathon.fridgeai.entity.GamificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GamificationLogRepository extends JpaRepository<GamificationLog, Long> {
    // Để sau này làm bảng xếp hạng hoặc lịch sử
    List<GamificationLog> findByUserIdOrderByCreatedAtDesc(Long userId);
}