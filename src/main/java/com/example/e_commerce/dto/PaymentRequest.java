package com.example.e_commerce.dto;

import lombok.Data;

@Data
public class PaymentRequest {
    private String orderId;
    private Double amount;
}
