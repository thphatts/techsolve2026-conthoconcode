package hackathon.fridgeai.controller;

import hackathon.fridgeai.dto.FridgeResponse;
import hackathon.fridgeai.entity.Fridge;
import hackathon.fridgeai.entity.FridgeItem;
import hackathon.fridgeai.entity.User;
import hackathon.fridgeai.repository.FridgeItemRepository;
import hackathon.fridgeai.repository.UserRepository;
import hackathon.fridgeai.service.FridgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/fridges")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Tạm mở CORS cho quá trình thi Hackathon
public class FridgeController {

    private final FridgeItemRepository fridgeItemRepository;
    private final FridgeService fridgeService;
    private final UserRepository userRepository;

    // API để tạo tủ lạnh mới (Sử dụng Principal để lấy user từ Token)
    @PostMapping
    public ResponseEntity<?> createFridge(Principal principal) {
        try {
            User user = userRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Fridge savedFridge = fridgeService.createFridge(user.getId());

            // Chuyển đổi Entity sang DTO để tránh vòng lặp JSON
            return ResponseEntity.ok(FridgeResponse.builder()
                    .id(savedFridge.getId())
                    .name(savedFridge.getName())
                    .ownerId(user.getId())
                    .ownerName(user.getName())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // API lấy danh sách thực phẩm trong một tủ lạnh cụ thể
    @GetMapping("/{fridgeId}/items")
    public ResponseEntity<List<FridgeItem>> getItemsInFridge(@PathVariable Long fridgeId) {
        List<FridgeItem> items = fridgeItemRepository.findByFridgeId(fridgeId);
        return ResponseEntity.ok(items);
    }

}