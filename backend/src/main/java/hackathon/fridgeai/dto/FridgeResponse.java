package hackathon.fridgeai.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FridgeResponse {
    private Long id;
    private String name;
    private Long ownerId;
    private String ownerName;
}