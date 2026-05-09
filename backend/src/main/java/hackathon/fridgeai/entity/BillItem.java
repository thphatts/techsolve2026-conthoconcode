package hackathon.fridgeai.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "bill_items", indexes = {
        @Index(name = "idx_bill_items_bill", columnList = "bill_id"),
        @Index(name = "idx_bill_items_product", columnList = "product_id")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = { "bill", "product" }) // Tránh vòng lặp khi đối chiếu ngược lên Bill
@ToString(callSuper = true, exclude = { "bill", "product" })
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class BillItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bill_id", nullable = false)
    @JsonIgnoreProperties("items") // Chặn không cho in ngược lại danh sách items của Bill
    private Bill bill;

    // Tại sao optional = true? Vì có thể AI đọc ra một món đồ lạ chưa từng có trong
    // bảng Products.
    // Lúc này product_id sẽ bằng null, đợi người dùng tự map (chỉnh sửa) lại sau.
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "product_id")
    private Product product;

    // Lưu lại chính xác tên mà AI đọc được từ bill (VD: "THIT BO MAT MEATDELI
    // 250G")
    @Column(name = "raw_name_from_ai", nullable = false, length = 200)
    private String rawNameFromAi;

    @Column(nullable = false)
    @Builder.Default
    private Double quantity = 1.0;

    // Giá của 1 đơn vị
    @Column(name = "unit_price", precision = 10, scale = 2)
    private BigDecimal unitPrice;

    // Tổng tiền của món đó (Phòng trường hợp AI đọc được tổng tiền nhưng không rõ
    // giá lẻ)
    @Column(name = "total_price", precision = 10, scale = 2)
    private BigDecimal totalPrice;

    // Đánh dấu xem người dùng đã review và chốt đúng món đồ này chưa
    @Column(nullable = false)
    @Builder.Default
    private Boolean confirmed = false;
}