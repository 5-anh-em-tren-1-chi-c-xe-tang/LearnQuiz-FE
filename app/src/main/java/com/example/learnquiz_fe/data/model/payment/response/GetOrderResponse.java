package com.example.learnquiz_fe.data.model.payment.response;

import com.google.gson.annotations.SerializedName;

public class GetOrderResponse {
    @SerializedName("id")
    private String id;

    @SerializedName("orderCode")
    private int orderCode;

    @SerializedName("amount")
    private int amount;

    @SerializedName("amountPaid")
    private int amountPaid;

    @SerializedName("amountRemaining")
    private int amountRemaining;

    @SerializedName("status")
    private String status; // "PENDING", "PAID", etc.

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("transactions")
    private Object[] transactions;

    @SerializedName("canceledAt")
    private String canceledAt;

    @SerializedName("cancellationReason")
    private String cancellationReason;

    public String getId() { return id; }
    public int getOrderCode() { return orderCode; }
    public int getAmount() { return amount; }
    public int getAmountPaid() { return amountPaid; }
    public int getAmountRemaining() { return amountRemaining; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
    public Object[] getTransactions() { return transactions; }
    public String getCanceledAt() { return canceledAt; }
    public String getCancellationReason() { return cancellationReason; }
}
