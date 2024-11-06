package com.pollub.cookie.controller;

import com.pollub.cookie.dto.OrderDTO;
import com.pollub.cookie.dto.PlaceOrderDTO;
import com.pollub.cookie.exception.ResourceNotFoundException;
import com.pollub.cookie.service.OrderService;
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
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Places a new order.
     *
     * @param placeOrderDTO     Order data
     * @param userDetails       Authenticated user details
     * @return Created order
     */
    @Operation(summary = "Składa nowe zamówienie", description = "Endpoint do składania nowego zamówienia przez zalogowanego użytkownika.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Zamówienie zostało pomyślnie złożone",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OrderDTO.class),
                            examples = @ExampleObject(value = "{ \"id\": 1, \"userEmail\": \"user@example.com\", \"products\": [ { \"productId\": 1, \"quantity\": 2 }, { \"productId\": 3, \"quantity\": 1 } ], \"totalPrice\": 59.99, \"status\": \"PLACED\", \"orderDate\": \"2024-04-27T14:30:00\" }"))),
            @ApiResponse(responseCode = "400", description = "Błędne dane zamówienia",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Nieautoryzowany dostęp",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień do składania zamówienia",
                    content = @Content)
    })
    @PostMapping("/place")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<OrderDTO> placeOrder(@Valid @RequestBody PlaceOrderDTO placeOrderDTO, @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        logger.info("Otrzymano żądanie złożenia zamówienia od użytkownika: {}", email);
        try {
            OrderDTO createdOrder = orderService.placeOrder(email, placeOrderDTO);
            logger.info("Zamówienie zostało pomyślnie złożone dla użytkownika: {}", email);
            return ResponseEntity.status(201).body(createdOrder);
        } catch (Exception e) {
            logger.error("Błąd podczas składania zamówienia dla użytkownika: {}", email, e);
            throw e;
        }
    }

    /**
     * Gets order by ID.
     *
     * @param id ID of the order
     * @return Order
     */
    @Operation(summary = "Pobiera zamówienie po ID", description = "Endpoint do pobierania szczegółów zamówienia na podstawie jego ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pomyślnie pobrano zamówienie",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OrderDTO.class),
                            examples = @ExampleObject(value = "{ \"id\": 1, \"userEmail\": \"user@example.com\", \"products\": [ { \"productId\": 1, \"quantity\": 2 }, { \"productId\": 3, \"quantity\": 1 } ], \"totalPrice\": 59.99, \"status\": \"PLACED\", \"orderDate\": \"2024-04-27T14:30:00\" }"))),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do zamówienia",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Zamówienie nie zostało znalezione",
                    content = @Content)
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long id) {
        OrderDTO orderDTO = orderService.getOrderById(id);
        return ResponseEntity.ok(orderDTO);
    }

    /**
     * Gets all orders.
     *
     * @return List of orders
     */
    @Operation(summary = "Pobiera wszystkie zamówienia", description = "Endpoint do pobierania listy wszystkich zamówień w systemie. Dostępny tylko dla administratorów.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pomyślnie pobrano listę zamówień",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OrderDTO.class),
                            examples = @ExampleObject(value = "[ { \"id\": 1, \"userEmail\": \"user@example.com\", \"products\": [ { \"productId\": 1, \"quantity\": 2 }, { \"productId\": 3, \"quantity\": 1 } ], \"totalPrice\": 59.99, \"status\": \"PLACED\", \"orderDate\": \"2024-04-27T14:30:00\" }, { \"id\": 2, \"userEmail\": \"admin@example.com\", \"products\": [ { \"productId\": 2, \"quantity\": 1 } ], \"totalPrice\": 19.99, \"status\": \"SHIPPED\", \"orderDate\": \"2024-04-28T09:15:00\" } ]"))),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do listy zamówień",
                    content = @Content)
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        List<OrderDTO> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    /**
     * Updates an existing order.
     *
     * @param id       ID of the order to update
     * @param orderDTO New order data
     * @return Updated order
     */
    @Operation(summary = "Aktualizuje istniejące zamówienie", description = "Endpoint do aktualizacji danych istniejącego zamówienia na podstawie jego ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Zamówienie zostało pomyślnie zaktualizowane",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OrderDTO.class),
                            examples = @ExampleObject(value = "{ \"id\": 1, \"userEmail\": \"user@example.com\", \"products\": [ { \"productId\": 1, \"quantity\": 3 }, { \"productId\": 3, \"quantity\": 2 } ], \"totalPrice\": 89.97, \"status\": \"CONFIRMED\", \"orderDate\": \"2024-04-27T14:30:00\" }"))),
            @ApiResponse(responseCode = "400", description = "Błędne dane zamówienia",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Zamówienie nie zostało znalezione",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do aktualizacji zamówienia",
                    content = @Content)
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderDTO> updateOrder(@PathVariable Long id, @Valid @RequestBody OrderDTO orderDTO) {
        OrderDTO updatedOrder = orderService.updateOrder(id, orderDTO);
        return ResponseEntity.ok(updatedOrder);
    }

    /**
     * Deletes an order by ID.
     *
     * @param id ID of the order to delete
     * @return No content
     */
    @Operation(summary = "Usuwa zamówienie po ID", description = "Endpoint do usuwania istniejącego zamówienia na podstawie jego ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Zamówienie zostało pomyślnie usunięte",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Zamówienie nie zostało znalezione",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do usunięcia zamówienia",
                    content = @Content)
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Gets orders for the authenticated user.
     *
     * @param userDetails Authenticated user details
     * @return List of orders
     */
    @Operation(summary = "Pobiera zamówienia użytkownika", description = "Endpoint do pobierania listy zamówień zalogowanego użytkownika.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pomyślnie pobrano listę zamówień",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OrderDTO.class),
                            examples = @ExampleObject(value = "[ { \"id\": 1, \"userEmail\": \"user@example.com\", \"products\": [ { \"productId\": 1, \"quantity\": 2 }, { \"productId\": 3, \"quantity\": 1 } ], \"totalPrice\": 59.99, \"status\": \"PLACED\", \"orderDate\": \"2024-04-27T14:30:00\" }, { \"id\": 3, \"userEmail\": \"user@example.com\", \"products\": [ { \"productId\": 2, \"quantity\": 1 } ], \"totalPrice\": 19.99, \"status\": \"SHIPPED\", \"orderDate\": \"2024-04-28T09:15:00\" } ]"))),
            @ApiResponse(responseCode = "401", description = "Nieautoryzowany dostęp",
                    content = @Content)
    })
    @GetMapping("/my")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<OrderDTO>> getMyOrders(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        List<OrderDTO> orders = orderService.getOrdersByUserEmail(email);
        return ResponseEntity.ok(orders);
    }

    /**
     * Updates the status of an existing order.
     *
     * @param id           ID of the order to update
     * @param statusUpdate New status data
     * @return Updated order
     */
    @Operation(summary = "Aktualizuje status zamówienia", description = "Endpoint do aktualizacji statusu istniejącego zamówienia na podstawie jego ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status zamówienia został pomyślnie zaktualizowany",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OrderDTO.class),
                            examples = @ExampleObject(value = "{ \"id\": 1, \"userEmail\": \"user@example.com\", \"products\": [ { \"productId\": 1, \"quantity\": 2 }, { \"productId\": 3, \"quantity\": 1 } ], \"totalPrice\": 59.99, \"status\": \"CONFIRMED\", \"orderDate\": \"2024-04-27T14:30:00\" }"))),
            @ApiResponse(responseCode = "400", description = "Błędne dane statusu",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Zamówienie nie zostało znalezione",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do aktualizacji statusu zamówienia",
                    content = @Content)
    })
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderDTO> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, String> statusUpdate) {
        String newStatus = statusUpdate.get("status");
        OrderDTO updatedOrder = orderService.updateOrderStatus(id, newStatus);
        return ResponseEntity.ok(updatedOrder);
    }

    /**
     * Cancels an order by ID.
     *
     * @param id ID of the order to cancel
     * @return Updated order
     */
    @Operation(summary = "Anuluje zamówienie po ID", description = "Endpoint do anulowania istniejącego zamówienia na podstawie jego ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Zamówienie zostało pomyślnie anulowane",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OrderDTO.class),
                            examples = @ExampleObject(value = "{ \"id\": 1, \"userEmail\": \"user@example.com\", \"products\": [ { \"productId\": 1, \"quantity\": 2 }, { \"productId\": 3, \"quantity\": 1 } ], \"totalPrice\": 59.99, \"status\": \"CANCELLED\", \"orderDate\": \"2024-04-27T14:30:00\" }"))),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do anulowania zamówienia",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Zamówienie nie zostało znalezione",
                    content = @Content)
    })
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<OrderDTO> cancelOrder(@PathVariable Long id) {
        OrderDTO updatedOrder = orderService.cancelOrder(id);
        return ResponseEntity.ok(updatedOrder);
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
