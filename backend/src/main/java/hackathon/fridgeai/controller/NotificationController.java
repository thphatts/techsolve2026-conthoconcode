package hackathon.fridgeai.controller;

import hackathon.fridgeai.entity.ExpiryAlert;
import hackathon.fridgeai.repository.ExpiryAlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NotificationController {

    private final ExpiryAlertRepository expiryAlertRepository;

    // API lấy các thông báo chưa đọc của một User
    @GetMapping("/users/{userId}/unread")
    public ResponseEntity<List<ExpiryAlert>> getUnreadNotifications(@PathVariable Long userId) {
        List<ExpiryAlert> unreadAlerts = expiryAlertRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        return ResponseEntity.ok(unreadAlerts);
    }
}