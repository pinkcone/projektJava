package com.pollub.cookie.dto;

import lombok.Data;
import lombok.Setter;
import lombok.Getter;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CartDTO {

    private Long id;

    @NotNull(message = "Cena całkowita jest wymagana")
    private BigDecimal cenaCalkowita;

    @Setter
    @Getter
    @NotNull(message = "Pozycje koszyka są wymagane")
    @Size(min = 1, message = "Koszyk musi zawierać co najmniej jedną pozycję")
    private List<CartItemDTO> pozycjeKoszyka;

    @Setter
    @Getter
    private Long uzytkownikId;


}
