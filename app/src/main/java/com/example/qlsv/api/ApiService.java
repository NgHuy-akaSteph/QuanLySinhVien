package com.example.qlsv.api;

import com.example.qlsv.model.SinhVien;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {

    @GET("api/SinhVien")
    Call<List<SinhVien>> getAllSinhVien();

    @GET("api/SinhVien/{id}")
    Call<SinhVien> getSinhVienById(@Path("id") String id);

    @POST("api/SinhVien")
    Call<ResponseBody> createSinhVien(@Body SinhVien sv);

    @PUT("api/SinhVien/{id}")
    Call<ResponseBody> updateSinhVien(@Path("id") String id, @Body SinhVien sv);

    @DELETE("api/SinhVien/{id}")
    Call<ResponseBody> deleteSinhVien(@Path("id") String id);
}
