package hackathon.fridgeai.service;

import hackathon.fridgeai.dto.AiReceiptItem;
import hackathon.fridgeai.dto.AiReceiptResponse;
import hackathon.fridgeai.entity.*;
import hackathon.fridgeai.enums.BillStatus;
import hackathon.fridgeai.repository.BillRepository;
import hackathon.fridgeai.repository.FridgeRepository;
import hackathon.fridgeai.repository.ProductRepository;
import hackathon.fridgeai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillService {

        private final BillRepository billRepository;
        private final ProductRepository productRepository;
        private final UserRepository userRepository;
        private final FridgeRepository fridgeRepository;

        /**
         * Hàm này nhận kết quả từ AI, map với Database và lưu lại hóa đơn
         */
        @Transactional
        public Bill processAndSaveAiReceipt(Long userId, Long fridgeId, String imageUrl, AiReceiptResponse aiResponse,
                        Map<String, Object> rawJson) {
                if (userId == null || fridgeId == null) {
                        throw new RuntimeException("UserID hoặc FridgeID không được để trống.");
                }

                // 1. Tìm User và Fridge (Trong thực tế Hackathon, bạn có thể hardcode ID 1L để
                // test cho nhanh)
                // 1. Tìm hoặc Tự tạo User (Tối ưu cho Hackathon demo)
                User user = userRepository.findById(userId)
                                .orElseGet(() -> {
                                        User newUser = User.builder()
                                                        .name("Demo User")
                                                        .email("demo" + userId + "@example.com")
                                                        .passwordHash("password_hash")
                                                        .totalPoints(0)
                                                        .build();
                                        return userRepository.save(newUser);
                                });

                // Tìm hoặc Tự tạo Fridge
                Fridge fridge = fridgeRepository.findById(fridgeId)
                                .orElseGet(() -> {
                                        Fridge newFridge = Fridge.builder()
                                                        .name("Tủ lạnh Demo")
                                                        .owner(user)
                                                        .build();
                                        return fridgeRepository.save(newFridge);
                                });

                // 2. Tạo hóa đơn mới
                Bill newBill = Bill.builder()
                                .user(user)
                                .fridge(fridge)
                                .imageUrl(imageUrl)
                                .aiRawJson(rawJson) // Lưu lại JSON gốc để debug
                                .status(BillStatus.PENDING) // Đang chờ user review
                                .build();

                // 3. Xử lý từng món đồ AI đọc được
                if (aiResponse != null && aiResponse.getItems() != null) {
                        // Khởi tạo danh sách an toàn, phòng trường hợp Builder không tự khởi tạo List
                        if (newBill.getItems() == null) {
                                newBill.setItems(new ArrayList<>());
                        }

                        for (AiReceiptItem aiItem : aiResponse.getItems()) {

                                // Cố gắng tìm sản phẩm trong "Từ điển" Product bằng tên AI đọc được
                                Product matchedProduct = productRepository.findByName(aiItem.getProductName())
                                                .orElse(null);

                                // Tạo chi tiết hóa đơn
                                BillItem billItem = BillItem.builder()
                                                .bill(newBill)
                                                .product(matchedProduct) // Có thể là null nếu đây là món lạ
                                                .rawNameFromAi(aiItem.getProductName())
                                                .quantity(aiItem.getQuantity() != null
                                                                ? aiItem.getQuantity().doubleValue()
                                                                : 1.0)
                                                .unitPrice(aiItem.getPrice())
                                                .confirmed(false) // Yêu cầu user xác nhận
                                                .build();

                                // Thêm vào danh sách của Bill
                                newBill.getItems().add(billItem);
                        }
                }

                // 4. Lưu tất cả vào Database (Vì có CascadeType.ALL, lưu Bill sẽ tự động lưu
                // các BillItem bên trong)
                if (newBill != null) {
                        return billRepository.save(newBill);
                }
                throw new RuntimeException("Không thể tạo hóa đơn mới.");
        }
}