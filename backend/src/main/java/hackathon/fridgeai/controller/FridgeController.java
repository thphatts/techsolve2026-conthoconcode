package hackathon.fridgeai.controller;

import hackathon.fridgeai.dto.BulkItemRequest;
import hackathon.fridgeai.service.FridgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/fridges")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FridgeController {

    private final FridgeService fridgeService;

    @PostMapping("/{fridgeId}/items/bulk")
    public ResponseEntity<?> bulkAddItems(
            @PathVariable Long fridgeId,
            @RequestParam Long userId,
            @RequestBody List<BulkItemRequest> requests) {

        fridgeService.addBulkItems(fridgeId, userId, requests);
        return ResponseEntity.ok().body("{\"message\": \"Lưu thành công!\"}");
    }

    // --- API: Lấy danh sách sản phẩm trong tủ lạnh ---
    @GetMapping("/{fridgeId}/items")
    public ResponseEntity<?> getFridgeItems(@PathVariable Long fridgeId) {
        return ResponseEntity.ok(fridgeService.getItemsInFridge(fridgeId));
    }

    // --- API: Đánh dấu đã sử dụng sản phẩm (Ăn/Nấu) ---
    @PostMapping("/items/{itemId}/consume")
    public ResponseEntity<?> consumeItem(
            @PathVariable Long itemId,
            @RequestParam Long userId) {
        return ResponseEntity.ok(fridgeService.consumeItem(itemId, userId));
    }
}