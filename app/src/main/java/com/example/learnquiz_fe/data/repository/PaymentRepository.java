package com.example.learnquiz_fe.data.repository;

import android.content.Context;
import android.util.Log;

import com.example.learnquiz_fe.data.model.payment.request.CreatePaymentRequest;
import com.example.learnquiz_fe.data.model.payment.response.PayOSCreatePaymentResponse;
import com.example.learnquiz_fe.data.model.payment.response.PayOSGetOrderResponse;
import com.example.learnquiz_fe.data.model.quiz.ApiResponse;
import com.example.learnquiz_fe.data.network.ApiService;
import com.example.learnquiz_fe.data.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentRepository {

    private final ApiService apiService;
    private static final String TAG = "PaymentRepository";
    private final Context context;

    public PaymentRepository(Context context) {
        this.context = context.getApplicationContext();
        this.apiService = RetrofitClient.getInstance(context).getApiService();
    }

    public interface GenericPaymentCallback<T> {
        void onSuccess(T data);
        void onError(String message, int code);
    }

    /**
     * Create a new payment intent
     */
    public void createPaymentIntent(CreatePaymentRequest paymentRequest, GenericPaymentCallback<PayOSCreatePaymentResponse> callback) {
        Call<ApiResponse<PayOSCreatePaymentResponse>> call = apiService.createPaymentIntent(paymentRequest);

        call.enqueue(new Callback<ApiResponse<PayOSCreatePaymentResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<PayOSCreatePaymentResponse>> call, Response<ApiResponse<PayOSCreatePaymentResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<PayOSCreatePaymentResponse> apiResponse = response.body();

                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        Log.d(TAG, "Payment intent created successfully");
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        Log.e(TAG, "API error: " + apiResponse.getMessage());
                        callback.onError(apiResponse.getMessage(), response.code());
                    }
                } else {
                    String errorMsg = handleErrorResponse(response.code());
                    Log.e(TAG, "HTTP error " + response.code() + ": " + errorMsg);
                    callback.onError(errorMsg, response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PayOSCreatePaymentResponse>> call, Throwable t) {
                Log.e(TAG, "Network error: " + t.getMessage());
                callback.onError("Network error: " + t.getMessage(), -1);
            }
        });
    }

    /**
     * Get an existing PayOS order
     */
    public void getOrder(int orderId, GenericPaymentCallback<PayOSGetOrderResponse> callback) {
        Call<ApiResponse<PayOSGetOrderResponse>> call = apiService.getOrder(orderId);

        call.enqueue(new Callback<ApiResponse<PayOSGetOrderResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<PayOSGetOrderResponse>> call, Response<ApiResponse<PayOSGetOrderResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<PayOSGetOrderResponse> apiResponse = response.body();

                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        Log.d(TAG, "Order info retrieved successfully");
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        Log.e(TAG, "API error: " + apiResponse.getMessage());
                        callback.onError(apiResponse.getMessage(), response.code());
                    }
                } else {
                    String errorMsg = handleErrorResponse(response.code());
                    Log.e(TAG, "HTTP error " + response.code() + ": " + errorMsg);
                    callback.onError(errorMsg, response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PayOSGetOrderResponse>> call, Throwable t) {
                Log.e(TAG, "Network error: " + t.getMessage());
                callback.onError("Network error: " + t.getMessage(), -1);
            }
        });
    }

    private String handleErrorResponse(int code) {
        switch (code) {
            case 400: return "Bad Request";
            case 401: return "Unauthorized";
            case 403: return "Forbidden";
            case 404: return "Not Found";
            case 500: return "Server Error";
            default:  return "Unexpected error";
        }
    }
}

