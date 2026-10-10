package com.example.qlsv.model;

import com.google.gson.annotations.SerializedName;

/**
 * Một tài khoản đăng nhập - response từ GET /api/TaiKhoan/danh-sach.
 * Cấu trúc này do backend quyết định; giữ các trường phổ biến.
 */
public class TaiKhoan {

    @SerializedName("userName")
    private String userName;

    @SerializedName("maGV")
    private String maGV;

    @SerializedName("hoTen")
    private String hoTen;

    @SerializedName("vaiTro")
    private String vaiTro;

    @SerializedName("ngayTao")
    private String ngayTao;

    public TaiKhoan() {}

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getMaGV() { return maGV; }
    public void setMaGV(String maGV) { this.maGV = maGV; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getVaiTro() { return vaiTro; }
    public void setVaiTro(String vaiTro) { this.vaiTro = vaiTro; }

    public String getNgayTao() { return ngayTao; }
    public void setNgayTao(String ngayTao) { this.ngayTao = ngayTao; }
}
