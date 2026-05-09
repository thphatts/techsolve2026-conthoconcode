package hackathon.fridgeai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import hackathon.fridgeai.dto.AiReceiptResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
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

    @Value("${app.ai.openrouter-url:https://openrouter.ai/api/v1/chat/completions}")
    private String openRouterUrl;

    @Value("${app.ai.openrouter-model:google/gemini-2.0-flash-lite-001}")
    private String openRouterModel;

    @Value("${app.ai.base-url:http://localhost:11434/api/chat}")
    private String localAiUrl;

    @Value("${app.ai.bill-scan-prompt}")
    private String systemPrompt;

    private final ObjectMapper objectMapper; // Thư viện parse JSON mặc định của Spring

    // Tăng response timeout lên 3 phút để bù đắp cho sự chậm trễ của AI Vision
    private final HttpClient httpClient = HttpClient.create()
            .responseTimeout(Duration.ofMinutes(3))
            .option(io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000);

    // Khởi tạo WebClient một lần duy nhất để tái sử dụng, tối ưu hiệu năng
    private final WebClient webClient = WebClient.builder()
            .exchangeStrategies(ExchangeStrategies.builder()
                    .codecs(configurer -> configurer
                            .defaultCodecs()
                            .maxInMemorySize(10 * 1024 * 1024)) // 10MB limit
                    .build())
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build();

    /**
     * Hàm chính: Gửi ảnh Base64 lên Gemini và trả về DTO
     */
    public AiReceiptResponse analyzeReceipt(String base64Image, String mimeType) {
        try {
            log.info("Ưu tiên: Gửi ảnh lên OpenRouter (Model: {})...", openRouterModel);
            return analyzeWithOpenRouter(base64Image, mimeType);
        } catch (Exception e) {
            log.warn("OpenRouter gặp lỗi: {}. Đang chuyển sang fallback dùng Ollama local...", e.getMessage());
            return analyzeWithOllama(base64Image);
        }
    }

    private AiReceiptResponse analyzeWithOpenRouter(String base64Image, String mimeType) throws Exception {
        Map<String, Object> requestBody = buildOpenRouterRequest(base64Image, mimeType);

        try {
            String rawJsonResponse = webClient.post()
                    .uri(openRouterUrl)
                    .header("Authorization", "Bearer " + geminiApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            var rootNode = objectMapper.readTree(rawJsonResponse);
            String rawText = rootNode.path("choices").get(0).path("message").path("content").asText();

            String cleanJson = extractJsonFromString(rawText);

            // ĐOẠN CODE MỚI THÊM VÀO ĐỂ FIX LỖI JACKSON
            // Xóa ký tự \N bị lỗi do AI sinh ra
            cleanJson = cleanJson.replace("\\N", "");
            cleanJson = cleanJson.replaceAll("\\\\u(?![0-9a-fA-F]{4})[\\s\\S]{0,4}", "");

            // In ra log để nếu có lỗi tiếp thì bạn biết AI đang trả về cái gì
            log.info("Chuỗi JSON sau khi dọn dẹp:\n{}", cleanJson);

            return objectMapper.readValue(cleanJson, AiReceiptResponse.class);
        } catch (Exception e) {
            throw new Exception("Lỗi khi gọi OpenRouter: " + e.getMessage());
        }
    }

    private AiReceiptResponse analyzeWithOllama(String base64Image) {
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

            var rootNode = objectMapper.readTree(rawJsonResponse);
            String rawText = rootNode.path("message").path("content").asText();

            String cleanJson = extractJsonFromString(rawText);
            cleanJson = cleanJson.replace("\\N", "");
            cleanJson = cleanJson.replaceAll("\\\\u(?![0-9a-fA-F]{4})[\\s\\S]{0,4}", "");

            log.info("Chuỗi JSON từ Ollama sau khi dọn dẹp:\n{}", cleanJson);
            return objectMapper.readValue(cleanJson, AiReceiptResponse.class);
        } catch (Exception e) {
            log.error("Lỗi khi gọi AI Local hoặc Parse JSON: ", e);
            throw new RuntimeException("Cả AI Cloud và Local đều không thể phân tích hóa đơn.");
        }
    }

    private Map<String, Object> buildOpenRouterRequest(String base64, String mimeType) {
        Map<String, Object> textContent = Map.of("type", "text", "text", systemPrompt);
        Map<String, Object> imageContent = Map.of("type", "image_url", "image_url",
                Map.of("url", "data:" + mimeType + ";base64," + base64));

        Map<String, Object> message = Map.of("role", "user", "content", List.of(textContent, imageContent));

        Map<String, Object> request = new HashMap<>();
        request.put("model", openRouterModel);
        request.put("messages", List.of(message));
        return request;
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
    private String extractJsonFromString(String rawText) throws Exception {
        // Tìm vị trí của ngoặc nhọn mở đầu tiên và ngoặc nhọn đóng cuối cùng
        int startIndex = rawText.indexOf('{');
        int endIndex = rawText.lastIndexOf('}');

        if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
            String cleanJson = rawText.substring(startIndex, endIndex + 1);
            return cleanJson;
        }

        // Dự phòng: Nếu AI chỉ trả về một mảng JSON [ ... ]
        int startArr = rawText.indexOf('[');
        int endArr = rawText.lastIndexOf(']');
        if (startArr != -1 && endArr != -1 && startArr < endArr) {
            String cleanArray = rawText.substring(startArr, endArr + 1);
            // Nếu model Gemini/Llama trả về mảng, ta bọc nó lại thành object khớp với
            // AiReceiptResponse DTO
            return "{\"items\": " + cleanArray + "}"; // Tự bọc lại cho đúng DTO
        }

        log.warn("Không tìm thấy format JSON. Chuỗi gốc: \n{}", rawText);
        throw new Exception("AI trả về sai định dạng JSON.");
    }
}