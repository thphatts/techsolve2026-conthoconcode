package hackathon.fridgeai.dto;

import hackathon.fridgeai.entity.BillItem;
import hackathon.fridgeai.enums.BillStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class BillDetailResponse {
    private Long id;
    private String imageUrl;
    private BillStatus status;
    private Instant scannedAt;
    private Long fridgeId;
    private Map<String, Object> aiRawJson;
    private List<BillItem> items;
}