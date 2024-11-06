package com.pollub.cookie.service;

import com.pollub.cookie.dto.AuthRequestDTO;
import com.pollub.cookie.dto.AuthResponseDTO;
import com.pollub.cookie.repository.UserRepository;
import com.pollub.cookie.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       JwtTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Uwierzytelnia użytkownika i generuje token JWT.
     *
     * @param authRequest Dane logowania użytkownika
     * @return Token JWT
     */
    public AuthResponseDTO authenticate(AuthRequestDTO authRequest) {
        try {
            System.out.println("Próba autentykacji użytkownika: " + authRequest.getEmail());
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getEmail(),
                            authRequest.getHaslo()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtTokenProvider.generateToken(authentication);
            System.out.println("Autentykacja udana dla użytkownika: " + authRequest.getEmail());
            return new AuthResponseDTO(token);
        } catch (BadCredentialsException e) {
            System.out.println("Błędne dane logowania dla użytkownika: " + authRequest.getEmail());
            throw e;
        } catch (Exception e) {
            System.out.println("Błąd podczas autentykacji dla użytkownika: " + authRequest.getEmail() + ". Błąd: " + e.getMessage());
            throw e;
        }
    }
}
