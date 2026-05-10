package hackathon.fridgeai.controller;

import hackathon.fridgeai.dto.AiReceiptResponse;
import hackathon.fridgeai.service.AiVisionService;
import hackathon.fridgeai.service.BillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Base64;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/scan")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // Tạm mở cho React test local
public class BillScanController {

    private final AiVisionService aiVisionService;
    private final BillService billService;

    @Value("${app.upload-dir}")
    private String uploadDir;

    // Constructor được Lombok @RequiredArgsConstructor tự generate từ các final fields bên trên.

    @PostMapping("/receipt")
    public ResponseEntity<AiReceiptResponse> uploadReceipt(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId,
            @RequestParam("fridgeId") Long fridgeId) throws Exception {

        String mimeType = file.getContentType();
        if (mimeType == null || !mimeType.startsWith("image/")) {
            throw new RuntimeException("Tệp tải lên không phải là ảnh hợp lệ!");
        }

        // 1. Lưu file vào thư mục local
        String savedFilePath = saveFileLocally(file);

        // 2. Chuyển file ảnh thành Base64 để gửi cho AI
        String base64Image = Base64.getEncoder().encodeToString(file.getBytes());

        // 3. Gửi qua Service xử lý AI
        AiReceiptResponse response = aiVisionService.analyzeReceipt(base64Image, mimeType);

        // 4. Gọi BillService để lưu hóa đơn vào Database với đường dẫn file đã lưu
        billService.processAndSaveAiReceipt(userId, fridgeId, savedFilePath, response, null);

        return ResponseEntity.ok(response);
    }

    private String saveFileLocally(MultipartFile file) throws IOException {
        // Tạo đường dẫn thư mục nếu chưa tồn tại
        Path root = Paths.get(uploadDir);
        if (!Files.exists(root)) {
            Files.createDirectories(root);
        }

        // Tạo tên file duy nhất để tránh trùng lặp (Dùng UUID)
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path targetPath = root.resolve(fileName);

        // Lưu file vật lý
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        return targetPath.toString();
    }
}