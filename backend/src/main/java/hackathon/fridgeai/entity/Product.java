package hackathon.fridgeai.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_products_barcode", columnList = "barcode", unique = true),
        @Index(name = "idx_products_category", columnList = "category")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = { "fridgeItems", "recipeIngredients" }) // Chống vòng lặp vô tận
@ToString(callSuper = true, exclude = { "fridgeItems", "recipeIngredients" }) // Tránh lỗi in log quá dài gây tràn bộ
                                                                              // nhớ
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Product extends BaseEntity {

    // FIX 1: Chuyển sang String để không bị mất số 0 ở đầu mã vạch
    @Column(length = 64, unique = true)
    private String barcode;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 100)
    private String category;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String unit = "cái";

    // FIX 2: Tăng độ dài url lên 512 (nhiều link ảnh trên mạng rất dài)
    @Column(name = "image_url", length = 512)
    private String imageUrl;

    // FIX 3: Thêm lại trường cốt lõi để AI dự đoán hạn sử dụng (Tính bằng ngày)
    @Column(name = "default_shelf_life")
    private Integer defaultShelfLife;

    // --- Relationships ---

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    @Builder.Default
    private List<FridgeItem> fridgeItems = new ArrayList<>();

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    @Builder.Default
    private List<RecipeIngredient> recipeIngredients = new ArrayList<>();
}