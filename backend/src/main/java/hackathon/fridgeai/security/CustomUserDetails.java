package hackathon.fridgeai.security;

import hackathon.fridgeai.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Data
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getAuthorities(); // Lấy trực tiếp quyền (ROLE_USER) đã định nghĩa trong entity User
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash(); // Trả về hash lưu trong DB
    }

    @Override
    public String getUsername() {
        return user.getEmail(); // Dùng email để đăng nhập
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
