package hackathon.fridgeai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import hackathon.fridgeai.dto.AiReceiptResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiVisionService {

    @Value("${app.ai.api-key}")
    private String geminiApiKey;

    @Value("${app.ai.model}")
    private String geminiModel; // Mặc định trong yml là gemini-1.5-flash

    @Value("${app.ai.bill-scan-prompt}")
    private String systemPrompt;

    private final ObjectMapper objectMapper; // Thư viện parse JSON mặc định của Spring

    // Khởi tạo WebClient một lần duy nhất để tái sử dụng, tối ưu hiệu năng
    private final WebClient webClient = WebClient.create();

    /**
     * Hàm chính: Gửi ảnh Base64 lên Gemini và trả về DTO
     */
    public AiReceiptResponse analyzeReceipt(String base64Image, String mimeType) {
        log.info("Bắt đầu gửi ảnh hóa đơn lên mô hình {}...", geminiModel);

        // 1. Dựng cấu trúc Request Body gửi cho Google (chuẩn format của Gemini)
        Map<String, Object> requestBody = buildGeminiRequest(base64Image, mimeType);

        // 2. Cấu hình WebClient và gọi API (Đồng bộ - block)
        String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/" + geminiModel
                + ":generateContent?key=" + geminiApiKey;

        try {
            String rawJsonResponse = webClient.post()
                    .uri(apiUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(); // Chờ kết quả trả về

            // 3. Xử lý chuỗi JSON rác (nếu AI trả về markdown ```json ...)
            String cleanJson = extractJsonFromResponse(rawJsonResponse);

            // 4. Map chuỗi JSON thành Object Java
            return objectMapper.readValue(cleanJson, AiReceiptResponse.class);

        } catch (Exception e) {
            log.error("Lỗi khi gọi API Gemini hoặc Parse JSON: ", e);
            throw new RuntimeException("Không thể phân tích hóa đơn lúc này. Chi tiết: " + e.getMessage());
        }
    }

    // --- Các hàm phụ trợ (Private Helper) ---

    private Map<String, Object> buildGeminiRequest(String base64, String mimeType) {
        // Cấu trúc Part cho Text (Prompt)
        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", systemPrompt);

        // Cấu trúc Part cho Image
        Map<String, Object> inlineData = new HashMap<>();
        inlineData.put("mime_type", mimeType);
        inlineData.put("data", base64);

        Map<String, Object> imagePart = new HashMap<>();
        imagePart.put("inline_data", inlineData);

        // Gộp vào Contents
        Map<String, Object> contents = new HashMap<>();
        contents.put("parts", List.of(textPart, imagePart));

        return Map.of("contents", List.of(contents));
    }

    /**
     * Trick cực kỳ quan trọng cho Hackathon:
     * AI thỉnh thoảng sẽ tự bọc JSON trong blockquote (
     * ```json ... ```). Hàm này dùng Regex để lôi cái JSON thực sự ra ngoài.
     */
    private String extractJsonFromResponse(String geminiResponse) throws Exception {
        // Lấy đoạn text chính trong response phức tạp của Gemini
        var rootNode = objectMapper.readTree(geminiResponse);
        var candidates = rootNode.path("candidates");

        if (candidates.isMissingNode() || candidates.isEmpty()) {
            throw new Exception("Gemini trả về response không hợp lệ (Không có ứng viên/candidates).");
        }

        String rawText = candidates.path(0).path("content").path("parts").path(0).path("text").asText();

        // Regex tìm đoạn bắt đầu bằng { và kết thúc bằng } (Dùng reluctant quantifier
        // .*? để tránh lỗi tham lam)
        Pattern pattern = Pattern.compile("(?s)\\{.*?\\}");
        Matcher matcher = pattern.matcher(rawText);

        if (matcher.find()) {
            return matcher.group();
        }

        log.warn("Không tìm thấy format JSON. Chuỗi gốc: \n{}", rawText);
        throw new Exception("AI trả về sai định dạng JSON.");
    }
}