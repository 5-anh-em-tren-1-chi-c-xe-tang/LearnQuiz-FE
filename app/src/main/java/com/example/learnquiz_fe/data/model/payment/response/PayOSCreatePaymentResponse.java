package com.example.learnquiz_fe.data.model.payment.response;

import com.google.gson.annotations.SerializedName;

public class PayOSCreatePaymentResponse {
    @SerializedName("code")
    private String code;

    @SerializedName("desc")
    private String desc;

    @SerializedName("data")
    private CreatePaymentResponse data;

    @SerializedName("signature")
    private String signature;

    public String getCode() { return code; }
    public String getDesc() { return desc; }
    public CreatePaymentResponse getData() { return data; }
    public String getSignature() { return signature; }
}
