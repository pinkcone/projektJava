package com.pollub.cookie.repository;

import com.pollub.cookie.model.Product;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Sprawdza, czy produkt o danym ID istnieje.
     *
     * @param id ID produktu
     * @return True jeśli produkt istnieje, inaczej false
     */
    boolean existsById(@NotNull Long id);

    List<Product> findByKategorie_Id(Long categoryId);

    List<Product> findByNazwaContainingIgnoreCase(String nazwa);

    List<Product> findByKategorie_IdAndNazwaContainingIgnoreCase(Long categoryId, String nazwa);
}
