package com.pollub.cookie.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;

@Data
public class PlaceOrderDTO {

    @NotNull(message = "Adres jest wymagany")
    private String adres;

    @NotNull(message = "Numer telefonu jest wymagany")
    private String numerTelefonu;


    @Getter
    @JsonProperty("totalPrice")
    @NotNull(message = "Brak ceny")
    private BigDecimal calkowitaCena;


}
