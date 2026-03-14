package com.podcast.voice.controller;

import com.podcast.voice.entity.PaymentEntity;
import com.podcast.voice.entity.SubscriptionEntity;
import com.podcast.voice.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 支付控制器
 * 处理支付、订阅、账单等相关操作
 */
@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    /**
     * 创建支付会话
     * @param amount 支付金额（分）
     * @param currency 货币类型
     * @param description 支付描述
     * @param userId 用户ID
     * @return 支付会话信息
     */
    @PostMapping("/create-session")
    public ResponseEntity<?> createPaymentSession(
            @RequestParam Long amount,
            @RequestParam String currency,
            @RequestParam String description,
            @RequestParam Long userId) {
        try {
            Map<String, Object> session = paymentService.createPaymentSession(amount, currency, description, userId);
            return ResponseEntity.ok(session);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to create payment session: " + e.getMessage());
        }
    }

    /**
     * 创建订阅
     * @param priceId Stripe价格ID
     * @param userId 用户ID
     * @return 订阅信息
     */
    @PostMapping("/create-subscription")
    public ResponseEntity<?> createSubscription(
            @RequestParam String priceId,
            @RequestParam Long userId) {
        try {
            SubscriptionEntity subscription = paymentService.createSubscription(priceId, userId);
            return ResponseEntity.ok(subscription);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to create subscription: " + e.getMessage());
        }
    }

    /**
     * 取消订阅
     * @param subscriptionId 订阅ID
     * @return 取消结果
     */
    @PostMapping("/cancel-subscription")
    public ResponseEntity<?> cancelSubscription(@RequestParam String subscriptionId) {
        try {
            boolean success = paymentService.cancelSubscription(subscriptionId);
            if (success) {
                return ResponseEntity.ok().body("Subscription cancelled successfully");
            } else {
                return ResponseEntity.badRequest().body("Failed to cancel subscription");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to cancel subscription: " + e.getMessage());
        }
    }

    /**
     * 获取用户支付历史
     * @param userId 用户ID
     * @return 支付历史列表
     */
    @GetMapping("/history")
    public ResponseEntity<?> getPaymentHistory(@RequestParam Long userId) {
        try {
            List<PaymentEntity> payments = paymentService.getPaymentHistory(userId);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to get payment history: " + e.getMessage());
        }
    }

    /**
     * 获取用户订阅信息
     * @param userId 用户ID
     * @return 订阅信息
     */
    @GetMapping("/subscription")
    public ResponseEntity<?> getUserSubscription(@RequestParam Long userId) {
        try {
            SubscriptionEntity subscription = paymentService.getUserSubscription(userId);
            if (subscription != null) {
                return ResponseEntity.ok(subscription);
            } else {
                return ResponseEntity.ok().body("No active subscription");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to get subscription: " + e.getMessage());
        }
    }

    /**
     * Webhook处理Stripe事件
     * @param payload Webhook payload
     * @param signature Stripe签名
     * @return 处理结果
     */
    @PostMapping("/webhook")
    public ResponseEntity<?> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {
        try {
            boolean handled = paymentService.handleStripeWebhook(payload, signature);
            if (handled) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest().body("Webhook not handled");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Webhook handling failed: " + e.getMessage());
        }
    }
}