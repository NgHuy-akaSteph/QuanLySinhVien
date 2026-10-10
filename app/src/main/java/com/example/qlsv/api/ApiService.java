package com.example.qlsv.api;

import com.example.qlsv.model.ChangePasswordRequest;
import com.example.qlsv.model.GiaoVien;
import com.example.qlsv.model.Lop;
import com.example.qlsv.model.LopCreateRequest;
import com.example.qlsv.model.LoginRequest;
import com.example.qlsv.model.LoginResponse;
import com.example.qlsv.model.PhanCongGvcnRequest;
import com.example.qlsv.model.SinhVien;
import com.example.qlsv.model.TaiKhoan;
import com.example.qlsv.model.TaiKhoanCreateRequest;
import com.example.qlsv.model.UploadResponse;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ApiService {

    // ==================== SinhVien ====================
    @GET("api/SinhVien")
    Call<List<SinhVien>> getAllSinhVien();

    @GET("api/SinhVien/{maSV}")
    Call<SinhVien> getSinhVienById(@Path("maSV") String maSV);

    @POST("api/SinhVien")
    Call<SinhVien> createSinhVien(@Body SinhVien sv);

    @PUT("api/SinhVien/{maSV}")
    Call<SinhVien> updateSinhVien(@Path("maSV") String maSV, @Body SinhVien sv);

    @DELETE("api/SinhVien/{maSV}")
    Call<ResponseBody> deleteSinhVien(@Path("maSV") String maSV);

    @Multipart
    @POST("api/SinhVien/{maSV}/upload-avatar")
    Call<UploadResponse> uploadAvatarSv(
            @Path("maSV") String maSV,
            @Part MultipartBody.Part file);

    // ==================== GiaoVien ====================
    @GET("api/GiaoVien")
    Call<List<GiaoVien>> getAllGiaoVien();

    @GET("api/GiaoVien/{maGV}")
    Call<GiaoVien> getGiaoVienById(@Path("maGV") String maGV);

    @POST("api/GiaoVien")
    Call<GiaoVien> createGiaoVien(@Body GiaoVien gv);

    @PUT("api/GiaoVien/{maGV}")
    Call<GiaoVien> updateGiaoVien(@Path("maGV") String maGV, @Body GiaoVien gv);

    @DELETE("api/GiaoVien/{maGV}")
    Call<ResponseBody> deleteGiaoVien(@Path("maGV") String maGV);

    @Multipart
    @POST("api/GiaoVien/{maGV}/upload-avatar")
    Call<UploadResponse> uploadAvatarGv(
            @Path("maGV") String maGV,
            @Part MultipartBody.Part file);

    // ==================== Lop ====================
    @GET("api/Lop")
    Call<List<Lop>> getAllLop();

    @POST("api/Lop")
    Call<Lop> createLop(@Body LopCreateRequest req);

    @DELETE("api/Lop/{lop}")
    Call<ResponseBody> deleteLop(@Path("lop") String lop);

    @PUT("api/Lop/{lop}/phan-cong-gvcn")
    Call<ResponseBody> phanCongGvcn(@Path("lop") String lop, @Body PhanCongGvcnRequest req);

    // ==================== TaiKhoan ====================
    @POST("api/TaiKhoan/login")
    Call<LoginResponse> login(@Body LoginRequest req);

    @POST("api/TaiKhoan/doi-mat-khau")
    Call<ResponseBody> doiMatKhau(@Body ChangePasswordRequest req);

    @GET("api/TaiKhoan/danh-sach")
    Call<List<TaiKhoan>> getDanhSachTaiKhoan();

    @POST("api/TaiKhoan/cap-tai-khoan")
    Call<TaiKhoan> capTaiKhoan(@Body TaiKhoanCreateRequest req);

    @DELETE("api/TaiKhoan/{maGV}/thu-hoi")
    Call<ResponseBody> thuHoiTaiKhoan(@Path("maGV") String maGV);
}
