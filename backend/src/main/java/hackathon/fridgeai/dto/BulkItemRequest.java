package hackathon.fridgeai.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class BulkItemRequest {
    private String productName;
    private Double quantity;
    private LocalDate expiresAt;
}