package com.pollub.cookie.service;

import com.pollub.cookie.dto.CartDTO;
import com.pollub.cookie.dto.CartItemDTO;
import com.pollub.cookie.dto.CartItemRequestDTO;
import com.pollub.cookie.dto.ProductDTO;
import com.pollub.cookie.exception.ResourceNotFoundException;
import com.pollub.cookie.model.Cart;
import com.pollub.cookie.model.CartItem;
import com.pollub.cookie.model.Product;
import com.pollub.cookie.model.User;
import com.pollub.cookie.repository.CartItemRepository;
import com.pollub.cookie.repository.CartRepository;
import com.pollub.cookie.repository.ProductRepository;
import com.pollub.cookie.repository.UserRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;

    @Autowired
    public CartService(
            CartRepository cartRepository,
            UserRepository userRepository,
            ProductRepository productRepository,
            CartItemRepository cartItemRepository) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
    }

    @Transactional(readOnly = true)
    public CartDTO getCartByUserEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Użytkownik nie znaleziony o email: " + email));

        Cart cart = cartRepository.findByUzytkownik(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUzytkownik(user);
                    newCart.setCenaCalkowita(BigDecimal.ZERO);
                    newCart.setPozycjeKoszyka(new ArrayList<>());
                    return cartRepository.save(newCart);
                });

        return mapToDTO(cart);
    }

    @Transactional
    public CartDTO addToCart(String email, CartItemRequestDTO cartItemRequestDTO) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Użytkownik nie znaleziony o email: " + email));

        Cart cart = cartRepository.findByUzytkownik(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUzytkownik(user);
                    newCart.setCenaCalkowita(BigDecimal.ZERO);
                    newCart.setPozycjeKoszyka(new ArrayList<>());
                    return cartRepository.save(newCart);
                });

        Product product = productRepository.findById(cartItemRequestDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Produkt nie znaleziony o ID: " + cartItemRequestDTO.getProductId()));


        Optional<CartItem> existingItemOpt = cart.getPozycjeKoszyka().stream()
                .filter(item -> item.getProdukt().getId().equals(product.getId()))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            existingItem.setIlosc(existingItem.getIlosc() + cartItemRequestDTO.getQuantity());
        } else {
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProdukt(product);
            cartItem.setIlosc(cartItemRequestDTO.getQuantity());
            cartItem.setCena(product.getCena());
            cart.getPozycjeKoszyka().add(cartItem);
        }

        cart.setCenaCalkowita(calculateTotalPrice(cart));

        Cart updatedCart = cartRepository.save(cart);
        return mapToDTO(updatedCart);
    }

    @Transactional
    public CartDTO updateCartItem(String email, CartItemRequestDTO cartItemRequestDTO) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Użytkownik nie znaleziony o email: " + email));

        Cart cart = cartRepository.findByUzytkownik(user)
                .orElseThrow(() -> new ResourceNotFoundException("Koszyk nie znaleziony dla użytkownika: " + email));

        CartItem cartItem = cart.getPozycjeKoszyka().stream()
                .filter(item -> item.getProdukt().getId().equals(cartItemRequestDTO.getProductId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Produkt nie znaleziony w koszyku: " + cartItemRequestDTO.getProductId()));

        cartItem.setIlosc(cartItemRequestDTO.getQuantity());


        cart.setCenaCalkowita(calculateTotalPrice(cart));

        Cart updatedCart = cartRepository.save(cart);
        return mapToDTO(updatedCart);
    }

    @Transactional
    public CartDTO removeFromCart(String email, Long productId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Użytkownik nie znaleziony o email: " + email));

        Cart cart = cartRepository.findByUzytkownik(user)
                .orElseThrow(() -> new ResourceNotFoundException("Koszyk nie znaleziony dla użytkownika: " + email));

        CartItem cartItem = cart.getPozycjeKoszyka().stream()
                .filter(item -> item.getProdukt().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Produkt nie znaleziony w koszyku: " + productId));

        cart.getPozycjeKoszyka().remove(cartItem);
        cartItemRepository.delete(cartItem);


        cart.setCenaCalkowita(calculateTotalPrice(cart));

        Cart updatedCart = cartRepository.save(cart);
        return mapToDTO(updatedCart);
    }

    private BigDecimal calculateTotalPrice(@NotNull Cart cart) {
        return cart.getPozycjeKoszyka().stream()
                .map(item -> item.getCena().multiply(BigDecimal.valueOf(item.getIlosc())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    /**
     * Mapuje encję Cart na CartDTO.
     *
     * @param cart Encja Cart
     * @return CartDTO
     */
    private CartDTO mapToDTO(Cart cart) {
        CartDTO cartDTO = new CartDTO();
        cartDTO.setId(cart.getId());
        cartDTO.setCenaCalkowita(cart.getCenaCalkowita());
        cartDTO.setUzytkownikId(cart.getUzytkownik().getId());

        List<CartItemDTO> cartItems = cart.getPozycjeKoszyka() != null
                ? cart.getPozycjeKoszyka().stream()
                .map(this::mapCartItemToDTO)
                .collect(Collectors.toList())
                : new ArrayList<>();
        cartDTO.setPozycjeKoszyka(cartItems);

        return cartDTO;
    }

    /**
     * Mapuje encję CartItem na CartItemDTO.
     *
     * @param cartItem Encja CartItem
     * @return CartItemDTO
     */
    private CartItemDTO mapCartItemToDTO(CartItem cartItem) {
        CartItemDTO cartItemDTO = new CartItemDTO();
        cartItemDTO.setId(cartItem.getId());
        cartItemDTO.setIlosc(cartItem.getIlosc());
        cartItemDTO.setCena(cartItem.getCena());
        cartItemDTO.setProduktId(cartItem.getProdukt().getId());
        cartItemDTO.setProdukt(mapProductToDTO(cartItem.getProdukt()));
        return cartItemDTO;
    }

    /**
     * Mapuje encję Product na ProductDTO.
     *
     * @param product Encja Product
     * @return ProductDTO
     */
    private ProductDTO mapProductToDTO(Product product) {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setId(product.getId());
        productDTO.setNazwa(product.getNazwa());
        productDTO.setCena(product.getCena());
        productDTO.setOpis(product.getOpis());
        productDTO.setZdjecie(product.getZdjecie());
        productDTO.setIloscNaStanie(product.getIloscNaStanie());

        return productDTO;
    }
}
