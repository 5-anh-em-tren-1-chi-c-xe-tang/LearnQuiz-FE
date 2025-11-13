package com.example.learnquiz_fe.ui.activities.payment;

import android.content.Intent;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.learnquiz_fe.MainActivity;
import com.example.learnquiz_fe.R;
import com.example.learnquiz_fe.data.network.RetrofitClient;

public class PaySuccessActivity extends AppCompatActivity {
    Button btnReturnMainSuccess;

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_success);

        // Update new user status here. Put it in the RetrofitClient
        RetrofitClient.getInstance(this).setPremium(true);

        btnReturnMainSuccess = findViewById(R.id.btnReturnMainSuccess);
        btnReturnMainSuccess.setOnClickListener(v -> {
            Intent intent = new Intent(PaySuccessActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}
