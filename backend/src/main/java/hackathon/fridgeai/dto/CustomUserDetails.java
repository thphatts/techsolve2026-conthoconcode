package hackathon.fridgeai.security;

import hackathon.fridgeai.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Data
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Tạm thời chưa chia role admin/user nên trả về rỗng
        return Collections.emptyList();
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