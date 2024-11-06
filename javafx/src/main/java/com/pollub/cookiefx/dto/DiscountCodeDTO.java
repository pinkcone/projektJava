package com.pollub.cookiefx.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DiscountCodeDTO {

    private Long id;

    private String kod;

    private String typ;

    private Double wartosc;

    private LocalDate dataWaznosci;


}
