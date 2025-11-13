package com.example.learnquiz_fe.ui.activities.payment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.learnquiz_fe.R;
import com.example.learnquiz_fe.data.model.payment.request.CreatePaymentRequest;
import com.example.learnquiz_fe.data.model.payment.response.GetOrderResponse;
import com.example.learnquiz_fe.data.model.payment.response.PayOSCreatePaymentResponse;
import com.example.learnquiz_fe.data.model.payment.response.CreatePaymentResponse;
import com.example.learnquiz_fe.data.model.payment.response.PayOSGetOrderResponse;
import com.example.learnquiz_fe.data.repository.PaymentRepository;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.logging.Logger;

public class PayActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private ImageView imgQrCode;
    private TextView tvDescription, tvAmount, tvAccountName, tvOrderCode, tvStatus, tvAccountNumber;
    private String planOption;
    private PaymentRepository paymentRepository;

    private Handler handler = new Handler(Looper.getMainLooper());
    private static final int POLL_INTERVAL_MS = 2000; // every 2 seconds
    private boolean isPolling = false;
    private int orderCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);
        paymentRepository = new PaymentRepository(this);
        findViews();
        setupListeners();

        planOption = getIntent().getStringExtra("plan_option");

        setupPaymentInformation();
    }

    private void setupPaymentInformation() {
        CreatePaymentRequest req = new CreatePaymentRequest(planOption);
        paymentRepository.createPaymentIntent(req, new PaymentRepository.GenericPaymentCallback<PayOSCreatePaymentResponse>() {
            @Override
            public void onSuccess(PayOSCreatePaymentResponse data) {
                Log.d("PayActivity", "Payment intent created successfully");
                if (data != null) {
                    bindPaymentData(data);
                    startPollingOrderStatus(); // begin polling after creating order
                }
            }

            @Override
            public void onError(String message, int code) {
                Log.d("PayActivity", "Error creating payment intent: " + message);
                Toast.makeText(PayActivity.this, "Error creating payment", Toast.LENGTH_SHORT).show();
                // Redirect to failed activity
                Intent intent  = new Intent(PayActivity.this, PayFailedActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    private void findViews() {
        btnBack = findViewById(R.id.btnBack);
        imgQrCode = findViewById(R.id.imgQrCode);
        tvDescription = findViewById(R.id.tvDescription);
        tvAmount = findViewById(R.id.tvAmount);
        tvAccountName = findViewById(R.id.tvAccountName);
        tvOrderCode = findViewById(R.id.tvOrderCode);
//        tvStatus = findViewById(R.id.tvPaymentStatus);
        tvAccountNumber = findViewById(R.id.tvAccountNumber);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void bindPaymentData(PayOSCreatePaymentResponse res) {
        var data = res.getData();
        orderCode = data.getOrderCode();
        Locale localeVN = new Locale("vi", "VN");
        NumberFormat currencyVN = NumberFormat.getCurrencyInstance(localeVN);
        String formattedAmount = currencyVN.format(data.getAmount());
        tvDescription.setText(data.getDescription());
        tvAmount.setText(formattedAmount);
        tvAccountName.setText(data.getAccountName());
        tvOrderCode.setText(String.valueOf(orderCode));
        tvAccountNumber.setText(data.getAccountNumber());

        Glide.with(this)
                .load(data.getQrCodeLink())
                .placeholder(R.drawable.img_loading_placeholder)
                .into(imgQrCode);
    }

    private void startPollingOrderStatus() {
        if (isPolling) return;
        isPolling = true;
        handler.post(pollRunnable); // start first immediately
    }

    private final Runnable pollRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isPolling) return;

            paymentRepository.getOrder(orderCode, new PaymentRepository.GenericPaymentCallback<PayOSGetOrderResponse>() {
                @Override
                public void onSuccess(PayOSGetOrderResponse res) {
                    Log.d("PayActivity", "Polled order data: " + res.getData());
                    if ("PAID".equalsIgnoreCase(res.getData().getStatus())) {
//                        tvStatus.setText("Status: Paid ✅");
                        isPolling = false;
                        // Redirect to success activity
                        Intent intent  = new Intent(PayActivity.this, PaySuccessActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
//                        tvStatus.setText("Status: Pending...");
                        // schedule next poll
                        handler.postDelayed(pollRunnable, POLL_INTERVAL_MS);
                    }
                }

                @Override
                public void onError(String message, int code) {
                    Log.w("PayActivity", "Polling error: " + message);
                    // retry again after delay
                    handler.postDelayed(pollRunnable, POLL_INTERVAL_MS);
                }
            });
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isPolling = false;
        handler.removeCallbacksAndMessages(null);
    }
}
