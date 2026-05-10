# TechSolve 2026 - Đội ConThoConCode (Smart Fridge Project)

## 📌 Giới thiệu dự án

Smart Fridge là một ứng dụng quản lý tủ lạnh thông minh, giúp người dùng theo dõi tình trạng thực phẩm, gợi ý món ăn, và quản lý chi tiêu (ví thực phẩm) một cách hiệu quả. Hệ thống tích hợp khả năng quét và nhận diện hóa đơn/thực phẩm bằng AI để tự động thêm vào tủ lạnh nhanh chóng.

## 🤖 Tuyên bố về việc sử dụng AI (AI Usage Declaration)

Theo yêu cầu và quy định, chúng tôi xin công bố rõ ràng việc sử dụng các công cụ AI hỗ trợ trong quá trình phát triển dự án này:

- **Công cụ sử dụng:**Gemini, và GitHub Copilot.
- **Mức độ hỗ trợ:**
  - Gợi ý cú pháp, tự động hoàn thành code (Autocomplete) và tối ưu hóa hoặc refactor các đoạn code nhỏ.
  - Hỗ trợ viết nhanh một số đoạn CSS/HTML giao diện cơ bản và các function Utility.
  - Hỗ trợ tìm kiếm, giải thích lỗi (debug) trong quá trình lập trình.
- **Cam kết cốt lõi:** **Toàn bộ kiến trúc phần mềm, logic hệ thống, thiết kế UI/UX, luồng dữ liệu (Data flow) và các quyết định công nghệ quan trọng đều hoàn toàn do các thành viên trong đội ngũ tự thiết kế và xây dựng.** Các công cụ AI chỉ được sử dụng với vai trò trợ lý hỗ trợ tăng tốc độ gõ code và gỡ lỗi, hoàn toàn tuân thủ quy định của giải đấu.

## 🚀 Tính năng nổi bật

- **Quản lý thực phẩm:** Theo dõi ngày hết hạn, số lượng, và trạng thái (Còn tốt, Sắp hết, Hết hạn).
- **Quét hóa đơn AI:** Sử dụng camera để chụp ảnh hóa đơn, AI sẽ tự động phân tích, bóc tách thông tin và tự động lưu vào tủ lạnh.
- **Ví thực phẩm (Food Wallet):** Thống kê dòng tiền thực phẩm, phân biệt rõ ràng số tiền "Đã ăn (Hợp lý)" và "Phung phí (Do để hết hạn)" giúp người dùng tiết kiệm hơn.
- **Gợi ý món ăn thông minh:** Tự động đề xuất công thức nấu ăn ưu tiên sử dụng các thực phẩm sắp hết hạn trong tủ.

## 🛠 Công nghệ sử dụng

- **Frontend:** HTML5, CSS3, JavaScript (Vanilla JS), giao diện thiết kế theo phong cách Pixel Art độc đáo, tối ưu hóa cho nền tảng Mobile.
- **Backend:** Java Spring Boot, cung cấp các RESTful API.
- **Tích hợp AI:** Sử dụng mô hình AI Vision để phân tích hóa đơn và bóc tách dữ liệu.

## 👨‍💻 Hướng dẫn chạy dự án

1. Clone repository này về máy.
2. Tại thư mục `backend/`, khởi chạy ứng dụng Spring Boot thông qua Maven: `mvn spring-boot:run`.
3. Mở file `backend/src/main/resources/static/Frontend/legacy/Index.html` bằng trình duyệt web để bắt đầu trải nghiệm (Hoặc sử dụng Live Server).
