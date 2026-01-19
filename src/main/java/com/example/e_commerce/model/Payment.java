package com.example.e_commerce.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Data
@Document(collection = "payments")
public class Payment {
    @Id
    private String id;
    private String orderId;
    private Double amount;
    private String status; // PENDING, SUCCESS, FAILED
    private String paymentId; // from Razorpay
    private Instant createdAt = Instant.now();
}
