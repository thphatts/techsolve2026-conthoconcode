package hackathon.fridgeai.controller;

import hackathon.fridgeai.entity.FridgeItem;
import hackathon.fridgeai.repository.FridgeItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/fridges")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Tạm mở CORS cho quá trình thi Hackathon
public class FridgeController {

    private final FridgeItemRepository fridgeItemRepository;

    // API lấy danh sách thực phẩm trong một tủ lạnh cụ thể
    @GetMapping("/{fridgeId}/items")
    public ResponseEntity<List<FridgeItem>> getItemsInFridge(@PathVariable Long fridgeId) {
        List<FridgeItem> items = fridgeItemRepository.findByFridgeId(fridgeId);
        return ResponseEntity.ok(items);
    }

}