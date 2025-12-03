package com.mutuelle.mobille.config;

import com.mutuelle.mobille.repository.AuthUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final AuthUserRepository authUserRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        return authUserRepository.findById(Long.parseLong(userId))
                .map(authUser -> org.springframework.security.core.userdetails.User
                        .withUsername(authUser.getId().toString())
                        .password(authUser.getPasswordHash()) // déjà hashé
                        .authorities(authUser.getUserType().name())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));
    }
}