package com.example.qlsv.model;

import com.google.gson.annotations.SerializedName;

/**
 * SinhVien - đồng bộ với SinhVienDto trong Swagger API mới.
 * - namSinh (int) -> ngaySinh (ISO datetime, String)
 * - thêm dienThoai, email
 * - giữ maSV, hoTen, lop, anhURI
 * Không còn field diaChi.
 */
public class SinhVien {

    @SerializedName("maSV")
    private String maSV;

    @SerializedName("hoTen")
    private String hoTen;

    @SerializedName("ngaySinh")
    private String ngaySinh; // ISO 8601 datetime, vd "2000-05-15T00:00:00"

    @SerializedName("lop")
    private String lop;

    @SerializedName("dienThoai")
    private String dienThoai;

    @SerializedName("email")
    private String email;

    @SerializedName("anhURI")
    private String anhURI;

    public SinhVien() {}

    public SinhVien(String maSV, String hoTen, String ngaySinh, String lop,
                    String dienThoai, String email, String anhURI) {
        this.maSV = maSV;
        this.hoTen = hoTen;
        this.ngaySinh = ngaySinh;
        this.lop = lop;
        this.dienThoai = dienThoai;
        this.email = email;
        this.anhURI = anhURI;
    }

    public String getMaSV() { return maSV; }
    public void setMaSV(String maSV) { this.maSV = maSV; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(String ngaySinh) { this.ngaySinh = ngaySinh; }

    public String getLop() { return lop; }
    public void setLop(String lop) { this.lop = lop; }

    public String getDienThoai() { return dienThoai; }
    public void setDienThoai(String dienThoai) { this.dienThoai = dienThoai; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAnhURI() { return anhURI; }
    public void setAnhURI(String anhURI) { this.anhURI = anhURI; }
}
