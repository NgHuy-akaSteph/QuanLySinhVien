package com.example.qlsv.api;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String BASE_URL = "https://upbound-antonetta-undefrauded.ngrok-free.dev/";
    private static ApiService apiService;

    public static synchronized ApiService getApiService() {
        if (apiService == null) {
            OkHttpClient okHttpClient = buildOkHttpClient();
            Retrofit retrofit = buildRetrofit(okHttpClient);
            apiService = retrofit.create(ApiService.class);
        }
        return apiService;
    }

    private static OkHttpClient buildOkHttpClient() {
        return new OkHttpClient.Builder()
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
}
