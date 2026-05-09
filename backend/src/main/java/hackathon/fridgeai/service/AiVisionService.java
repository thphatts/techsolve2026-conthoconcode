package hackathon.fridgeai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import hackathon.fridgeai.dto.AiReceiptResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiVisionService {

    @Value("${app.ai.api-key}")
    private String geminiApiKey;

    @Value("${app.ai.model}")
    private String aiModel;

    @Value("${app.ai.base-url:http://localhost:11434/api/chat}")
    private String localAiUrl;

    @Value("${app.ai.bill-scan-prompt}")
    private String systemPrompt;

    private final ObjectMapper objectMapper; // Thư viện parse JSON mặc định của Spring

    // Khởi tạo WebClient một lần duy nhất để tái sử dụng, tối ưu hiệu năng
    private final WebClient webClient = WebClient.builder()
            .exchangeStrategies(ExchangeStrategies.builder()
                    .codecs(configurer -> configurer
                            .defaultCodecs()
                            .maxInMemorySize(10 * 1024 * 1024)) // 10MB limit
                    .build())
            .build();

    /**
     * Hàm chính: Gửi ảnh Base64 lên Gemini và trả về DTO
     */
    public AiReceiptResponse analyzeReceipt(String base64Image, String mimeType) {
        log.info("Bắt đầu gửi ảnh hóa đơn lên AI local model {}...", aiModel);

        Map<String, Object> requestBody = buildOllamaRequest(base64Image);

        try {
            String rawJsonResponse = webClient.post()
                    .uri(localAiUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // 3. Xử lý chuỗi JSON từ response của Ollama
            String cleanJson = extractJsonFromResponse(rawJsonResponse);

            // ==========================================
            // ĐOẠN CODE MỚI THÊM VÀO ĐỂ FIX LỖI JACKSON
            // ==========================================
            // Xóa ký tự \N bị lỗi do AI sinh ra
            cleanJson = cleanJson.replace("\\N", "");
            cleanJson = cleanJson.replaceAll("\\\\u(?![0-9a-fA-F]{4})[\\s\\S]{0,4}", "");

            // In ra log để nếu có lỗi tiếp thì bạn biết AI đang trả về cái gì
            log.info("Chuỗi JSON sau khi dọn dẹp:\n{}", cleanJson);
            // ==========================================

            // 4. Map chuỗi JSON thành Object Java
            return objectMapper.readValue(cleanJson, AiReceiptResponse.class);

        } catch (Exception e) {
            log.error("Lỗi khi gọi AI Local hoặc Parse JSON: ", e);
            throw new RuntimeException("Không thể phân tích hóa đơn lúc này. Chi tiết: " + e.getMessage());
        }
    }

    // --- Các hàm phụ trợ (Private Helper) ---

    private Map<String, Object> buildOllamaRequest(String base64) {
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", systemPrompt);
        message.put("images", List.of(base64));

        Map<String, Object> request = new HashMap<>();
        request.put("model", aiModel);
        request.put("messages", List.of(message));
        request.put("stream", false); // Tắt streaming để nhận 1 cục data duy nhất

        return request;
    }

    /**
     * Trick cực kỳ quan trọng cho Hackathon:
     * AI thỉnh thoảng sẽ tự bọc JSON trong blockquote (
     * ```json ... ```). Hàm này dùng Regex để lôi cái JSON thực sự ra ngoài.
     */
    private String extractJsonFromResponse(String response) throws Exception {
        var rootNode = objectMapper.readTree(response);
        String rawText = rootNode.path("message").path("content").asText();

        // Tìm vị trí của ngoặc nhọn mở đầu tiên và ngoặc nhọn đóng cuối cùng
        int startIndex = rawText.indexOf('{');
        int endIndex = rawText.lastIndexOf('}');

        if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
            String cleanJson = rawText.substring(startIndex, endIndex + 1);
            return cleanJson;
        }

        // Backup: Đề phòng AI trả về dạng Array [...] thay vì Object {...}
        int startArr = rawText.indexOf('[');
        int endArr = rawText.lastIndexOf(']');
        if (startArr != -1 && endArr != -1 && startArr < endArr) {
            String cleanArray = rawText.substring(startArr, endArr + 1);
            return "{\"items\": " + cleanArray + "}"; // Tự bọc lại cho đúng DTO
        }

        log.warn("Không tìm thấy format JSON. Chuỗi gốc: \n{}", rawText);
        throw new Exception("AI trả về sai định dạng JSON.");
    }
}