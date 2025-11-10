package com.example.learnquiz_fe.data.model.payment.response;

import com.google.gson.annotations.SerializedName;

public class PayOSGetOrderResponse {
    @SerializedName("code")
    private String code;

    @SerializedName("desc")
    private String desc;

    @SerializedName("data")
    private GetOrderResponse data;

    @SerializedName("signature")
    private String signature;

    public String getCode() { return code; }
    public String getDesc() { return desc; }
    public GetOrderResponse getData() { return data; }
    public String getSignature() { return signature; }
}

