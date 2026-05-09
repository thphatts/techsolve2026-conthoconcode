package hackathon.fridgeai.service;

import hackathon.fridgeai.entity.*;
import hackathon.fridgeai.enums.EventType;
import hackathon.fridgeai.enums.FridgeItemStatus;
import hackathon.fridgeai.repository.FridgeItemRepository;
import hackathon.fridgeai.repository.FridgeRepository;
import hackathon.fridgeai.repository.FridgeMemberRepository;
import hackathon.fridgeai.enums.FridgeMemberRole;
import hackathon.fridgeai.repository.GamificationLogRepository;
import hackathon.fridgeai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FridgeService {

        private final FridgeItemRepository fridgeItemRepository;
        private final FridgeRepository fridgeRepository;
        private final UserRepository userRepository;
        private final GamificationLogRepository gamificationLogRepository;
        private final FridgeMemberRepository fridgeMemberRepository;

        // Lấy các chỉ số Gamification từ file application.yml
        @Value("${app.gamification.points-consumed-before-expiry:10}")
        private Integer pointsConsumedBonus;

        /**
         * Lấy toàn bộ đồ ăn trong 1 tủ lạnh cụ thể
         */
        @Transactional(readOnly = true)
        public List<FridgeItem> getItemsInFridge(Long fridgeId) {
                log.info("Lấy danh sách đồ ăn cho tủ lạnh ID: {}", fridgeId);
                return fridgeItemRepository.findByFridgeId(fridgeId);
        }

        /**
         * Người dùng đánh dấu đã ăn (CONSUMED) một món đồ trước khi nó hết hạn.
         * Đây là hành động bảo vệ môi trường -> Được thưởng điểm (Gamification).
         */
        @Transactional
        public FridgeItem consumeItem(Long itemId, Long userId) {
                FridgeItem item = fridgeItemRepository.findById(itemId)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy món đồ này trong tủ lạnh."));

                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy User."));

                // Kiểm tra xem món này đã bị ăn hay hỏng chưa
                if (item.getStatus() == FridgeItemStatus.CONSUMED || item.getStatus() == FridgeItemStatus.EXPIRED) {
                        throw new RuntimeException("Món đồ này đã được xử lý hoặc đã hết hạn trước đó.");
                }

                // Đổi trạng thái thành ĐÃ ĂN
                item.setStatus(FridgeItemStatus.CONSUMED);
                FridgeItem savedItem = fridgeItemRepository.save(item);

                // --- BẮT ĐẦU LUỒNG GAMIFICATION (Thưởng điểm) ---
                // Chỉ thưởng nếu ăn trước khi hết hạn
                if (!LocalDate.now().isAfter(item.getExpiresAt())) {

                        // 1. Tạo Log Giao dịch
                        GamificationLog logEntry = GamificationLog.builder()
                                        .user(user)
                                        .fridgeItem(item)
                                        .eventType(EventType.CONSUMED_BEFORE_EXPIRY)
                                        .pointsDelta(pointsConsumedBonus) // Cộng điểm EXP
                                        .walletDelta(BigDecimal.ZERO) // Ăn đúng hạn thì không bị phạt tiền
                                        .build();
                        gamificationLogRepository.save(logEntry);

                        // 2. Cập nhật tổng điểm cho User
                        user.setTotalPoints((user.getTotalPoints() != null ? user.getTotalPoints() : 0)
                                        + pointsConsumedBonus);
                        userRepository.save(user);

                        // Thay vì user.getUsername(), hãy sửa thành user.getName()
                        log.info("User {} đã nhận được {} điểm nhờ ăn món {} trước hạn.",
                                        user.getName(),
                                        pointsConsumedBonus,
                                        item.getProduct().getName());
                }

                return savedItem;
        }

        /**
         * Thêm một món đồ mới vào tủ lạnh bằng tay (Manual Input).
         */
        @Transactional
        public FridgeItem addItemManually(Long fridgeId, Long userId, Product product, LocalDate expiresAt,
                        Double quantity, BigDecimal price) {
                Fridge fridge = fridgeRepository.findById(fridgeId)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy Fridge."));
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy User."));

                FridgeItem newItem = FridgeItem.builder()
                                .fridge(fridge)
                                .product(product)
                                .addedBy(user)
                                .expiresAt(expiresAt)
                                .quantity(quantity != null ? quantity : 1.0)
                                .purchasePrice(price != null ? price : BigDecimal.ZERO)
                                .status(FridgeItemStatus.FRESH)
                                .build();

                log.info("Thêm món đồ {} vào tủ lạnh {} thành công.", product.getName(), fridgeId);
                return fridgeItemRepository.save(newItem);
        }

        /**
         * Tạo một tủ lạnh mới và gán người tạo làm OWNER
         */
        @Transactional
        public Fridge createFridge(Long userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy User."));

                // 1. Tạo bản ghi Tủ lạnh mới và gán các thông tin bắt buộc
                Fridge newFridge = Fridge.builder()
                                .name("Tủ lạnh của " + user.getName())
                                .owner(user)
                                .build();

                Fridge savedFridge = fridgeRepository.save(newFridge);

                // 2. Thêm người tạo vào danh sách thành viên với quyền OWNER
                FridgeMember ownerMember = FridgeMember.builder()
                                .fridge(savedFridge)
                                .user(user)
                                .role(FridgeMemberRole.OWNER)
                                .build();
                fridgeMemberRepository.save(ownerMember);

                log.info("User {} đã tạo tủ lạnh mới với ID: {}", user.getName(), savedFridge.getId());
                return savedFridge;
        }
}