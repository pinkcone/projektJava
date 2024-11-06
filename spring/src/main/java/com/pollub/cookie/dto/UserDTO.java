package com.pollub.cookie.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.Setter;
import lombok.Getter;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

@Data
public class UserDTO {
    @Setter
    @Getter
    private Long id;
    @Email(message = "Nieprawidłowy format email")
    @NotBlank(message = "Email jest wymagany")
    private String email;

    @NotBlank(message = "Hasło jest wymagane")
    @Size(min = 6, message = "Hasło musi mieć przynajmniej 6 znaków")
    private String haslo;

    private String imie;
    private String nazwisko;
    private String adres;
    @Pattern(regexp = "\\d{9}", message = "Numer telefonu musi składać się z 9 cyfr")
    private String numerTelefonu;

    private String rola;
    private List<Long> zamowieniaIds;
    private Long koszykId;

}
