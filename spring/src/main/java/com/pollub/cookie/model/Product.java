package com.pollub.cookie.model;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nazwa produktu jest wymagana")
    @Column(nullable = false)
    private String nazwa;

    @NotBlank(message = "Opis produktu jest wymagany")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String opis;

    @Column(nullable = false)
    @DecimalMin(value = "0.0", inclusive = false, message = "Gramatura musi być większa od zera")
    private BigDecimal gramatura;



    private String zdjecie;

    @NotNull(message = "Ilość na stanie jest wymagana")
    @Positive(message = "Ilość na stanie musi być dodatnia")
    @Column(nullable = false)
    private Integer iloscNaStanie;

    @NotNull(message = "Cena produktu jest wymagana")
    @Positive(message = "Cena musi być dodatnia")
    @Column(nullable = false)
    private BigDecimal cena;


    @ManyToMany
    @JoinTable(
            name = "product_category",
            joinColumns = @JoinColumn(name = "produkt_id"),
            inverseJoinColumns = @JoinColumn(name = "kategoria_id")
    )

    private List<Category> kategorie;

    @OneToMany(mappedBy = "produkt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> zamowienia;

    @OneToMany(mappedBy = "produkt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> koszyki;
}
