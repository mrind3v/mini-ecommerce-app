package com.example.e_commerce.dto;

import com.example.e_commerce.model.OrderItem;
import com.example.e_commerce.model.Payment;
import lombok.Data;
import java.util.List;

@Data
public class OrderResponse {
    private String id;
    private String userId;
    private Double totalAmount;
    private String status;
    private Payment payment;
    private List<OrderItem> items;
}
