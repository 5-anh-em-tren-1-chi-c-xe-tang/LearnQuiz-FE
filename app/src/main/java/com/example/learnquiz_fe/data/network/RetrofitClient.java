package com.example.learnquiz_fe.data.network;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.learnquiz_fe.R;
import com.example.learnquiz_fe.data.model.auth.AuthResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Retrofit client singleton
 * Manages HTTP client configuration and API service instance
 */
public class RetrofitClient {

    private static RetrofitClient instance;
    private final Retrofit retrofit;
    private final ApiService apiService;
    private final Context context;

    // START: Thêm hằng số cho SharedPreferences
    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_AUTH_TOKEN = "auth_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "user_name";
    private static final String KEY_EMAIL = "user_email";
    private static final String KEY_ROLE = "user_role";

    /**
     * Private constructor for singleton pattern
     */
    private RetrofitClient(Context context) {
        this.context = context.getApplicationContext();

        // Create Gson with custom date format
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .setLenient()
                .create();

//         Create OkHttp client with interceptors
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(ApiEndpoints.CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(ApiEndpoints.READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(ApiEndpoints.WRITE_TIMEOUT, TimeUnit.SECONDS)
                .hostnameVerifier(((hostname, session) -> true)) // Accept all hostnames (for dev with self-signed certs)
                .retryOnConnectionFailure(true)
                .addInterceptor(new AuthInterceptor(context))
                .addInterceptor(createLoggingInterceptor())
                .build();
//        OkHttpClient okHttpClient = createSecureClient(this.context);

        // Create Retrofit instance
        retrofit = new Retrofit.Builder()
                .baseUrl(ApiEndpoints.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        // Create API service
        apiService = retrofit.create(ApiService.class);
    }

    /**
     * Get singleton instance
     */
    public static synchronized RetrofitClient getInstance(Context context) {
        if (instance == null) {
            instance = new RetrofitClient(context);
        }
        return instance;
    }

    /**
     * Get API service
     */
    public ApiService getApiService() {
        return apiService;
    }

    /**
     * Create logging interceptor for debugging
     */
    private HttpLoggingInterceptor createLoggingInterceptor() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        // Set level to BODY for development, NONE for production
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return interceptor;
    }

    /**
     * Auth interceptor to add Authorization header
     */
    private static class AuthInterceptor implements okhttp3.Interceptor {
        private final Context context;

        public AuthInterceptor(Context context) {
            this.context = context;
        }

        @Override
        public okhttp3.Response intercept(Chain chain) throws java.io.IOException {
            okhttp3.Request original = chain.request();

            // Get token from SharedPreferences
            SharedPreferences prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
            String token = prefs.getString("auth_token", null);

            // Add Authorization header if token exists
            okhttp3.Request.Builder requestBuilder = original.newBuilder();
            if (token != null && !token.isEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer " + token);
            }

            // Add common headers
            requestBuilder
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .method(original.method(), original.body());

            okhttp3.Request request = requestBuilder.build();
            return chain.proceed(request);
        }
    }

    /**
     * Update authentication token
     */
    public void setAuthToken(String token) {
        SharedPreferences prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        prefs.edit().putString("auth_token", token).apply();
    }
    public void saveAuthData(AuthResponse authResponse) {
        if (authResponse == null) return;

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(KEY_USER_ID, authResponse.getUserId());
        editor.putString(KEY_USERNAME, authResponse.getUsername());
        editor.putString(KEY_EMAIL, authResponse.getEmail());
        editor.putString(KEY_ROLE, authResponse.getRole());

        editor.apply();
    }


    /**
     * Clear authentication token
     */
    public void clearAuthToken() {
        SharedPreferences prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        prefs.edit().remove("auth_token").apply();
    }

    public void clearAuthData() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.remove(KEY_AUTH_TOKEN);
        editor.remove(KEY_USER_ID);
        editor.remove(KEY_USERNAME);
        editor.remove(KEY_EMAIL);
        editor.remove(KEY_ROLE);

        editor.apply();
    }
    /**
     * Lấy User ID của người dùng đã đăng nhập.
     * @return User ID hoặc null nếu chưa đăng nhập.
     */
    public String getUserId() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_USER_ID, null);
    }

    /**
     * Lấy Username của người dùng đã đăng nhập.
     * @return Username hoặc null nếu chưa đăng nhập.
     */
    public String getUsername() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_USERNAME, null);
    }

    /**
     * Lấy Email của người dùng đã đăng nhập.
     * @return Email hoặc null nếu chưa đăng nhập.
     */
    public String getEmail() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_EMAIL, null);
    }

    /**
     * Lấy Role (vai trò) của người dùng đã đăng nhập.
     * @return Role hoặc null nếu chưa đăng nhập.
     */
    public String getRole() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_ROLE, null);
    }
    /**
     * Get current authentication token
     */
    public String getAuthToken() {
        SharedPreferences prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        return prefs.getString("auth_token", null);
    }
}
