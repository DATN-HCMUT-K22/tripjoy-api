package com.tripjoy.api.configuration.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Builder
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class UserDetailsCustom implements UserDetails {

    private UUID userId;

    private String username;

    private String password;

    private List<String> roles;

    private Collection<? extends GrantedAuthority> authorities;

    public static UserDetailsCustom getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (!authentication.isAuthenticated()) {
                return null;
            }

            return (UserDetailsCustom) authentication.getPrincipal();
        } catch (Exception ex) {
            return null;
        }
    }

    public static UUID getCurrentUserId() {
        return Objects.requireNonNull(getCurrentUser()).getUserId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isEnabled() {
        return true;
    }
}