package hackathon.fridgeai.controller;

import hackathon.fridgeai.entity.BillItem;
import hackathon.fridgeai.entity.FridgeItem;
import hackathon.fridgeai.service.BillService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/bills")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BillController {

    private final BillService billService;

    // API Lấy danh sách các món đồ AI đã quét ra từ 1 hóa đơn
    @GetMapping("/{billId}/items")
    public ResponseEntity<List<BillItem>> getBillItems(@PathVariable Long billId) {
        return ResponseEntity.ok(billService.getItemsByBill(billId));
    }

    // API Xác nhận món đồ từ hóa đơn và chính thức đưa vào Tủ lạnh
    @PostMapping("/items/{billItemId}/confirm")
    public ResponseEntity<FridgeItem> confirmBillItem(
            @PathVariable Long billItemId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiresAt) {
        return ResponseEntity.ok(billService.confirmBillItem(billItemId, expiresAt));
    }
}