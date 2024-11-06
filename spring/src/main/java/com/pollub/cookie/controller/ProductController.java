package com.pollub.cookie.controller;

import com.pollub.cookie.dto.CategoryDTO;
import com.pollub.cookie.dto.ProductCreateDTO;
import com.pollub.cookie.dto.ProductDTO;
import com.pollub.cookie.dto.ProductImportJsonDTO;
import com.pollub.cookie.model.Category;
import com.pollub.cookie.model.Product;
import com.pollub.cookie.repository.ProductRepository;
import com.pollub.cookie.service.ProductService;
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
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Produkty", description = "Operacje związane z produktami")
public class ProductController {

    private final ProductService productService;
    private final ProductRepository productRepository;

    @Autowired
    public ProductController(ProductService productService, ProductRepository productRepository) {
        this.productService = productService;
        this.productRepository = productRepository;
    }

    /**
     * Tworzy nowy produkt.
     *
     * @param productCreateDTO Dane produktu
     * @param zdjecieFile      Plik zdjęcia produktu
     * @return Utworzony produkt
     */
    @Operation(summary = "Tworzy nowy produkt", description = "Endpoint do tworzenia nowego produktu w systemie.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Produkt został pomyślnie utworzony",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductDTO.class),
                            examples = @ExampleObject(value = "{ \"id\": 1, \"nazwa\": \"Laptop\", \"opis\": \"Nowoczesny laptop\", \"gramatura\": 1500, \"zdjecie\": \"laptop.jpg\", \"iloscNaStanie\": 10, \"cena\": 2999.99, \"kategorie\": [ { \"id\": 1, \"nazwa\": \"Elektronika\", \"opis\": \"Kategorie związane z elektroniką.\" } ] }"))),
            @ApiResponse(responseCode = "400", description = "Błędne dane produktu",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Brak dostępu",
                    content = @Content)
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDTO> createProduct(
            @Valid @ModelAttribute ProductCreateDTO productCreateDTO,
            @RequestParam("zdjecie") MultipartFile zdjecieFile) {
        System.out.println("ProductCreateDTO: " + productCreateDTO);
        System.out.println("ZdjecieFile: " + zdjecieFile.getOriginalFilename());
        ProductDTO createdProduct = productService.createProduct(productCreateDTO, zdjecieFile);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    /**
     * Pobiera produkt po ID.
     *
     * @param id ID produktu
     * @return Produkt
     */
    @Operation(summary = "Pobiera produkt po ID", description = "Endpoint do pobierania szczegółów produktu na podstawie jego ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pomyślnie pobrano produkt",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductDTO.class),
                            examples = @ExampleObject(value = "{ \"id\": 1, \"nazwa\": \"Laptop\", \"opis\": \"Nowoczesny laptop\", \"gramatura\": 1500, \"zdjecie\": \"laptop.jpg\", \"iloscNaStanie\": 10, \"cena\": 2999.99, \"kategorie\": [ { \"id\": 1, \"nazwa\": \"Elektronika\", \"opis\": \"Kategorie związane z elektroniką.\" } ] }"))),
            @ApiResponse(responseCode = "404", description = "Produkt nie został znaleziony",
                    content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        ProductDTO productDTO = productService.getProductById(id);
        return ResponseEntity.ok(productDTO);
    }

    /**
     * Pobiera wszystkie produkty.
     *
     * @return Lista produktów
     */
    @Operation(summary = "Pobiera wszystkie produkty", description = "Endpoint do pobierania listy wszystkich produktów w systemie.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pomyślnie pobrano listę produktów",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductDTO.class),
                            examples = @ExampleObject(value = "[ { \"id\": 1, \"nazwa\": \"Laptop\", \"opis\": \"Nowoczesny laptop\", \"gramatura\": 1500, \"zdjecie\": \"laptop.jpg\", \"iloscNaStanie\": 10, \"cena\": 2999.99, \"kategorie\": [ { \"id\": 1, \"nazwa\": \"Elektronika\", \"opis\": \"Kategorie związane z elektroniką.\" } ] }, { \"id\": 2, \"nazwa\": \"Smartfon\", \"opis\": \"Nowy smartfon\", \"gramatura\": 200, \"zdjecie\": \"smartfon.jpg\", \"iloscNaStanie\": 25, \"cena\": 1999.99, \"kategorie\": [ { \"id\": 1, \"nazwa\": \"Elektronika\", \"opis\": \"Kategorie związane z elektroniką.\" } ] } ]"))),
            @ApiResponse(responseCode = "400", description = "Błędne parametry zapytania",
                    content = @Content)
    })
    @GetMapping("/all")
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<ProductDTO> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * Pobiera produkty z opcjonalnym filtrowaniem po kategorii i wyszukiwaniu.
     *
     * @param category Opcjonalne ID kategorii
     * @param search   Opcjonalny termin wyszukiwania
     * @return Lista produktów
     */
    @Operation(summary = "Pobiera produkty z filtrowaniem", description = "Endpoint do pobierania produktów z opcjonalnym filtrowaniem po kategorii i wyszukiwaniem nazwy.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pomyślnie pobrano listę produktów",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductDTO.class),
                            examples = @ExampleObject(value = "[ { \"id\": 1, \"nazwa\": \"Laptop\", \"opis\": \"Nowoczesny laptop\", \"gramatura\": 1500, \"zdjecie\": \"laptop.jpg\", \"iloscNaStanie\": 10, \"cena\": 2999.99, \"kategorie\": [ { \"id\": 1, \"nazwa\": \"Elektronika\", \"opis\": \"Kategorie związane z elektroniką.\" } ] }, { \"id\": 2, \"nazwa\": \"Smartfon\", \"opis\": \"Nowy smartfon\", \"gramatura\": 200, \"zdjecie\": \"smartfon.jpg\", \"iloscNaStanie\": 25, \"cena\": 1999.99, \"kategorie\": [ { \"id\": 1, \"nazwa\": \"Elektronika\", \"opis\": \"Kategorie związane z elektroniką.\" } ] } ]")))
    })
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getProducts(
            @RequestParam(required = false) Long category,
            @RequestParam(required = false) String search
    ) {
        List<Product> products;

        if (category != null && search != null) {
            products = productRepository.findByKategorie_IdAndNazwaContainingIgnoreCase(category, search);
        } else if (category != null) {
            products = productRepository.findByKategorie_Id(category);
        } else if (search != null) {
            products = productRepository.findByNazwaContainingIgnoreCase(search);
        } else {
            products = productRepository.findAll();
        }

        List<ProductDTO> productDTOs = products.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(productDTOs);
    }

    /**
     * Aktualizuje istniejący produkt.
     *
     * @param id               ID produktu do aktualizacji
     * @param productCreateDTO Nowe dane produktu
     * @param zdjecieFile      Opcjonalny plik zdjęcia produktu
     * @return Zaktualizowany produkt
     */
    @Operation(summary = "Aktualizuje istniejący produkt", description = "Endpoint do aktualizacji danych istniejącego produktu na podstawie jego ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produkt został pomyślnie zaktualizowany",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductDTO.class),
                            examples = @ExampleObject(value = "{ \"id\": 1, \"nazwa\": \"Laptop Pro\", \"opis\": \"Zaawansowany laptop\", \"gramatura\": 1600, \"zdjecie\": \"laptop_pro.jpg\", \"iloscNaStanie\": 8, \"cena\": 3499.99, \"kategorie\": [ { \"id\": 1, \"nazwa\": \"Elektronika\", \"opis\": \"Kategorie związane z elektroniką.\" } ] }"))),
            @ApiResponse(responseCode = "400", description = "Błędne dane produktu",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Brak dostępu",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Produkt nie został znaleziony",
                    content = @Content)
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable Long id,
            @Valid @ModelAttribute ProductCreateDTO productCreateDTO,
            @RequestParam(value = "zdjecie", required = false) MultipartFile zdjecieFile) {
        ProductDTO updatedProduct = productService.updateProduct(id, productCreateDTO, zdjecieFile);
        return ResponseEntity.ok(updatedProduct);
    }

    /**
     * Usuwa produkt po ID.
     *
     * @param id ID produktu do usunięcia
     * @return Brak treści
     */
    @Operation(summary = "Usuwa produkt po ID", description = "Endpoint do usuwania istniejącego produktu na podstawie jego ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Produkt został pomyślnie usunięty",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Produkt nie został znaleziony",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do usunięcia produktu",
                    content = @Content)
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Importuje listę produktów z JSON.
     *
     * @param productImportJsonDTOs Lista danych produktów do importu
     * @return Lista utworzonych produktów
     */
    @Operation(summary = "Importuje listę produktów z JSON", description = "Endpoint do importowania wielu produktów jednocześnie z danych w formacie JSON.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Produkty zostały pomyślnie zaimportowane",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductDTO.class),
                            examples = @ExampleObject(value = "[ { \"id\": 1, \"nazwa\": \"Laptop\", \"opis\": \"Nowoczesny laptop\", \"gramatura\": 1500, \"zdjecie\": \"laptop.jpg\", \"iloscNaStanie\": 10, \"cena\": 2999.99, \"kategorie\": [ { \"id\": 1, \"nazwa\": \"Elektronika\", \"opis\": \"Kategorie związane z elektroniką.\" } ] }, { \"id\": 2, \"nazwa\": \"Smartfon\", \"opis\": \"Nowy smartfon\", \"gramatura\": 200, \"zdjecie\": \"smartfon.jpg\", \"iloscNaStanie\": 25, \"cena\": 1999.99, \"kategorie\": [ { \"id\": 1, \"nazwa\": \"Elektronika\", \"opis\": \"Kategorie związane z elektroniką.\" } ] } ]"))),
            @ApiResponse(responseCode = "400", description = "Błędne dane wejściowe",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Brak dostępu",
                    content = @Content)
    })
    @PostMapping("/import/json")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProductDTO>> importProductsFromJson(
            @Valid @RequestBody List<ProductImportJsonDTO> productImportJsonDTOs) {
        List<ProductDTO> createdProducts = productService.importProductsFromJson(productImportJsonDTOs);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProducts);
    }

    /**
     * Eksportuje wszystkie produkty do JSON.
     *
     * @return Lista produktów
     */
    @Operation(summary = "Eksportuje wszystkie produkty do JSON", description = "Endpoint do eksportowania wszystkich produktów w systemie do formatu JSON.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pomyślnie wyeksportowano produkty",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductDTO.class),
                            examples = @ExampleObject(value = "[ { \"id\": 1, \"nazwa\": \"Laptop\", \"opis\": \"Nowoczesny laptop\", \"gramatura\": 1500, \"zdjecie\": \"laptop.jpg\", \"iloscNaStanie\": 10, \"cena\": 2999.99, \"kategorie\": [ { \"id\": 1, \"nazwa\": \"Elektronika\", \"opis\": \"Kategorie związane z elektroniką.\" } ] }, { \"id\": 2, \"nazwa\": \"Smartfon\", \"opis\": \"Nowy smartfon\", \"gramatura\": 200, \"zdjecie\": \"smartfon.jpg\", \"iloscNaStanie\": 25, \"cena\": 1999.99, \"kategorie\": [ { \"id\": 1, \"nazwa\": \"Elektronika\", \"opis\": \"Kategorie związane z elektroniką.\" } ] } ]"))),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do eksportu produktów",
                    content = @Content)
    })
    @GetMapping("/export/json")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProductDTO>> exportProductsToJson() {
        List<ProductDTO> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    private ProductDTO mapToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setNazwa(product.getNazwa());
        dto.setOpis(product.getOpis());
        dto.setGramatura(product.getGramatura());
        dto.setZdjecie(product.getZdjecie());
        dto.setIloscNaStanie(product.getIloscNaStanie());
        dto.setCena(product.getCena());


        List<CategoryDTO> categoryDTOs = product.getKategorie().stream()
                .map(this::mapCategoryToDTO)
                .collect(Collectors.toList());
        dto.setKategorie(categoryDTOs);

        return dto;
    }

    private CategoryDTO mapCategoryToDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setNazwa(category.getNazwa());
        dto.setOpis(category.getOpis());
        return dto;
    }
}
