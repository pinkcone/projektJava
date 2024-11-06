package com.pollub.cookie.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tresc;

    private boolean przeczytane;

    private LocalDateTime dataUtworzenia;

    @ManyToOne
    @JoinColumn(name = "uzytkownik_id")
    private User uzytkownik;
}
