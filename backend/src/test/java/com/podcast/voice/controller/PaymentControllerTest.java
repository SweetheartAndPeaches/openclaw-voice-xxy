package com.podcast.voice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.podcast.voice.entity.PaymentEntity;
import com.podcast.voice.entity.SubscriptionEntity;
import com.podcast.voice.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 支付控制器测试类
 */
@WebMvcTest(PaymentController.class)
public class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    private PaymentEntity paymentEntity;
    private SubscriptionEntity subscriptionEntity;

    @BeforeEach
    void setUp() {
        paymentEntity = new PaymentEntity();
        paymentEntity.setId(1L);
        paymentEntity.setUserId(1L);
        paymentEntity.setAmount(BigDecimal.valueOf(1000));
        paymentEntity.setCurrency("usd");
        paymentEntity.setStatus("succeeded");
        paymentEntity.setPaymentIntentId("pi_123456789");
        paymentEntity.setCreatedAt(LocalDateTime.now());

        subscriptionEntity = new SubscriptionEntity();
        subscriptionEntity.setId(1L);
        subscriptionEntity.setUserId(1L);
        subscriptionEntity.setPlanId("plan_123");
        subscriptionEntity.setPlanName("Premium Plan");
        subscriptionEntity.setPrice(9.99);
        subscriptionEntity.setCurrency("usd");
        subscriptionEntity.setStartDate(LocalDateTime.now());
        subscriptionEntity.setEndDate(LocalDateTime.now().plusMonths(1));
        subscriptionEntity.setActive(true);
        subscriptionEntity.setStripeSubscriptionId("sub_123456789");
        subscriptionEntity.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testCreatePaymentSession_Success() throws Exception {
        // Given
        String expectedSession = "{\"clientSecret\":\"pi_123456_secret\"}";
        
        when(paymentService.createPaymentSession(anyLong(), anyString(), anyString(), anyLong()))
            .thenReturn(objectMapper.readValue(expectedSession, Map.class));

        // When & Then
        mockMvc.perform(post("/api/payment/create-session")
                .param("amount", "1000")
                .param("currency", "usd")
                .param("description", "Test payment")
                .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.clientSecret").value("pi_123456_secret"));

        verify(paymentService, times(1))
            .createPaymentSession(eq(1000L), eq("usd"), eq("Test payment"), eq(1L));
    }

    @Test
    void testCreatePaymentSession_Failure() throws Exception {
        // Given
        when(paymentService.createPaymentSession(anyLong(), anyString(), anyString(), anyLong()))
            .thenThrow(new RuntimeException("Failed to create payment session"));

        // When & Then
        mockMvc.perform(post("/api/payment/create-session")
                .param("amount", "1000")
                .param("currency", "usd")
                .param("description", "Test payment")
                .param("userId", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Failed to create payment session")));

        verify(paymentService, times(1))
            .createPaymentSession(eq(1000L), eq("usd"), eq("Test payment"), eq(1L));
    }

    @Test
    void testCreateSubscription_Success() throws Exception {
        // Given
        when(paymentService.createSubscription(anyString(), anyLong()))
            .thenReturn(subscriptionEntity);

        // When & Then
        mockMvc.perform(post("/api/payment/create-subscription")
                .param("priceId", "price_123")
                .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.planName").value("Premium Plan"))
                .andExpect(jsonPath("$.price").value(9.99));

        verify(paymentService, times(1))
            .createSubscription(eq("price_123"), eq(1L));
    }

    @Test
    void testCancelSubscription_Success() throws Exception {
        // Given
        when(paymentService.cancelSubscription(anyString())).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/payment/cancel-subscription")
                .param("subscriptionId", "sub_123456789"))
                .andExpect(status().isOk())
                .andExpect(content().string("Subscription cancelled successfully"));

        verify(paymentService, times(1)).cancelSubscription("sub_123456789");
    }

    @Test
    void testGetPaymentHistory_Success() throws Exception {
        // Given
        when(paymentService.getPaymentHistory(anyLong()))
            .thenReturn(Arrays.asList(paymentEntity));

        // When & Then
        mockMvc.perform(get("/api/payment/history")
                .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].amount").value(1000))
                .andExpect(jsonPath("$[0].currency").value("usd"));

        verify(paymentService, times(1)).getPaymentHistory(1L);
    }

    @Test
    void testGetUserSubscription_Success() throws Exception {
        // Given
        when(paymentService.getUserSubscription(anyLong()))
            .thenReturn(subscriptionEntity);

        // When & Then
        mockMvc.perform(get("/api/payment/subscription")
                .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.planName").value("Premium Plan"))
                .andExpect(jsonPath("$.price").value(9.99));

        verify(paymentService, times(1)).getUserSubscription(1L);
    }

    @Test
    void testGetUserSubscription_NoSubscription() throws Exception {
        // Given
        when(paymentService.getUserSubscription(anyLong()))
            .thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/payment/subscription")
                .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("No active subscription"));

        verify(paymentService, times(1)).getUserSubscription(1L);
    }

    @Test
    void testHandleWebhook_Success() throws Exception {
        // Given
        String payload = "{\"id\":\"evt_123\",\"type\":\"payment_intent.succeeded\"}";
        String signature = "t=123456,v1=abc123";
        
        when(paymentService.handleStripeWebhook(anyString(), anyString())).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/payment/webhook")
                .content(payload)
                .header("Stripe-Signature", signature)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(paymentService, times(1)).handleStripeWebhook(payload, signature);
    }

    @Test
    void testHandleWebhook_Failure() throws Exception {
        // Given
        String payload = "{\"id\":\"evt_123\",\"type\":\"payment_intent.succeeded\"}";
        String signature = "t=123456,v1=abc123";
        
        when(paymentService.handleStripeWebhook(anyString(), anyString())).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/api/payment/webhook")
                .content(payload)
                .header("Stripe-Signature", signature)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Webhook not handled"));

        verify(paymentService, times(1)).handleStripeWebhook(payload, signature);
    }
}