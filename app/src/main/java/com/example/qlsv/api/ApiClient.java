package com.example.qlsv.api;

import android.content.Context;

import com.example.qlsv.repository.SessionManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String BASE_URL = "https://upbound-antonetta-undefrauded.ngrok-free.dev/";
    private static ApiService apiService;
    private static SessionManager sessionManagerInstance;

    public static void init(Context appContext) {
        sessionManagerInstance = new SessionManager(appContext);
        // Reset client để lần sau getApiService() dùng session mới nhất
        apiService = null;
    }

    public static synchronized ApiService getApiService() {
        if (apiService == null) {
            OkHttpClient okHttpClient = buildOkHttpClient();
            Retrofit retrofit = buildRetrofit(okHttpClient);
            apiService = retrofit.create(ApiService.class);
        }
        return apiService;
    }

    public static SessionManager getSessionManager() {
        return sessionManagerInstance;
    }

    private static OkHttpClient buildOkHttpClient() {
        return new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor())
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request request = original.newBuilder()
                            .header("ngrok-skip-browser-warning", "true")
                            .build();
                    return chain.proceed(request);
                })
                .build();
    }

    private static Retrofit buildRetrofit(OkHttpClient client) {
        return new Retrofit.Builder()
                .baseUrl(ApiClient.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    /**
     * Gắn header Authorization: Bearer <token> nếu có token trong SessionManager.
     * Bỏ qua cho endpoint /api/TaiKhoan/login (chưa có token khi gọi).
     */
    private static class AuthInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request original = chain.request();
            String path = original.url().encodedPath();
            // Không gắn token cho login
            if (path != null && path.endsWith("/api/TaiKhoan/login")) {
                return chain.proceed(original);
            }
            if (sessionManagerInstance == null) {
                return chain.proceed(original);
            }
            String token = sessionManagerInstance.getToken();
            if (token == null || token.isEmpty()) {
                return chain.proceed(original);
            }
            Request request = original.newBuilder()
                    .header("Authorization", "Bearer " + token)
                    .build();
            return chain.proceed(request);
        }
    }
}
