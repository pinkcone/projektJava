package com.pollub.cookie.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import lombok.Setter;

import java.util.List;

@Data
public class CategoryDTO {

    @Setter
    private Long id;

    @NotBlank(message = "Nazwa kategorii jest wymagana")
    private String nazwa;

    @NotBlank(message = "Opis kategorii jest wymagany")
    private String opis;

    private List<Long> produktyIds;

}
