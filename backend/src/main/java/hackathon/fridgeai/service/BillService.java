package hackathon.fridgeai.service;

import hackathon.fridgeai.dto.AiReceiptResponse;
import hackathon.fridgeai.entity.Bill;
import hackathon.fridgeai.entity.BillItem;
import hackathon.fridgeai.entity.Fridge;
import hackathon.fridgeai.entity.FridgeItem;
import hackathon.fridgeai.entity.Product;
import hackathon.fridgeai.entity.User;
import hackathon.fridgeai.enums.BillStatus;
import hackathon.fridgeai.enums.FridgeItemStatus;
import hackathon.fridgeai.repository.BillItemRepository;
import hackathon.fridgeai.repository.BillRepository;
import hackathon.fridgeai.repository.FridgeItemRepository;
import hackathon.fridgeai.repository.FridgeRepository;
import hackathon.fridgeai.repository.ProductRepository;
import hackathon.fridgeai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BillService {

        private final BillItemRepository billItemRepository;
        private final FridgeItemRepository fridgeItemRepository;
        private final ProductRepository productRepository;
        private final BillRepository billRepository;
        private final UserRepository userRepository;
        private final FridgeRepository fridgeRepository;

        @Transactional
        public FridgeItem confirmBillItem(Long billItemId, LocalDate expiresAt) {
                BillItem billItem = billItemRepository.findById(billItemId)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy món hàng trong hóa đơn."));

                if (billItem.getConfirmed()) {
                        throw new RuntimeException("Món hàng này đã được xác nhận và thêm vào tủ lạnh rồi!");
                }

                // Nếu AI quét ra món mới chưa có trong DB Products, tự động tạo mới
                Product product = billItem.getProduct();
                if (product == null) {
                        product = Product.builder().name(billItem.getRawNameFromAi()).defaultShelfLife(3).build();
                        product = productRepository.save(product);
                        billItem.setProduct(product);
                }

                // Chuyển món hàng vào tủ lạnh
                FridgeItem fridgeItem = FridgeItem.builder().fridge(billItem.getBill().getFridge()).product(product)
                                .addedBy(billItem.getBill().getUser()).quantity(billItem.getQuantity())
                                .purchasePrice(billItem.getUnitPrice())
                                .expiresAt(expiresAt != null ? expiresAt
                                                : LocalDate.now()
                                                                .plusDays(product.getDefaultShelfLife() != null
                                                                                ? product.getDefaultShelfLife()
                                                                                : 3))
                                .status(FridgeItemStatus.FRESH).build();

                // Đánh dấu đã chốt
                billItem.setConfirmed(true);
                billItemRepository.save(billItem);
                return fridgeItemRepository.save(fridgeItem);
        }

        public List<BillItem> getItemsByBill(Long billId) {
                return billItemRepository.findByBillId(billId);
        }

        @Transactional
        public void processAndSaveAiReceipt(Long userId, Long fridgeId, String imageUrl, AiReceiptResponse aiResponse,
                        Map<String, Object> aiRawJson) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy User."));
                Fridge fridge = fridgeRepository.findById(fridgeId)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy Fridge."));

                // 1. Tạo hóa đơn mới (Bill)
                Bill bill = Bill.builder()
                                .user(user)
                                .fridge(fridge)
                                .imageUrl(imageUrl)
                                .aiRawJson(aiRawJson)
                                .status(BillStatus.PENDING)
                                .build();

                // 2. Chuyển đổi các món AI đọc được thành BillItem
                List<BillItem> billItems = aiResponse.getItems().stream().map(aiItem -> {
                        // Tìm xem trong DB đã có món này chưa, nếu chưa thì để null để người dùng tự
                        // xác nhận
                        Product product = productRepository.findByName(aiItem.getProductName()).orElse(null);

                        return BillItem.builder()
                                        .bill(bill)
                                        .product(product)
                                        .rawNameFromAi(aiItem.getProductName())
                                        .quantity(aiItem.getQuantity() != null ? aiItem.getQuantity().doubleValue()
                                                        : 1.0)
                                        .unitPrice(aiItem.getPrice())
                                        .totalPrice(aiItem.getPrice() != null && aiItem.getQuantity() != null
                                                        ? aiItem.getPrice().multiply(
                                                                        BigDecimal.valueOf(aiItem.getQuantity()))
                                                        : null)
                                        .confirmed(false)
                                        .build();
                }).collect(Collectors.toList());

                bill.setItems(billItems);
                billRepository.save(bill); // Nhờ cấu hình CascadeType.ALL, lệnh này sẽ tự động lưu cả Bill và các
                                           // BillItem
        }
}