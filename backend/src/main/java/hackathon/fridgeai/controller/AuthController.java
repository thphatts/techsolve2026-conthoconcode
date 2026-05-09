package hackathon.fridgeai.controller;

import hackathon.fridgeai.dto.AuthDto.AuthResponse;
import hackathon.fridgeai.dto.AuthDto.LoginRequest;
import hackathon.fridgeai.dto.AuthDto.SignupRequest;
import hackathon.fridgeai.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Hỗ trợ React/Vue truy cập
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@RequestBody SignupRequest request) {
        return ResponseEntity.ok(authService.signup(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}