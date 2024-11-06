package com.pollub.cookiefx.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderItemDTO {

    private Long id;

    private Integer ilosc;

    private BigDecimal cena;

    private Long produktId;

    private ProductDTO produkt;

}