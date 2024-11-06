package com.pollub.cookie.repository;

import com.pollub.cookie.model.Cart;
import com.pollub.cookie.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUzytkownik(User uzytkownik);
}
