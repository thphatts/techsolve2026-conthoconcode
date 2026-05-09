package hackathon.fridgeai.controller;

import hackathon.fridgeai.entity.GamificationLog;
import hackathon.fridgeai.entity.User;
import hackathon.fridgeai.repository.GamificationLogRepository;
import hackathon.fridgeai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UserRepository userRepository;
    private final GamificationLogRepository gamificationLogRepository;

    // Lấy Profile User (Bao gồm Tổng điểm TotalPoints và Tiền ảo)
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserProfile(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy User"));
    }

    // Lấy lịch sử Gamification (Lịch sử cộng/trừ điểm để hiển thị Timeline)
    @GetMapping("/{id}/gamification-logs")
    public ResponseEntity<List<GamificationLog>> getUserGamificationLogs(@PathVariable Long id) {
        List<GamificationLog> logs = gamificationLogRepository.findByUserIdOrderByCreatedAtDesc(id);
        return ResponseEntity.ok(logs);
    }
}