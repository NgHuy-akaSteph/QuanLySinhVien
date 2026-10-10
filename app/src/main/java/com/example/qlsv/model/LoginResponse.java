package com.example.qlsv.model;

import com.google.gson.annotations.SerializedName;

/**
 * Response cho POST /api/TaiKhoan/login.
 * Backend có thể trả về nhiều trường - giữ các trường phổ biến.
 * Trường nào server không trả sẽ là null; code sẽ check null khi dùng.
 */
public class LoginResponse {

    @SerializedName("token")
    private String token;

    @SerializedName("vaiTro")
    private String vaiTro; // STUDENT / TEACHER / ADMIN

    @SerializedName("maLienQuan")
    private String maLienQuan; // maSV hoặc maGV

    @SerializedName("hoTen")
    private String hoTen;

    @SerializedName("userName")
    private String userName;

    @SerializedName("lop")
    private String lop; // chỉ áp dụng cho sinh viên / giáo viên phụ trách

    public LoginResponse() {}

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getVaiTro() { return vaiTro; }
    public void setVaiTro(String vaiTro) { this.vaiTro = vaiTro; }

    public String getMaLienQuan() { return maLienQuan; }
    public void setMaLienQuan(String maLienQuan) { this.maLienQuan = maLienQuan; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getLop() { return lop; }
    public void setLop(String lop) { this.lop = lop; }
}
