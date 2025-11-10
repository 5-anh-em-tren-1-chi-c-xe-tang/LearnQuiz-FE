package com.example.learnquiz_fe.data.model.payment.response;

import com.google.gson.annotations.SerializedName;

public class CreatePaymentResponse {
    @SerializedName("bin")
    private String bin;

    @SerializedName("accountNumber")
    private String accountNumber;

    @SerializedName("accountName")
    private String accountName;

    @SerializedName("currency")
    private String currency;

    @SerializedName("paymentLink")
    private String paymentLink;

    @SerializedName("amount")
    private int amount;

    @SerializedName("description")
    private String description;

    @SerializedName("orderCode")
    private int orderCode;

    @SerializedName("expiredAt")
    private Integer expiredAt;

    @SerializedName("status")
    private String status;

    @SerializedName("checkoutUrl")
    private String checkoutUrl;

    @SerializedName("qrCode")
    private String qrCode;

    @SerializedName("qrCodeLink")
    private String qrCodeLink;

    // Getters
    public String getBin() { return bin; }
    public String getAccountNumber() { return accountNumber; }
    public String getAccountName() { return accountName; }
    public String getCurrency() { return currency; }
    public String getPaymentLink() { return paymentLink; }
    public int getAmount() { return amount; }
    public String getDescription() { return description; }
    public int getOrderCode() { return orderCode; }
    public Integer getExpiredAt() { return expiredAt; }
    public String getStatus() { return status; }
    public String getCheckoutUrl() { return checkoutUrl; }
    public String getQrCode() { return qrCode; }
    public String getQrCodeLink() { return qrCodeLink; }
}
