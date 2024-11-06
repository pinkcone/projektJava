package com.pollub.cookie.repository;

import com.pollub.cookie.model.Order;
import com.pollub.cookie.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Znajduje wszystkie zamówienia dla danego użytkownika.
     *
     * @param userId ID użytkownika
     * @return Lista zamówień
     */
    List<Order> findByUzytkownikId(Long userId);
    List<Order> findByUzytkownik(User uzytkownik);
}
