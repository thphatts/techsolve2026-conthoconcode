package hackathon.fridgeai.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/test")
@CrossOrigin(origins = "*")
public class TestController {

    @GetMapping("/public")
    public ResponseEntity<String> publicEndpoint() {
        return ResponseEntity.ok("Chúc mừng! Server của bạn đang chạy ngon lành (Không cần Token).");
    }

    @GetMapping("/secured")
    public ResponseEntity<String> securedEndpoint(Principal principal) {
        // Principal chứa thông tin user (email) sau khi đã giải mã JWT thành công
        return ResponseEntity.ok("Tuyệt vời! JWT Token hợp lệ. Email của bạn là: " + principal.getName());
    }
}