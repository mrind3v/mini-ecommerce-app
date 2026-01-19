package com.example.e_commerce.dto;

import lombok.Data;

@Data
public class AddToCartRequest {
    private String userId;
    private String productId;
    private Integer quantity;
}
