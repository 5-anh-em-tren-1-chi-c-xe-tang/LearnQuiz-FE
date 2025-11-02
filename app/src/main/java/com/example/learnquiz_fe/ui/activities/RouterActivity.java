package com.example.learnquiz_fe.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.learnquiz_fe.MainActivity;

/** * Activity điều hướng không có giao diện.
 * Đây là điểm khởi đầu của ứng dụng.
 * Nhiệm vụ của nó là kiểm tra trạng thái đăng nhập (dựa vào token)
 * và điều hướng đến MainActivity hoặc LoginActivity tương ứng.
 */
public class RouterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Lấy SharedPreferences
        SharedPreferences prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        String token = prefs.getString("auth_token", null);

        Intent intent;
        // Kiểm tra xem token có tồn tại và không rỗng hay không
        if (token != null && !token.isEmpty()) {
            // Nếu có token, chuyển đến MainActivity
            intent = new Intent(RouterActivity.this, MainActivity.class);
        } else {
            // Nếu không có token, chuyển đến LoginActivity
            intent = new Intent(RouterActivity.this, LoginActivity.class);
        }

        // Bắt đầu Activity mới
        startActivity(intent);

        // Đóng RouterActivity ngay lập tức để người dùng không thể quay lại nó
        finish();
    }
}
