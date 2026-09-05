package com.example.demo.security;

import com.example.demo.entity.Users;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class CustomUserPrincipal implements UserDetails {

    private final Users user;

    public CustomUserPrincipal(Users user) {
        this.user = user;
    }

    public Integer getUserId() {
        return user.getUserId();
    }

    public Integer getShelterId() {
        return user.getShelter() == null ? null : user.getShelter().getShelterId();
    }

    public String getRole() {
        return user.getRole();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String normalized = user.getRole() == null ? "STAFF" : user.getRole().trim();
        return List.of(new SimpleGrantedAuthority("ROLE_" + normalized.toUpperCase(Locale.ROOT)));
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
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
