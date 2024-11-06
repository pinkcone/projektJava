package com.pollub.cookie.dto;

import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Data
public class CartItemRequestDTO {

    @NotNull(message = "ID produktu jest wymagane")
    private Long productId;

    @NotNull(message = "Ilość jest wymagana")
    @Min(value = 1, message = "Ilość musi być przynajmniej 1")
    private Integer quantity;
}
