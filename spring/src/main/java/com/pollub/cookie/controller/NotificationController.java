package com.pollub.cookie.controller;

import com.pollub.cookie.dto.NotificationDTO;
import com.pollub.cookie.exception.ResourceNotFoundException;
import com.pollub.cookie.model.Notification;
import com.pollub.cookie.model.User;
import com.pollub.cookie.repository.NotificationRepository;
import com.pollub.cookie.repository.UserRepository;
import com.pollub.cookie.service.NotificationService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Autowired
    public NotificationController(NotificationRepository notificationRepository, UserRepository userRepository, NotificationService notificationService) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    /**
     * Pobiera powiadomienia użytkownika.
     *
     * @param userDetails Obiekt uwierzytelniania
     * @return Koszyk
     */
    @Operation(summary = "Pobiera powiadomienia użytkownika", description = "Endpoint do pobierania nieprzeczytanych powiadomień zalogowanego użytkownika.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pomyślnie pobrano powiadomienia",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = NotificationDTO.class),
                            examples = @ExampleObject(value = "[ { \"id\": 1, \"tresc\": \"Nowe zamówienie\", \"przeczytane\": false, \"dataUtworzenia\": \"2024-04-25T10:15:30\" }, { \"id\": 2, \"tresc\": \"Zmiana statusu zamówienia\", \"przeczytane\": false, \"dataUtworzenia\": \"2024-04-26T12:00:00\" } ]"))),
            @ApiResponse(responseCode = "401", description = "Nieautoryzowany dostęp",
                    content = @Content)
    })
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificationDTO>> getNotifications(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Użytkownik nie znaleziony"));

        List<Notification> notifications = notificationRepository.findByUzytkownikAndPrzeczytaneFalse(user);


        List<NotificationDTO> notificationDTOs = notifications.stream()
                .map(notification -> {
                    NotificationDTO dto = new NotificationDTO();
                    dto.setId(notification.getId());
                    dto.setTresc(notification.getTresc());
                    dto.setPrzeczytane(notification.isPrzeczytane());
                    dto.setDataUtworzenia(notification.getDataUtworzenia());
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(notificationDTOs);
    }

    /**
     * Oznacza powiadomienie jako przeczytane
     * @param userDetails        Obiekt uwierzytelniania
     * @return resposneEntity
     */
    @Operation(summary = "Oznacza powiadomienie jako przeczytane", description = "Endpoint do oznaczania konkretnego powiadomienia jako przeczytane.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Powiadomienie zostało oznaczone jako przeczytane",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do powiadomienia",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Powiadomienie nie zostało znalezione",
                    content = @Content)
    })
    @PostMapping("/{id}/markAsRead")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Użytkownik nie znaleziony"));

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Powiadomienie nie znalezione"));

        if (!notification.getUzytkownik().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        notification.setPrzeczytane(true);
        notificationRepository.save(notification);
        return ResponseEntity.ok().build();
    }

    /**
     * Oznacza wszystkie powiadomienia użytkownika jako przeczytane.
     *
     * @param userDetails Obiekt uwierzytelniania
     * @return Brak treści
     */
    @Operation(summary = "Oznacza wszystkie powiadomienia jako przeczytane", description = "Endpoint do oznaczania wszystkich powiadomień zalogowanego użytkownika jako przeczytane.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Wszystkie powiadomienia zostały oznaczone jako przeczytane",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Nieautoryzowany dostęp",
                    content = @Content)
    })
    @PostMapping("/markAllAsRead")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal UserDetails userDetails) {
        notificationService.markAllAsReadForUser(userDetails.getUsername());
        return ResponseEntity.ok().build();
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
