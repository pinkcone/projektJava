package com.pollub.cookie.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDTO {

    private Long id;

    @NotNull(message = "Data zamówienia jest wymagana")
    private LocalDateTime datazamowienia;

    @NotNull(message = "Status zamówienia jest wymagany")
    private String status;

    @NotNull(message = "Całkowita cena jest wymagana")
    private BigDecimal calkowitaCena;

    @NotNull(message = "Pozycje zamówienia są wymagane")
    @Size(min = 1, message = "Zamówienie musi zawierać co najmniej jedną pozycję")
    private List<Long> pozycjeZamowieniaIds;


    private Long uzytkownikId;

    @NotNull(message = "Adres jest wymagany")
    private String adres;

    @NotNull(message = "Numer telefonu jest wymagany")
    private String numerTelefonu;


    private List<OrderItemDTO> pozycjeZamowienia;

}
