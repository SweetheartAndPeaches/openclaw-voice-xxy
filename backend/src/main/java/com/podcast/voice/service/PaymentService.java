package com.podcast.voice.service;

import com.podcast.voice.entity.PaymentEntity;
import com.podcast.voice.entity.SubscriptionEntity;
import com.podcast.voice.entity.UserEntity;
import com.podcast.voice.repository.PaymentRepository;
import com.podcast.voice.repository.SubscriptionRepository;
import com.podcast.voice.repository.UserRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.param.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 支付服务类 - 处理Stripe支付集成、订阅管理、用量统计和计费
 * 
 * 功能包括：
 * - 创建Stripe客户
 * - 处理一次性支付
 * - 管理订阅计划
 * - 用量统计和计费
 * - Webhook事件处理
 * 
 * @author OpenClaw-Coder
 */
@Service
public class PaymentService {
    
    @Value("${payment.stripe.secret-key}")
    private String stripeSecretKey;
    
    @Value("${payment.stripe.publishable-key}")
    private String stripePublishableKey;
    
    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    
    public PaymentService(PaymentRepository paymentRepository, 
                         SubscriptionRepository subscriptionRepository,
                         UserRepository userRepository) {
        this.paymentRepository = paymentRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
    }
    
    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }
    
    /**
     * 为用户创建Stripe客户
     * 
     * @param user 用户实体
     * @return Stripe客户ID
     * @throws StripeException 如果创建失败
     */
    public String createCustomer(UserEntity user) throws StripeException {
        CustomerCreateParams params = CustomerCreateParams.builder()
            .setName(user.getUsername())
            .setEmail(user.getEmail())
            .setDescription("Customer for " + user.getUsername())
            .build();
        
        Customer customer = Customer.create(params);
        return customer.getId();
    }
    
    /**
     * 创建支付Intent用于一次性支付
     * 
     * @param amount 支付金额（美分）
     * @param currency 货币类型
     * @param customerId Stripe客户ID
     * @return PaymentIntent客户端密钥
     * @throws StripeException 如果创建失败
     */
    public String createPaymentIntent(Long amount, String currency, String customerId) throws StripeException {
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
            .setAmount(amount)
            .setCurrency(currency)
            .setCustomer(customerId)
            .setAutomaticPaymentMethods(
                PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                    .setEnabled(true)
                    .build()
            )
            .build();
        
        PaymentIntent intent = PaymentIntent.create(params);
        return intent.getClientSecret();
    }
    
    /**
     * 创建订阅
     * 
     * @param customerId Stripe客户ID
     * @param priceId 价格ID
     * @return 订阅ID
     * @throws StripeException 如果创建失败
     */
    public String createSubscription(String customerId, String priceId) throws StripeException {
        SubscriptionCreateParams params = SubscriptionCreateParams.builder()
            .setCustomer(customerId)
            .addItem(
                SubscriptionCreateParams.Item.builder()
                    .setPrice(priceId)
                    .build()
            )
            .build();
        
        Subscription subscription = Subscription.create(params);
        return subscription.getId();
    }
    
    /**
     * 取消订阅
     * 
     * @param subscriptionId 订阅ID
     * @throws StripeException 如果取消失败
     */
    public void cancelSubscription(String subscriptionId) throws StripeException {
        Subscription subscription = Subscription.retrieve(subscriptionId);
        subscription.cancel();
    }
    
    /**
     * 记录支付成功事件
     * 
     * @param paymentIntentId PaymentIntent ID
     * @param userId 用户ID
     * @param amount 金额
     * @param currency 货币
     * @param description 描述
     */
    public void recordSuccessfulPayment(String paymentIntentId, Long userId, Long amount, 
                                      String currency, String description) {
        PaymentEntity payment = new PaymentEntity();
        payment.setPaymentIntentId(paymentIntentId);
        payment.setUserId(userId);
        payment.setAmount(BigDecimal.valueOf(amount));
        payment.setCurrency(currency);
        payment.setDescription(description);
        payment.setStatus("succeeded");
        payment.setCreatedAt(LocalDateTime.now());
        
        paymentRepository.save(payment);
    }
    
    /**
     * 记录订阅事件
     * 
     * @param subscriptionId 订阅ID
     * @param userId 用户ID
     * @param status 状态
     * @param currentPeriodStart 当前周期开始时间
     * @param currentPeriodEnd 当前周期结束时间
     * @param planId 计划ID
     */
    public void recordSubscription(String subscriptionId, Long userId, String status,
                                 Long currentPeriodStart, Long currentPeriodEnd, String planId) {
        SubscriptionEntity subscription = new SubscriptionEntity();
        subscription.setSubscriptionId(subscriptionId);
        subscription.setUserId(userId);
        subscription.setStatus(status);
        subscription.setCurrentPeriodStart(LocalDateTime.ofInstant(
            Instant.ofEpochSecond(currentPeriodStart), java.time.ZoneId.systemDefault()));
        subscription.setCurrentPeriodEnd(LocalDateTime.ofInstant(
            Instant.ofEpochSecond(currentPeriodEnd), java.time.ZoneId.systemDefault()));
        subscription.setPlanId(planId);
        subscription.setCreatedAt(LocalDateTime.now());
        
        subscriptionRepository.save(subscription);
    }
    
    /**
     * 获取用户的支付历史
     * 
     * @param userId 用户ID
     * @return 支付历史列表
     */
    public List<PaymentEntity> getUserPaymentHistory(Long userId) {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    /**
     * 获取用户的当前订阅
     * 
     * @param userId 用户ID
     * @return 当前订阅（如果存在）
     */
    public Optional<SubscriptionEntity> getUserCurrentSubscription(Long userId) {
        return subscriptionRepository.findActiveByUserId(userId);
    }
    
    /**
     * 处理Stripe webhook事件
     * 
     * @param payload Webhook payload
     * @param sigHeader 签名头
     * @return 处理结果
     */
    public String handleWebhookEvent(String payload, String sigHeader) {
        try {
            Event event = Webhook.constructEvent(payload, sigHeader, stripeSecretKey);
            
            switch (event.getType()) {
                case "payment_intent.succeeded":
                    handlePaymentIntentSucceeded(event.getDataObjectDeserializer());
                    break;
                case "customer.subscription.created":
                case "customer.subscription.updated":
                    handleSubscriptionUpdated(event.getDataObjectDeserializer());
                    break;
                case "customer.subscription.deleted":
                    handleSubscriptionDeleted(event.getDataObjectDeserializer());
                    break;
                default:
                    // 忽略其他事件
                    break;
            }
            
            return "success";
        } catch (Exception e) {
            // 记录错误
            return "error";
        }
    }
    
    private void handlePaymentIntentSucceeded(Event.DataObjectDeserializer deserializer) {
        PaymentIntent paymentIntent = deserializer.getObject().orElse(null);
        if (paymentIntent != null) {
            String customerId = paymentIntent.getCustomer();
            Long amount = paymentIntent.getAmount();
            String currency = paymentIntent.getCurrency();
            String description = paymentIntent.getDescription();
            
            // 查找用户
            Optional<UserEntity> userOpt = userRepository.findByCustomerId(customerId);
            if (userOpt.isPresent()) {
                UserEntity user = userOpt.get();
                recordSuccessfulPayment(paymentIntent.getId(), user.getId(), amount, 
                                     currency, description);
            }
        }
    }
    
    private void handleSubscriptionUpdated(Event.DataObjectDeserializer deserializer) {
        Subscription subscription = deserializer.getObject().orElse(null);
        if (subscription != null) {
            String customerId = subscription.getCustomer();
            String subscriptionId = subscription.getId();
            String status = subscription.getStatus();
            Long currentPeriodStart = subscription.getCurrentPeriodStart();
            Long currentPeriodEnd = subscription.getCurrentPeriodEnd();
            String planId = subscription.getItems().getData().get(0).getPrice().getProduct();
            
            // 查找用户
            Optional<UserEntity> userOpt = userRepository.findByCustomerId(customerId);
            if (userOpt.isPresent()) {
                UserEntity user = userOpt.get();
                recordSubscription(subscriptionId, user.getId(), status, 
                                 currentPeriodStart, currentPeriodEnd, planId);
            }
        }
    }
    
    private void handleSubscriptionDeleted(Event.DataObjectDeserializer deserializer) {
        Subscription subscription = deserializer.getObject().orElse(null);
        if (subscription != null) {
            String customerId = subscription.getCustomer();
            
            // 查找用户并更新状态
            Optional<UserEntity> userOpt = userRepository.findByCustomerId(customerId);
            if (userOpt.isPresent()) {
                UserEntity user = userOpt.get();
                // 更新用户订阅状态
                user.setSubscriptionStatus("cancelled");
                userRepository.save(user);
            }
        }
    }
}