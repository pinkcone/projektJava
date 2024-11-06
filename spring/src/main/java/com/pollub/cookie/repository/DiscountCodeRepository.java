package com.pollub.cookie.repository;

import com.pollub.cookie.model.DiscountCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DiscountCodeRepository extends JpaRepository<DiscountCode, Long> {

    /**
     * Sprawdza, czy kod rabatowy o danym kodzie istnieje.
     *
     * @param kod Kod rabatowy
     * @return True jeśli kod istnieje, inaczej false
     */
    boolean existsByKod(String kod);

    /**
     * Znajduje kod rabatowy po jego kodzie.
     *
     * @param kod Kod rabatowy
     * @return Opcjonalny obiekt DiscountCode
     */
    Optional<DiscountCode> findByKod(String kod);
}
