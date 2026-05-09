package hackathon.fridgeai.dto;

import lombok.Data;
import java.util.List;

@Data
public class AiReceiptResponse {
    private List<AiReceiptItem> items;
}