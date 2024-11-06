package com.pollub.cookie.dto;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class NotificationDTO {
    private Long id;
    private String tresc;
    private boolean przeczytane;
    private LocalDateTime dataUtworzenia;
}
