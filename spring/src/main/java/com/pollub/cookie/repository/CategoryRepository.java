package com.pollub.cookie.repository;

import com.pollub.cookie.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Sprawdza, czy kategoria o danej nazwie istnieje.
     *
     * @param nazwa Nazwa kategorii
     * @return True jeśli kategoria istnieje, inaczej false
     */
    boolean existsByNazwa(String nazwa);
}
