package com.pollub.cookie.service;

import com.pollub.cookie.dto.UserDTO;
import com.pollub.cookie.dto.UserUpdateDTO;
import com.pollub.cookie.exception.ResourceNotFoundException;
import com.pollub.cookie.model.Role;
import com.pollub.cookie.model.User;
import com.pollub.cookie.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void testCreateUserSuccess() {
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("newuser@example.com");
        userDTO.setHaslo("password123");
        userDTO.setImie("Jan");
        userDTO.setNazwisko("Kowalski");
        userDTO.setAdres("ul. Kwiatowa 1");
        userDTO.setNumerTelefonu("123456789");
        userDTO.setRola("USER");

        when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        UserDTO createdUser = userService.createUser(userDTO);

        assertNotNull(createdUser.getId());
        assertEquals("newuser@example.com", createdUser.getEmail());
        assertEquals("Jan", createdUser.getImie());
        assertEquals("Kowalski", createdUser.getNazwisko());
        assertEquals("ul. Kwiatowa 1", createdUser.getAdres());
        assertEquals("123456789", createdUser.getNumerTelefonu());
        assertEquals("USER", createdUser.getRola());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals("newuser@example.com", savedUser.getEmail());
        assertEquals("encodedPassword", savedUser.getHaslo());
        assertEquals(Role.USER, savedUser.getRola());
    }

    @Test
    void testCreateUserEmailAlreadyExists() {
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("existinguser@example.com");
        userDTO.setHaslo("password123");

        when(userRepository.findByEmail("existinguser@example.com")).thenReturn(Optional.of(new User()));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.createUser(userDTO));

        assertEquals("Email już istnieje: existinguser@example.com", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testGetUserByIdSuccess() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setImie("Jan");
        user.setNazwisko("Kowalski");
        user.setAdres("ul. Kwiatowa 1");
        user.setNumerTelefonu("123456789");
        user.setRola(Role.USER);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDTO userDTO = userService.getUserById(1L);

        assertEquals(1L, userDTO.getId());
        assertEquals("user@example.com", userDTO.getEmail());
        assertEquals("Jan", userDTO.getImie());
        assertEquals("Kowalski", userDTO.getNazwisko());
        assertEquals("ul. Kwiatowa 1", userDTO.getAdres());
        assertEquals("123456789", userDTO.getNumerTelefonu());
        assertEquals("USER", userDTO.getRola());
    }

    @Test
    void testGetUserByIdNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(1L));

        assertEquals("Użytkownik nie znaleziony o ID: 1", exception.getMessage());
    }


    @Test
    void testUpdateUserNotFound() {
        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setEmail("updateduser@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(1L, updateDTO));

        assertEquals("Nie znaleziono użytkownika o ID: 1", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testDeleteUserSuccess() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        assertDoesNotThrow(() -> userService.deleteUser(1L));

        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteUserNotFound() {
        when(userRepository.existsById(1L)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(1L));

        assertEquals("Użytkownik nie znaleziony o ID: 1", exception.getMessage());
        verify(userRepository, never()).deleteById(anyLong());
    }
}
