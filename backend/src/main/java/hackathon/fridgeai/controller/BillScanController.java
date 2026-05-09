package hackathon.fridgeai.controller;

import hackathon.fridgeai.dto.AiReceiptResponse;
import hackathon.fridgeai.service.AiVisionService;
import hackathon.fridgeai.service.BillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;

@RestController
@RequestMapping("/api/v1/scan")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // Tạm mở cho React test local
public class BillScanController {

    private final AiVisionService aiVisionService;
    private final BillService billService;

    @PostMapping("/receipt")
    public ResponseEntity<AiReceiptResponse> uploadReceipt(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId,
            @RequestParam("fridgeId") Long fridgeId) {
        try {
            // 1. Chuyển file ảnh thành Base64
            String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
            String mimeType = file.getContentType();

            if (mimeType == null || !mimeType.startsWith("image/")) {
                return ResponseEntity.badRequest().build();
            }

            // 2. Gửi qua Service xử lý AI
            AiReceiptResponse response = aiVisionService.analyzeReceipt(base64Image, mimeType);

            // 3. Gọi BillService để lưu hóa đơn vào Database
            billService.processAndSaveAiReceipt(userId, fridgeId, file.getOriginalFilename(), response, null);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi hệ thống khi upload và xử lý hóa đơn: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}