package hackathon.fridgeai.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class HomeController {

    @GetMapping("/")
    public RedirectView redirectToAuth() {
        // Tự động chuyển hướng từ trang gốc (/) sang trang đăng nhập
        return new RedirectView("/Frontend/legacy/auth.html");
    }
}