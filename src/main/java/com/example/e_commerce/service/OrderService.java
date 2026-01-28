package com.example.e_commerce.service;

import com.example.e_commerce.dto.OrderResponse;
import com.example.e_commerce.model.*;
import com.example.e_commerce.repository.*;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public OrderResponse createOrder(String userId) {
        // 1. Get cart items
        List<CartItem> cartItems = cartRepository.findByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // 2. Calculate total and verify stock
        double totalAmount = 0.0;
        for (CartItem item : cartItems) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + item.getProductId()));

            if (product.getStock() < item.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }

            totalAmount += product.getPrice() * item.getQuantity();
        }

        // 3. Create Order
        Order order = new Order();
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus("CREATED");
        order = orderRepository.save(order);

        // 4. Create OrderItems and Update Stock
        for (CartItem item : cartItems) {
            Product product = productRepository.findById(item.getProductId()).get();

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(item.getProductId());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPrice(product.getPrice());
            orderItemRepository.save(orderItem);

            // Update Stock
            product.setStock(product.getStock() - item.getQuantity());
            productRepository.save(product);
        }

        // 5. Clear Cart
        cartRepository.deleteByUserId(userId);

        // Build Response
        return getOrderDetails(order.getId());
    }

    public OrderResponse getOrderDetails(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);

        OrderResponse response = modelMapper.map(order, OrderResponse.class);
        response.setItems(items);
        response.setPayment(payment);

        return response;
    }
}
