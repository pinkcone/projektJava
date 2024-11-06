package com.pollub.cookie.repository;

import com.pollub.cookie.model.Role;
import com.pollub.cookie.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Znajduje użytkownika po emailu.
     *
     * @param email Email użytkownika
     * @return Opcjonalny obiekt User
     */
    Optional<User> findByEmail(String email);

    /**
     * Sprawdza, czy użytkownik o danym emailu istnieje.
     *
     * @param email Email użytkownika
     * @return True jeśli użytkownik istnieje, inaczej false
     */
    boolean existsByEmail(String email);
    List<User> findByRola(Role rola);
}
