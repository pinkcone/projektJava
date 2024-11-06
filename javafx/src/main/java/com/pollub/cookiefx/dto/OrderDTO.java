package com.pollub.cookiefx.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@Data
public class OrderDTO {
    private Long id;
    private LocalDateTime datazamowienia;
    private String status;
    private BigDecimal calkowitaCena;
    private List<Long> pozycjeZamowieniaIds;
    private Long uzytkownikId;
    private String adres;
    private String numerTelefonu;
    private List<OrderItemDTO> pozycjeZamowienia;
}
