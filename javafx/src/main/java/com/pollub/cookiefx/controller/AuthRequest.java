package com.pollub.cookiefx.controller;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class AuthRequest {
    private String email;
    private String haslo;

    public AuthRequest() {}

    public AuthRequest(String email, String haslo) {
        this.email = email;
        this.haslo = haslo;
    }

    @Override
    public String toString() {
        return "AuthRequest{email='" + email + "', haslo='[UKRYTE]'}";
    }
}
