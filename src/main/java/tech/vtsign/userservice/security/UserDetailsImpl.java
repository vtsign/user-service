package tech.vtsign.userservice.security;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import tech.vtsign.userservice.model.UserResponseDto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
public class UserDetailsImpl implements UserDetails {
    private UserResponseDto user;

    public UserDetailsImpl(UserResponseDto user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> list = new ArrayList<>();
        //list of permission
        if (this.user.getPermissions() != null)
            this.user.getPermissions().forEach(permission -> {
                GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(permission.getName());
                list.add(grantedAuthority);
            });
        // list of roles
        if (this.user.getRoles() != null)
            this.user.getRoles().forEach(role -> {
                GrantedAuthority grantedAuthority = new SimpleGrantedAuthority("ROLE_" + role.getName());
                list.add(grantedAuthority);
            });
        return list;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !user.isBlocked() && !user.isDeleted();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }
}
