package hackathon.fridgeai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class AiReceiptItem {
    @JsonProperty("product_name")
    private String productName;

    private Integer quantity;

    private BigDecimal price;

    @JsonProperty("estimated_expiry_days")
    private Integer estimatedExpiryDays;
}