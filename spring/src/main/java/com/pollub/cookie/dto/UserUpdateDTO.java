package com.pollub.cookie.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class UserUpdateDTO {

    @Email(message = "Nieprawidłowy format email")
    private String email;
    private String haslo;
    private String imie;
    private String nazwisko;
    private String adres;
    @Pattern(regexp = "\\d{9}", message = "Numer telefonu musi składać się z 9 cyfr")
    private String numerTelefonu;

}
