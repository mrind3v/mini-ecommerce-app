package com.example.e_commerce.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

@Data
@Document(collection = "cart_items")
public class CartItem {
    @Id
    private String id;
    private String userId;
    private String productId;
    private Integer quantity;

    // Optional: For fetching product details directly if needed,
    // but typically we keep IDs and fetch separately or aggregate.
    // However, for simplicity in response, we might populate it explicitly in DTOs.
}
