package com.example.e_commerce.service;

import com.example.e_commerce.model.Order;
import com.example.e_commerce.model.Payment;
import com.example.e_commerce.repository.OrderRepository;
import com.example.e_commerce.repository.PaymentRepository;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    private RazorpayClient razorpayClient;

    @PostConstruct
    public void init() throws RazorpayException {
        this.razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
    }

    public Payment createPayment(String orderId, Double amount) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!"CREATED".equals(order.getStatus())) {
            throw new RuntimeException("Order is not in CREATED state");
        }

        try {
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", (int) (amount * 100)); // Amount in paise
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "txn_" + orderId);

            com.razorpay.Order razorpayOrder = razorpayClient.orders.create(orderRequest);
            String razorpayOrderId = razorpayOrder.get("id");

            Payment payment = new Payment();
            payment.setOrderId(orderId);
            payment.setAmount(amount);
            payment.setStatus("PENDING");
            payment.setPaymentId(razorpayOrderId); // Storing Razorpay Order ID here as paymentId for tracking
            return paymentRepository.save(payment);

        } catch (RazorpayException e) {
            throw new RuntimeException("Razorpay error: " + e.getMessage());
        }
    }

    public void processPaymentWebhook(String payload) {
        // Simplified webhook processing
        JSONObject json = new JSONObject(payload);

        // Check if event is payment.captured
        if (json.has("event") && json.getString("event").equals("payment.captured")) {
            JSONObject paymentEntity = json.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity");
            String razorpayOrderId = paymentEntity.getString("order_id");
            String status = paymentEntity.getString("status");

            if ("captured".equals(status)) {
                // Find payment by razorpay order id (which we stored in paymentId field)
                Payment payment = paymentRepository.findByPaymentId(razorpayOrderId)
                        .orElseThrow(() -> new RuntimeException(
                                "Payment not found for Razorpay Order ID: " + razorpayOrderId));

                payment.setStatus("SUCCESS");
                paymentRepository.save(payment);

                Order order = orderRepository.findById(payment.getOrderId())
                        .orElseThrow(() -> new RuntimeException("Order not found"));

                order.setStatus("PAID");
                orderRepository.save(order);
            }
        }
    }
}
