package com.pollub.cookie.controller;

import com.pollub.cookie.dto.CartDTO;
import com.pollub.cookie.dto.CartItemRequestDTO;
import com.pollub.cookie.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/carts")
public class CartController {

    private final CartService cartService;

    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * Pobiera koszyk zalogowanego użytkownika.
     *
     * @param authentication Obiekt uwierzytelniania
     * @return Koszyk
     */
    @Operation(summary = "Pobiera koszyk zalogowanego użytkownika", description = "Endpoint do pobierania aktualnego koszyka użytkownika.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pomyślnie pobrano koszyk",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CartDTO.class),
                            examples = @ExampleObject(value = "{ \"id\": 1, \"userId\": 1, \"items\": [ { \"productId\": 2, \"quantity\": 3 } ] }"))),
            @ApiResponse(responseCode = "401", description = "Nieautoryzowany dostęp",
                    content = @Content)
    })
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CartDTO> getMyCart(Authentication authentication) {
        CartDTO cartDTO = cartService.getCartByUserEmail(authentication.getName());
        return ResponseEntity.ok(cartDTO);
    }

    /**
     * Dodaje produkt do koszyka.
     *
     * @param cartItemRequestDTO Dane produktu do dodania
     * @param authentication     Obiekt uwierzytelniania
     * @return Zaktualizowany koszyk
     */
    @Operation(summary = "Dodaje produkt do koszyka", description = "Endpoint do dodawania nowego produktu do koszyka użytkownika.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produkt został dodany do koszyka",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CartDTO.class),
                            examples = @ExampleObject(value = "{ \"id\": 1, \"userId\": 1, \"items\": [ { \"productId\": 2, \"quantity\": 4 } ] }"))),
            @ApiResponse(responseCode = "400", description = "Błędne dane wejściowe",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Nieautoryzowany dostęp",
                    content = @Content)
    })
    @PostMapping("/add")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CartDTO> addToCart(
            @Valid @RequestBody CartItemRequestDTO cartItemRequestDTO,
            Authentication authentication) {
        CartDTO updatedCart = cartService.addToCart(authentication.getName(), cartItemRequestDTO);
        return ResponseEntity.ok(updatedCart);
    }

    /**
     * Aktualizuje ilość produktu w koszyku.
     *
     * @param cartItemRequestDTO Dane produktu do aktualizacji
     * @param authentication     Obiekt uwierzytelniania
     * @return Zaktualizowany koszyk
     */
    @Operation(summary = "Aktualizuje ilość produktu w koszyku", description = "Endpoint do zmiany ilości istniejącego produktu w koszyku użytkownika.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ilość produktu została zaktualizowana",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CartDTO.class),
                            examples = @ExampleObject(value = "{ \"id\": 1, \"userId\": 1, \"items\": [ { \"productId\": 2, \"quantity\": 5 } ] }"))),
            @ApiResponse(responseCode = "400", description = "Błędne dane wejściowe",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Nieautoryzowany dostęp",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Produkt nie został znaleziony w koszyku",
                    content = @Content)
    })
    @PutMapping("/update")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CartDTO> updateCartItem(
            @Valid @RequestBody CartItemRequestDTO cartItemRequestDTO,
            Authentication authentication) {
        CartDTO updatedCart = cartService.updateCartItem(authentication.getName(), cartItemRequestDTO);
        return ResponseEntity.ok(updatedCart);
    }

    /**
     * Usuwa produkt z koszyka.
     *
     * @param productId      ID produktu do usunięcia
     * @param authentication Obiekt uwierzytelniania
     * @return Zaktualizowany koszyk
     */
    @Operation(summary = "Usuwa produkt z koszyka", description = "Endpoint do usuwania produktu z koszyka użytkownika na podstawie jego ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produkt został usunięty z koszyka",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CartDTO.class),
                            examples = @ExampleObject(value = "{ \"id\": 1, \"userId\": 1, \"items\": [ { \"productId\": 3, \"quantity\": 2 } ] }"))),
            @ApiResponse(responseCode = "401", description = "Nieautoryzowany dostęp",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Produkt nie został znaleziony w koszyku",
                    content = @Content)
    })
    @DeleteMapping("/remove/{productId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CartDTO> removeFromCart(
            @PathVariable Long productId,
            Authentication authentication) {
        CartDTO updatedCart = cartService.removeFromCart(authentication.getName(), productId);
        return ResponseEntity.ok(updatedCart);
    }


}
