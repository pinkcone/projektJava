package com.pollub.cookie.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Data
public class AuthRequestDTO {

    @NotBlank(message = "Email jest wymagany")
    private String email;

    @NotBlank(message = "Hasło jest wymagane")
    private String haslo;

    @Override
    public String toString() {
        return "AuthRequestDTO{email='" + email + "', haslo='[UKRYTE]'}";
    }


}
