package com.pollub.cookie.security;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${app.jwtSecret}")
    private String jwtSecret;

    @Value("${app.jwtExpirationInMs}")
    private int jwtExpirationInMs;

    /**
     * Generuje token JWT na podstawie uwierzytelnienia.
     *
     * @param authentication Obiekt uwierzytelnienia
     * @return Token JWT
     */
    public String generateToken(Authentication authentication) {
        CustomUserDetails userPrincipal = (CustomUserDetails) authentication.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .claim("id", userPrincipal.getId())
                .claim("roles", userPrincipal.getAuthorities())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    /**
     * Pobiera email użytkownika z tokenu JWT.
     *
     * @param token Token JWT
     * @return Email użytkownika
     */
    public String getUserEmailFromJWT(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    /**
     * Weryfikuje poprawność tokenu JWT.
     *
     * @param authToken Token JWT
     * @return True jeśli token jest poprawny, inaczej false
     */
    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException ex) {
            // Nieprawidłowy podpis JWT
        } catch (MalformedJwtException ex) {
            // Nieprawidłowy format JWT
        } catch (ExpiredJwtException ex) {
            // Token JWT wygasł
        } catch (UnsupportedJwtException ex) {
            // Nieobsługiwany token JWT
        } catch (IllegalArgumentException ex) {
            // Pusty token JWT
        }
        return false;
    }
}
