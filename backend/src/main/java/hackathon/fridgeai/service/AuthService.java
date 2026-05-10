package hackathon.fridgeai.service;

import hackathon.fridgeai.dto.AuthDto.AuthResponse;
import hackathon.fridgeai.dto.AuthDto.LoginRequest;
import hackathon.fridgeai.dto.AuthDto.SignupRequest;
import hackathon.fridgeai.entity.User;
import hackathon.fridgeai.entity.Fridge;
import hackathon.fridgeai.service.FridgeService;
import hackathon.fridgeai.repository.UserRepository;
import hackathon.fridgeai.security.CustomUserDetails;
import hackathon.fridgeai.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AuthService {

        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtService jwtService;
        private final AuthenticationManager authenticationManager;
        private final FridgeService fridgeService;

        @Transactional
        public AuthResponse signup(SignupRequest request) {
                // Kiểm tra email trùng lặp
                if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                        throw new RuntimeException("Email đã được sử dụng!");
                }

                // Mã hóa mật khẩu và tạo user
                User user = User.builder()
                                .name(request.getName())
                                .email(request.getEmail())
                                .passwordHash(passwordEncoder.encode(request.getPassword()))
                                .totalPoints(0)
                                .walletBalance(BigDecimal.ZERO)
                                .build();
                // Save and flush để đảm bảo lấy được ID ngay lập tức cho Token
                User savedUser = userRepository.saveAndFlush(user);

                // Tự động tạo một tủ lạnh mặc định cho người dùng mới
                Fridge defaultFridge = fridgeService.createFridge(savedUser.getId());

                // Cấp JWT
                CustomUserDetails userDetails = new CustomUserDetails(savedUser);
                String jwtToken = jwtService.generateToken(userDetails);

                return AuthResponse.builder()
                                .token(jwtToken)
                                .userId(savedUser.getId())
                                .name(savedUser.getName())
                                .email(savedUser.getEmail())
                                .fridgeId(defaultFridge.getId())
                                .build();
        }

        public AuthResponse login(LoginRequest request) {
                // Xác thực username/password
                authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

                User user = userRepository.findByEmail(request.getEmail())
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy User"));

                String jwtToken = jwtService.generateToken(new CustomUserDetails(user));

                Long userFridgeId = user.getFridges().isEmpty() ? null : user.getFridges().get(0).getId();

                return AuthResponse.builder()
                                .token(jwtToken)
                                .userId(user.getId())
                                .name(user.getName())
                                .email(user.getEmail())
                                .fridgeId(userFridgeId)
                                .build();
        }
}