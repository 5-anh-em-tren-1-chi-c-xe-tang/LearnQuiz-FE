package com.example.learnquiz_fe.data.model.payment.request;

public class CreatePaymentRequest {
    private String subscriptionPlan; // "monthly" or "yearly"

    public String getSubscriptionPlan() {
        return subscriptionPlan;
    }

    public void setSubscriptionPlan(String subscriptionPlan) {
        this.subscriptionPlan = subscriptionPlan;
    }
}
