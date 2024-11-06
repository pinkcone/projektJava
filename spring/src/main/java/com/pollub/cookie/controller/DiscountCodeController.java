package com.pollub.cookie.controller;

import com.pollub.cookie.dto.DiscountCodeDTO;
import com.pollub.cookie.exception.ResourceNotFoundException;
import com.pollub.cookie.service.DiscountCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/discount-codes")
public class DiscountCodeController {

    private final DiscountCodeService discountCodeService;

    @Autowired
    public DiscountCodeController(DiscountCodeService discountCodeService) {
        this.discountCodeService = discountCodeService;
    }

    /**
     * Tworzy nowy kod rabatowy.
     *
     * @param discountCodeDTO Dane kodu rabatowego
     * @return Utworzony kod rabatowy
     */
    @Operation(summary = "Tworzy nowy kod rabatowy", description = "Endpoint do tworzenia nowego kodu rabatowego w systemie.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Kod rabatowy został pomyślnie utworzony",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DiscountCodeDTO.class),
                            examples = @ExampleObject(value = "{ \"id\": 1, \"kod\": \"DISCOUNT10\", \"procent\": 10, \"dataWygasniecia\": \"2024-12-31\", \"uzywany\": false }"))),
            @ApiResponse(responseCode = "400", description = "Błędne dane wejściowe",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Brak dostępu",
                    content = @Content)
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DiscountCodeDTO> createDiscountCode(@Valid @RequestBody DiscountCodeDTO discountCodeDTO) {
        DiscountCodeDTO createdDiscountCode = discountCodeService.createDiscountCode(discountCodeDTO);
        return ResponseEntity.status(201).body(createdDiscountCode);
    }

    /**
     * Pobiera kod rabatowy po ID.
     *
     * @param id ID kodu rabatowego
     * @return Kod rabatowy
     */
    @Operation(summary = "Pobiera kod rabatowy po ID", description = "Endpoint do pobierania szczegółów kodu rabatowego na podstawie jego ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pomyślnie pobrano kod rabatowy",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DiscountCodeDTO.class),
                            examples = @ExampleObject(value = "{ \"id\": 1, \"kod\": \"DISCOUNT10\", \"procent\": 10, \"dataWygasniecia\": \"2024-12-31\", \"uzywany\": false }"))),
            @ApiResponse(responseCode = "404", description = "Kod rabatowy nie został znaleziony",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Brak dostępu",
                    content = @Content)
    })
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DiscountCodeDTO> getDiscountCodeById(@PathVariable Long id) {
        DiscountCodeDTO discountCodeDTO = discountCodeService.getDiscountCodeById(id);
        return ResponseEntity.ok(discountCodeDTO);
    }

    /**
     * Pobiera wszystkie kody rabatowe.
     *
     * @return Lista kodów rabatowych
     */
    @Operation(summary = "Pobiera wszystkie kody rabatowe", description = "Endpoint do pobierania listy wszystkich kodów rabatowych w systemie.")
    @ApiResponse(responseCode = "200", description = "Pomyślnie pobrano listę kodów rabatowych",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = DiscountCodeDTO.class),
                    examples = @ExampleObject(value = "[ { \"id\": 1, \"kod\": \"DISCOUNT10\", \"procent\": 10, \"dataWygasniecia\": \"2024-12-31\", \"uzywany\": false }, { \"id\": 2, \"kod\": \"DISCOUNT20\", \"procent\": 20, \"dataWygasniecia\": \"2025-01-31\", \"uzywany\": false } ]")))
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DiscountCodeDTO>> getAllDiscountCodes() {
        List<DiscountCodeDTO> discountCodes = discountCodeService.getAllDiscountCodes();
        return ResponseEntity.ok(discountCodes);
    }

    /**
     * Aktualizuje istniejący kod rabatowy.
     *
     * @param id              ID kodu rabatowego do aktualizacji
     * @param discountCodeDTO Nowe dane kodu rabatowego
     * @return Zaktualizowany kod rabatowy
     */
    @Operation(summary = "Aktualizuje istniejący kod rabatowy", description = "Endpoint do aktualizacji danych istniejącego kodu rabatowego na podstawie jego ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Kod rabatowy został pomyślnie zaktualizowany",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DiscountCodeDTO.class),
                            examples = @ExampleObject(value = "{ \"id\": 1, \"kod\": \"DISCOUNT15\", \"procent\": 15, \"dataWygasniecia\": \"2025-12-31\", \"uzywany\": false }"))),
            @ApiResponse(responseCode = "400", description = "Błędne dane wejściowe",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Kod rabatowy nie został znaleziony",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Brak dostępu",
                    content = @Content)
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DiscountCodeDTO> updateDiscountCode(@PathVariable Long id, @Valid @RequestBody DiscountCodeDTO discountCodeDTO) {
        DiscountCodeDTO updatedDiscountCode = discountCodeService.updateDiscountCode(id, discountCodeDTO);
        return ResponseEntity.ok(updatedDiscountCode);
    }

    /**
     * Usuwa kod rabatowy po ID.
     *
     * @param id ID kodu rabatowego do usunięcia
     * @return Brak treści
     */
    @Operation(summary = "Usuwa kod rabatowy po ID", description = "Endpoint do usuwania istniejącego kodu rabatowego na podstawie jego ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Kod rabatowy został pomyślnie usunięty",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Kod rabatowy nie został znaleziony",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Brak dostępu",
                    content = @Content)
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDiscountCode(@PathVariable Long id) {
        discountCodeService.deleteDiscountCode(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Pobiera kod rabatowy po kodzie.
     *
     * @param kod Kod rabatowy
     * @return Kod rabatowy
     */
    @Operation(summary = "Pobiera kod rabatowy po kodzie", description = "Endpoint do pobierania szczegółów kodu rabatowego na podstawie jego kodu.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pomyślnie pobrano kod rabatowy",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DiscountCodeDTO.class),
                            examples = @ExampleObject(value = "{ \"id\": 1, \"kod\": \"DISCOUNT10\", \"procent\": 10, \"dataWygasniecia\": \"2024-12-31\", \"uzywany\": false }"))),
            @ApiResponse(responseCode = "404", description = "Kod rabatowy nie został znaleziony",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Brak dostępu",
                    content = @Content)
    })
    @GetMapping("/code/{kod}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DiscountCodeDTO> getDiscountCodeByKod(@PathVariable String kod) {
        DiscountCodeDTO discountCodeDTO = discountCodeService.getDiscountCodeByKod(kod);
        return ResponseEntity.ok(discountCodeDTO);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String error = ex.getName() + " powinno być typu " + Objects.requireNonNull(ex.getRequiredType()).getName();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleResourceNotFound(ResourceNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }
}
