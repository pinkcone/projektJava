package com.pollub.cookie.service;

import com.pollub.cookie.dto.UserDTO;
import com.pollub.cookie.dto.UserUpdateDTO;
import com.pollub.cookie.exception.ResourceNotFoundException;
import com.pollub.cookie.model.Role;
import com.pollub.cookie.model.User;
import com.pollub.cookie.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Tworzy nowego użytkownika.
     *
     * @param userDTO Dane użytkownika
     * @return Utworzony użytkownik
     */
    @Transactional
    public UserDTO createUser(UserDTO userDTO) {
        if(userRepository.findByEmail(userDTO.getEmail()).isPresent()){
            throw new IllegalArgumentException("Email już istnieje: " + userDTO.getEmail());
        }

        User user = mapToEntity(userDTO);

        user.setHaslo(passwordEncoder.encode(userDTO.getHaslo()));

        String rolaString = userDTO.getRola();
        if (rolaString == null || rolaString.isEmpty()) {
            rolaString = "USER";
        }
        user.setRola(mapRolaStringToEnum(rolaString));


        User savedUser = userRepository.save(user);

        return mapToDTO(savedUser);
    }

    /**
     * Pobiera użytkownika po ID.
     *
     * @param id ID użytkownika
     * @return Użytkownik jako UserDTO
     */
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Użytkownik nie znaleziony o ID: " + id));
        return mapToDTO(user);
    }

    /**
     * Pobiera wszystkich użytkowników.
     *
     * @return Lista użytkowników jako UserDTO
     */
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Aktualizuje istniejącego użytkownika.
     *
     * @param id      ID użytkownika do aktualizacji
     * @param userUpdateDTO Nowe dane użytkownika
     * @return Zaktualizowany użytkownik jako UserDTO
     */
    public UserDTO updateUser(Long id, UserUpdateDTO userUpdateDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono użytkownika o ID: " + id));

        if (userUpdateDTO.getEmail() != null) {
            user.setEmail(userUpdateDTO.getEmail());
        }

        if (userUpdateDTO.getHaslo() != null && !userUpdateDTO.getHaslo().isEmpty()) {
            user.setHaslo(new BCryptPasswordEncoder().encode(userUpdateDTO.getHaslo()));
        }

        if (userUpdateDTO.getImie() != null) {
            user.setImie(userUpdateDTO.getImie());
        }

        if (userUpdateDTO.getNazwisko() != null) {
            user.setNazwisko(userUpdateDTO.getNazwisko());
        }

        if (userUpdateDTO.getAdres() != null) {
            user.setAdres(userUpdateDTO.getAdres());
        }

        if (userUpdateDTO.getNumerTelefonu() != null) {
            user.setNumerTelefonu(userUpdateDTO.getNumerTelefonu());
        }

        userRepository.save(user);

        return mapToDTO(user);
    }


    /**
     * Usuwa użytkownika po ID.
     *
     * @param id ID użytkownika do usunięcia
     */
    @Transactional
    public void deleteUser(Long id) {
        if(!userRepository.existsById(id)){
            throw new ResourceNotFoundException("Użytkownik nie znaleziony o ID: " + id);
        }
        userRepository.deleteById(id);
    }


    private UserDTO mapToDTO(User user){
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setImie(user.getImie());
        dto.setNazwisko(user.getNazwisko());
        dto.setAdres(user.getAdres());
        dto.setNumerTelefonu(user.getNumerTelefonu());
        dto.setRola(user.getRola().name());
        return dto;
    }

    private User mapToEntity(UserDTO dto){
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setHaslo(dto.getHaslo());
        user.setImie(dto.getImie());
        user.setNazwisko(dto.getNazwisko());
        user.setAdres(dto.getAdres());
        user.setNumerTelefonu(dto.getNumerTelefonu());
        return user;
    }

    private Role mapRolaStringToEnum(String rolaString){
        try {
            return Role.valueOf(rolaString.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Nieprawidłowa rola: " + rolaString);
        }
    }
    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Użytkownik nie znaleziony o email: " + email));

        return mapToDTO(user);
    }
}
