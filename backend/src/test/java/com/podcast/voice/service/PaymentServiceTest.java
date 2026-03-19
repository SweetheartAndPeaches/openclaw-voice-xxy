package com.podcast.voice.service;

import com.podcast.voice.entity.PaymentEntity;
import com.podcast.voice.entity.SubscriptionEntity;
import com.podcast.voice.entity.UserEntity;
import com.podcast.voice.repository.PaymentRepository;
import com.podcast.voice.repository.SubscriptionRepository;
import com.podcast.voice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * PaymentService 测试类
 */
@TestPropertySource(properties = {
    "payment.stripe.secret-key=test_secret_key",
    "payment.stripe.publishable-key=test_publishable_key"
})
public class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PaymentService paymentService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testRecordSuccessfulPayment() {
        // Given
        String paymentIntentId = "pi_123456";
        Long userId = 1L;
        Long amount = 1000L; // $10.00 in cents
        String currency = "usd";
        String description = "Test payment";

        // When
        paymentService.recordSuccessfulPayment(paymentIntentId, userId, amount, currency, description);

        // Then
        verify(paymentRepository, times(1)).save(any(PaymentEntity.class));
    }

    @Test
    public void testRecordSubscription() {
        // Given
        String subscriptionId = "sub_123456";
        Long userId = 1L;
        String status = "active";
        Long currentPeriodStart = System.currentTimeMillis() / 1000;
        Long currentPeriodEnd = (System.currentTimeMillis() + 2592000000L) / 1000; // +30 days
        String planId = "plan_123456";

        // When
        paymentService.recordSubscription(subscriptionId, userId, status, 
            currentPeriodStart, currentPeriodEnd, planId);

        // Then
        verify(subscriptionRepository, times(1)).save(any(SubscriptionEntity.class));
    }

    @Test
    public void testGetUserPaymentHistory() {
        // Given
        Long userId = 1L;
        PaymentEntity payment1 = new PaymentEntity();
        payment1.setId(1L);
        payment1.setUserId(userId);
        payment1.setAmount(BigDecimal.valueOf(1000));
        payment1.setCurrency("usd");
        payment1.setStatus("succeeded");
        payment1.setCreatedAt(LocalDateTime.now());

        PaymentEntity payment2 = new PaymentEntity();
        payment2.setId(2L);
        payment2.setUserId(userId);
        payment2.setAmount(BigDecimal.valueOf(2000));
        payment2.setCurrency("usd");
        payment2.setStatus("succeeded");
        payment2.setCreatedAt(LocalDateTime.now().minusDays(1));

        List<PaymentEntity> expectedPayments = Arrays.asList(payment2, payment1);
        when(paymentRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(expectedPayments);

        // When
        List<PaymentEntity> actualPayments = paymentService.getUserPaymentHistory(userId);

        // Then
        assertEquals(2, actualPayments.size());
        assertEquals(expectedPayments, actualPayments);
        verify(paymentRepository, times(1)).findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Test
    public void testGetUserCurrentSubscription() {
        // Given
        Long userId = 1L;
        SubscriptionEntity subscription = new SubscriptionEntity();
        subscription.setId(1L);
        subscription.setUserId(userId);
        subscription.setPlanId("plan_123456");
        subscription.setPlanName("Premium Plan");
        subscription.setPrice(9.99);
        subscription.setCurrency("usd");
        subscription.setStartDate(LocalDateTime.now());
        subscription.setEndDate(LocalDateTime.now().plusMonths(1));
        subscription.setActive(true);
        subscription.setStripeSubscriptionId("sub_123456");

        when(subscriptionRepository.findActiveByUserId(userId)).thenReturn(Optional.of(subscription));

        // When
        Optional<SubscriptionEntity> actualSubscription = paymentService.getUserCurrentSubscription(userId);

        // Then
        assertTrue(actualSubscription.isPresent());
        assertEquals(subscription, actualSubscription.get());
        verify(subscriptionRepository, times(1)).findActiveByUserId(userId);
    }
}