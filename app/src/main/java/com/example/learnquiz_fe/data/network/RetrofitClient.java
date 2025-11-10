package com.example.learnquiz_fe.data.network;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.learnquiz_fe.R;
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

    private OkHttpClient createSecureClient(Context context) {
        try {
            // 1️⃣ Load the certificate you just exported
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream caInput = context.getResources().openRawResource(R.raw.dev_cert_kiet);
            Certificate ca = cf.generateCertificate(caInput);
            caInput.close();

            // 2️⃣ Put it into a KeyStore
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            // 3️⃣ Create TrustManager based on that KeyStore
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);

            // 4️⃣ Build an SSLContext that uses our TrustManager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), new SecureRandom());
            X509TrustManager trustManager = (X509TrustManager) tmf.getTrustManagers()[0];

            // 5️⃣ Build the OkHttpClient with it
            return new OkHttpClient.Builder()
                    .sslSocketFactory(sslContext.getSocketFactory(), trustManager)
                    .hostnameVerifier((hostname, session) -> true) // relax hostname for 10.0.2.2
                    .addInterceptor(new AuthInterceptor(context))
                    .addInterceptor(createLoggingInterceptor())
                    .connectTimeout(ApiEndpoints.CONNECT_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(ApiEndpoints.READ_TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(ApiEndpoints.WRITE_TIMEOUT, TimeUnit.SECONDS)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Error setting up SSL for dev cert", e);
        }
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

    /**
     * Clear authentication token
     */
    public void clearAuthToken() {
        SharedPreferences prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        prefs.edit().remove("auth_token").apply();
    }

    /**
     * Get current authentication token
     */
    public String getAuthToken() {
        SharedPreferences prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        return prefs.getString("auth_token", null);
    }
}
