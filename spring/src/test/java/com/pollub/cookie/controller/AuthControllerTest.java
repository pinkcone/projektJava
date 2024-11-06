package com.pollub.cookie.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pollub.cookie.dto.AuthRequestDTO;
import com.pollub.cookie.dto.AuthResponseDTO;
import com.pollub.cookie.dto.UserDTO;
import com.pollub.cookie.security.JwtTokenProvider;
import com.pollub.cookie.service.AuthService;
import com.pollub.cookie.service.CustomUserDetailsService;
import com.pollub.cookie.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(AuthControllerTest.TestSecurityConfig.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean(name = "inMemoryUserDetailsManager")
    private org.springframework.security.core.userdetails.UserDetailsService inMemoryUserDetailsManager;

    @Autowired
    private ObjectMapper objectMapper;

    private AuthRequestDTO authRequestDTO;
    private AuthResponseDTO authResponseDTO;
    private UserDTO userDTO;

    @TestConfiguration
    static class TestSecurityConfig {

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http.csrf(AbstractHttpConfigurer::disable)
                    .authorizeRequests()
                    .requestMatchers("/api/auth/**").permitAll()
                    .anyRequest().authenticated();

            return http.build();
        }
    }

    @BeforeEach
    void setUp() {
        authRequestDTO = new AuthRequestDTO();
        authRequestDTO.setEmail("user@example.com");
        authRequestDTO.setHaslo("password123"); // Upewnij się, że pole jest poprawnie nazwane

        authResponseDTO = new AuthResponseDTO();
        authResponseDTO.setToken("dummy-jwt-token");

        userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setEmail("newuser@example.com");
        userDTO.setHaslo("password123");
        userDTO.setImie("Jan");
        userDTO.setNazwisko("Kowalski");
        userDTO.setAdres("ul. Kwiatowa 1");
        userDTO.setNumerTelefonu("123456789");
        userDTO.setRola("USER");
    }

    @Test
    void testLoginSuccess() throws Exception {
        Mockito.when(authService.authenticate(any(AuthRequestDTO.class))).thenReturn(authResponseDTO);

        mockMvc.perform(post("/api/auth/login")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("dummy-jwt-token"));
    }

    @Test
    void testLoginFailure() throws Exception {
        Mockito.when(authService.authenticate(any(AuthRequestDTO.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequestDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testRegisterSuccess() throws Exception {
        Mockito.when(userService.createUser(any(UserDTO.class))).thenReturn(userDTO);

        mockMvc.perform(post("/api/auth/register")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.imie").value("Jan"))
                .andExpect(jsonPath("$.nazwisko").value("Kowalski"))
                .andExpect(jsonPath("$.adres").value("ul. Kwiatowa 1"))
                .andExpect(jsonPath("$.numerTelefonu").value("123456789"))
                .andExpect(jsonPath("$.rola").value("USER"));
    }

    @Test
    void testRegisterFailure_EmailAlreadyExists() throws Exception {
        Mockito.when(userService.createUser(any(UserDTO.class)))
                .thenThrow(new IllegalArgumentException("Email już istnieje: newuser@example.com"));

        mockMvc.perform(post("/api/auth/register")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest());
    }
}
