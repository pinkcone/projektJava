package com.pollub.cookie.service;

import com.pollub.cookie.model.User;
import com.pollub.cookie.repository.UserRepository;
import com.pollub.cookie.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Ładuje użytkownika na podstawie emailu.
     *
     * @param email Email użytkownika
     * @return Obiekt UserDetails
     * @throws UsernameNotFoundException Jeśli użytkownik nie zostanie znaleziony
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("Ładowanie użytkownika o emailu: " + email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    System.out.println("Nie znaleziono użytkownika o emailu: " + email);
                    return new UsernameNotFoundException("Nie znaleziono użytkownika o emailu: " + email);
                });

        return new CustomUserDetails(user);
    }

    /**
     * Ładuje użytkownika na podstawie ID.
     *
     * @param id ID użytkownika
     * @return Obiekt UserDetails
     * @throws UsernameNotFoundException Jeśli użytkownik nie zostanie znaleziony
     */
    public UserDetails loadUserById(Long id) throws UsernameNotFoundException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Użytkownik nie znaleziony o ID: " + id));

        return new CustomUserDetails(user);
    }
}
