package hackathon.fridgeai.repository;

import hackathon.fridgeai.entity.ExpiryAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpiryAlertRepository extends JpaRepository<ExpiryAlert, Long> {
    List<ExpiryAlert> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);
}