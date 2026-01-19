package com.example.e_commerce.service;

import com.example.e_commerce.dto.AddToCartRequest;
import com.example.e_commerce.dto.CartItemResponse;
import com.example.e_commerce.model.CartItem;
import com.example.e_commerce.model.Product;
import com.example.e_commerce.repository.CartRepository;
import com.example.e_commerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public CartItem addToCart(AddToCartRequest request) {
        // Validate product exists
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Check stock
        if (product.getStock() < request.getQuantity()) {
            throw new RuntimeException("Insufficient stock");
        }

        // Check if item already in cart
        Optional<CartItem> existingItem = cartRepository.findByUserIdAndProductId(request.getUserId(),
                request.getProductId());

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            return cartRepository.save(item);
        } else {
            CartItem newItem = new CartItem();
            newItem.setUserId(request.getUserId());
            newItem.setProductId(request.getProductId());
            newItem.setQuantity(request.getQuantity());
            return cartRepository.save(newItem);
        }
    }

    public List<CartItemResponse> getUserCart(String userId) {
        List<CartItem> items = cartRepository.findByUserId(userId);
        List<CartItemResponse> responses = new ArrayList<>();

        for (CartItem item : items) {
            CartItemResponse response = new CartItemResponse();
            response.setId(item.getId());
            response.setProductId(item.getProductId());
            response.setQuantity(item.getQuantity());

            Product product = productRepository.findById(item.getProductId()).orElse(null);
            response.setProduct(product);

            responses.add(response);
        }
        return responses;
    }

    public void clearCart(String userId) {
        cartRepository.deleteByUserId(userId);
    }
}
