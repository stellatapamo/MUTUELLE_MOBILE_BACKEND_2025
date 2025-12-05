package com.mutuelle.mobille.service;

import com.mutuelle.mobille.models.AuthUser;
import com.mutuelle.mobille.repository.AuthUserRepository;
import com.mutuelle.mobille.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final AuthUserRepository authUserRepository;
    private final JwtUtils jwtUtils;

    @Override
    public UserDetails loadUserByUsername(String userIdStr) {
        Long userId = Long.parseLong(userIdStr);
        AuthUser authUser = authUserRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Rôle avec le préfixe Spring Security
        String authority = "ROLE_" + authUser.getRole().name();

        return User.withUsername(userId.toString())
                .password("") // pas utilisé après login
                .authorities(authority)
                .build();
    }
}