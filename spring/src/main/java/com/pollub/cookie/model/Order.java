package com.pollub.cookie.model;

import lombok.Data;
import lombok.Setter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Getter
    @NotNull(message = "Data zamówienia jest wymagana")
    @Column(nullable = false)
    private LocalDateTime datazamowienia;

    @NotNull(message = "Status zamówienia jest wymagany")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @NotNull(message = "Całkowita cena jest wymagana")
    @Column(nullable = false)
    private BigDecimal calkowitaCena;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uzytkownik_id", nullable = false)
    private User uzytkownik;

    @OneToMany(mappedBy = "zamowienie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> pozycjeZamowienia;
    @NotNull(message = "Adres jest wymagany")
    @Column(nullable = false)
    private String adres;

    @NotNull(message = "Numer telefonu jest wymagany")
    @Column(nullable = false)
    private String numerTelefonu;
}
