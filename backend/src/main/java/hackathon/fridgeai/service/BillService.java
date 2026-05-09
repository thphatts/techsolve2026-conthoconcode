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

                // 1. Tìm User và Fridge (Trong thực tế Hackathon, bạn có thể hardcode ID 1L để
                // test cho nhanh)
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy User"));
                Fridge fridge = fridgeRepository.findById(fridgeId)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy Fridge"));

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
                return billRepository.save(newBill);
        }
}