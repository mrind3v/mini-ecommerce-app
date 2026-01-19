package com.example.e_commerce.dto;

import com.example.e_commerce.model.Product;
import lombok.Data;

@Data
public class CartItemResponse {
    private String id;
    private String productId;
    private Integer quantity;
    private Product product;
}
