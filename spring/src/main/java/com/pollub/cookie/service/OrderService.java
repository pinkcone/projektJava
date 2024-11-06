package com.pollub.cookie.service;

import com.pollub.cookie.dto.OrderDTO;
import com.pollub.cookie.dto.OrderItemDTO;
import com.pollub.cookie.dto.PlaceOrderDTO;
import com.pollub.cookie.dto.ProductDTO;
import com.pollub.cookie.exception.ResourceNotFoundException;
import com.pollub.cookie.model.*;
import com.pollub.cookie.repository.*;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.pollub.cookie.model.Notification;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    private final NotificationRepository notificationRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository,
                        UserRepository userRepository,
                        OrderItemRepository orderItemRepository,
                        ProductRepository productRepository,
                        CartRepository cartRepository, NotificationRepository notificationRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.notificationRepository = notificationRepository;

    }

    /**
     * Places an order based on OrderDTO.
     *
     * @param email    User's email
     * @param orderDTO Order data
     * @return Placed order as OrderDTO
     */
    @Transactional
    public OrderDTO placeOrder(String email, @Valid PlaceOrderDTO orderDTO) {
        logger.info("Starting order placement for user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("User not found with email: {}", email);
                    return new ResourceNotFoundException("User not found with email: " + email);
                });

        Cart cart = cartRepository.findByUzytkownik(user)
                .orElseThrow(() -> {
                    logger.error("Cart not found for user: {}", email);
                    return new ResourceNotFoundException("Cart not found for user: " + email);
                });

        if (cart.getPozycjeKoszyka().isEmpty()) {
            logger.warn("Cart is empty for user: {}", email);
            throw new IllegalArgumentException("Cart is empty. Cannot place order.");
        }

        // Stock validation
        for (CartItem cartItem : cart.getPozycjeKoszyka()) {
            Product product = cartItem.getProdukt();
            if (cartItem.getIlosc() > product.getIloscNaStanie()) {
                logger.warn("Insufficient stock for product '{}'. Available: {}, requested: {}",
                        product.getNazwa(), product.getIloscNaStanie(), cartItem.getIlosc());
                throw new IllegalArgumentException("Product '" + product.getNazwa() + "' has only " + product.getIloscNaStanie() + " units in stock. Cannot order " + cartItem.getIlosc() + " units.");
            }
        }

        try {
            for (CartItem cartItem : cart.getPozycjeKoszyka()) {
                Product product = cartItem.getProdukt();
                product.setIloscNaStanie(product.getIloscNaStanie() - cartItem.getIlosc());
                productRepository.save(product);
                logger.info("Updated stock for product '{}'. New stock: {}",
                        product.getNazwa(), product.getIloscNaStanie());
            }

            Order order = new Order();
            order.setDatazamowienia(LocalDateTime.now());
            order.setStatus(OrderStatus.NOWE);
            order.setCalkowitaCena(orderDTO.getCalkowitaCena());
            order.setUzytkownik(user);
            order.setAdres(orderDTO.getAdres());
            order.setNumerTelefonu(orderDTO.getNumerTelefonu());

            List<OrderItem> orderItems = cart.getPozycjeKoszyka().stream()
                    .map(cartItem -> {
                        OrderItem orderItem = new OrderItem();
                        orderItem.setZamowienie(order);
                        orderItem.setProdukt(cartItem.getProdukt());
                        orderItem.setIlosc(cartItem.getIlosc());
                        orderItem.setCena(cartItem.getCena());
                        return orderItem;
                    })
                    .collect(Collectors.toList());

            order.setPozycjeZamowienia(orderItems);

            Order savedOrder = orderRepository.save(order);
            logger.info("Order with ID {} has been saved for user: {}", savedOrder.getId(), email);
            createNotificationForAdmins("Nowe zamówienie o ID " + savedOrder.getId() + " zostało złożone.");
            cart.getPozycjeKoszyka().clear();
            cart.setCenaCalkowita(BigDecimal.ZERO);
            cartRepository.save(cart);
            logger.info("Cart for user {} has been cleared.", email);

            return mapToDTOWithItems(savedOrder);
        } catch (Exception e) {
            logger.error("Error during order placement for user: {}", email, e);
            throw new RuntimeException("An error occurred during order placement.", e);
        }
    }
    private void createNotificationForAdmins(String message) {
        List<User> admins = userRepository.findByRola(Role.ADMIN);

        for (User admin : admins) {
            Notification notification = new Notification();
            notification.setTresc(message);
            notification.setPrzeczytane(false);
            notification.setDataUtworzenia(LocalDateTime.now());
            notification.setUzytkownik(admin);
            notificationRepository.save(notification);
        }
    }

    /**
     * Gets orders by user's email.
     *
     * @param email User's email
     * @return List of OrderDTO
     */
    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByUserEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        List<Order> orders = orderRepository.findByUzytkownik(user);

        return orders.stream()
                .map(this::mapToDTOWithItems)
                .collect(Collectors.toList());
    }

    /**
     * Creates a new order based on OrderDTO.
     *
     * @param orderDTO Order data
     * @return Created order as OrderDTO
     */
    @Transactional
    public OrderDTO createOrder(OrderDTO orderDTO) {

        Order order = mapToEntity(orderDTO);


        User user = mapUserIdToEntity(orderDTO.getUzytkownikId());
        order.setUzytkownik(user);

        order.setDatazamowienia(orderDTO.getDatazamowienia());

        order.setStatus(mapStatusStringToEnum(orderDTO.getStatus()));

        order.setCalkowitaCena(orderDTO.getCalkowitaCena());

        List<OrderItem> orderItems = mapOrderItemIdsToEntities(orderDTO.getPozycjeZamowieniaIds(), order);
        order.setPozycjeZamowienia(orderItems);

        Order savedOrder = orderRepository.save(order);

        return mapToDTO(savedOrder);
    }

    /**
     * Gets an order by ID.
     *
     * @param id Order ID
     * @return Order as OrderDTO
     */
    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));
        return mapToDTOWithItems(order);
    }

    /**
     * Gets all orders.
     *
     * @return List of OrderDTO
     */
    @Transactional(readOnly = true)
    public List<OrderDTO> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(this::mapToDTOWithItems)
                .collect(Collectors.toList());
    }

    /**
     * Updates an existing order.
     *
     * @param id       Order ID to update
     * @param orderDTO New order data
     * @return Updated order as OrderDTO
     */
    @Transactional
    public OrderDTO updateOrder(Long id, OrderDTO orderDTO) {
        Order existingOrder = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));

        existingOrder.setDatazamowienia(orderDTO.getDatazamowienia());

        existingOrder.setStatus(mapStatusStringToEnum(orderDTO.getStatus()));

        existingOrder.setCalkowitaCena(orderDTO.getCalkowitaCena());

        User user = mapUserIdToEntity(orderDTO.getUzytkownikId());
        existingOrder.setUzytkownik(user);

        existingOrder.setAdres(orderDTO.getAdres());
        existingOrder.setNumerTelefonu(orderDTO.getNumerTelefonu());

        orderItemRepository.deleteAll(existingOrder.getPozycjeZamowienia());
        existingOrder.getPozycjeZamowienia().clear();

        List<OrderItem> updatedOrderItems = mapOrderItemIdsToEntities(orderDTO.getPozycjeZamowieniaIds(), existingOrder);
        existingOrder.setPozycjeZamowienia(updatedOrderItems);

        Order updatedOrder = orderRepository.save(existingOrder);

        return mapToDTOWithItems(updatedOrder);
    }

    /**
     * Deletes an order by ID.
     *
     * @param id Order ID to delete
     */
    @Transactional
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Order not found with ID: " + id);
        }
        orderRepository.deleteById(id);
    }


    /**
     * Maps OrderDTO to Order entity.
     *
     * @param orderDTO Order data
     * @return Order entity
     */
    private Order mapToEntity(OrderDTO orderDTO) {
        Order order = new Order();
        order.setDatazamowienia(orderDTO.getDatazamowienia());
        order.setStatus(mapStatusStringToEnum(orderDTO.getStatus()));
        order.setCalkowitaCena(orderDTO.getCalkowitaCena());
        order.setAdres(orderDTO.getAdres());
        order.setNumerTelefonu(orderDTO.getNumerTelefonu());
        return order;
    }

    /**
     * Maps Order entity to OrderDTO.
     *
     * @param order Order entity
     * @return OrderDTO
     */
    private OrderDTO mapToDTO(Order order) {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(order.getId());
        orderDTO.setDatazamowienia(order.getDatazamowienia());
        orderDTO.setStatus(order.getStatus().name());
        orderDTO.setCalkowitaCena(order.getCalkowitaCena());
        orderDTO.setUzytkownikId(order.getUzytkownik().getId());
        orderDTO.setAdres(order.getAdres());
        orderDTO.setNumerTelefonu(order.getNumerTelefonu());
        // Map order item IDs
        List<Long> orderItemIds = order.getPozycjeZamowienia() != null
                ? order.getPozycjeZamowienia().stream()
                .map(OrderItem::getId)
                .collect(Collectors.toList())
                : new ArrayList<>();
        orderDTO.setPozycjeZamowieniaIds(orderItemIds);
        return orderDTO;
    }

    /**
     * Maps Order entity to OrderDTO with items.
     *
     * @param order Order entity
     * @return OrderDTO with items
     */
    private OrderDTO mapToDTOWithItems(Order order) {
        OrderDTO orderDTO = mapToDTO(order);
        List<OrderItemDTO> orderItems = order.getPozycjeZamowienia().stream()
                .map(this::mapOrderItemToDTO)
                .collect(Collectors.toList());
        orderDTO.setPozycjeZamowienia(orderItems);
        return orderDTO;
    }

    /**
     * Maps OrderItem entity to OrderItemDTO.
     *
     * @param orderItem OrderItem entity
     * @return OrderItemDTO
     */
    private OrderItemDTO mapOrderItemToDTO(OrderItem orderItem) {
        OrderItemDTO orderItemDTO = new OrderItemDTO();
        orderItemDTO.setId(orderItem.getId());
        orderItemDTO.setIlosc(orderItem.getIlosc());
        orderItemDTO.setCena(orderItem.getCena());
        orderItemDTO.setProduktId(orderItem.getProdukt().getId());

        ProductDTO productDTO = mapProductToDTO(orderItem.getProdukt());
        orderItemDTO.setProdukt(productDTO);
        return orderItemDTO;
    }

    /**
     * Maps Product entity to ProductDTO.
     *
     * @param product Product entity
     * @return ProductDTO
     */
    private ProductDTO mapProductToDTO(Product product) {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setId(product.getId());
        productDTO.setNazwa(product.getNazwa());
        productDTO.setCena(product.getCena());
        productDTO.setOpis(product.getOpis());
        productDTO.setZdjecie(product.getZdjecie());

        return productDTO;
    }

    /**
     * Maps String status to OrderStatus enum.
     *
     * @param statusString Status as String
     * @return OrderStatus enum
     */
    private OrderStatus mapStatusStringToEnum(String statusString) {
        try {
            return OrderStatus.valueOf(statusString.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid order status: " + statusString);
        }
    }

    /**
     * Maps a list of OrderItem IDs to entities and assigns them to the order.
     *
     * @param orderItemIds List of OrderItem IDs
     * @param order        Order entity
     * @return List of OrderItem entities
     */
    private List<OrderItem> mapOrderItemIdsToEntities(List<Long> orderItemIds, Order order) {
        if (orderItemIds == null || orderItemIds.isEmpty()) {
            throw new IllegalArgumentException("Order item ID list cannot be empty");
        }

        List<OrderItem> orderItems = orderItemRepository.findAllById(orderItemIds);
        if (orderItems.size() != orderItemIds.size()) {
            throw new ResourceNotFoundException("Some order items were not found for the provided IDs.");
        }

        orderItems.forEach(item -> item.setZamowienie(order));

        return orderItems;
    }

    /**
     * Maps user ID to User entity.
     *
     * @param userId User ID
     * @return User entity
     */
    private User mapUserIdToEntity(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }
    @Transactional
    public OrderDTO updateOrderStatus(Long id, String newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zamówienie nie znalezione o ID: " + id));

        try {
            OrderStatus status = OrderStatus.valueOf(newStatus);
            order.setStatus(status);
            Order updatedOrder = orderRepository.save(order);
            createNotificationForUser(order.getUzytkownik(), "Status Twojego zamówienia o ID " + order.getId() + " został zmieniony na: " + status.name());
            return mapToDTOWithItems(updatedOrder);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Nieprawidłowy status zamówienia: " + newStatus);
        }
    }
    private void createNotificationForUser(User user, String message) {
        Notification notification = new Notification();
        notification.setTresc(message);
        notification.setPrzeczytane(false);
        notification.setDataUtworzenia(LocalDateTime.now());
        notification.setUzytkownik(user);
        notificationRepository.save(notification);
    }
    @Transactional
    public OrderDTO cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zamówienie nie znalezione o ID: " + id));

        if (order.getStatus() == OrderStatus.NOWE || order.getStatus() == OrderStatus.W_TRAKCIE_PRZETWARZANIA) {
            order.setStatus(OrderStatus.ANULOWANE);
            Order updatedOrder = orderRepository.save(order);
            return mapToDTOWithItems(updatedOrder);
        } else {
            throw new IllegalStateException("Nie można anulować zamówienia o statusie: " + order.getStatus());
        }
    }

}
