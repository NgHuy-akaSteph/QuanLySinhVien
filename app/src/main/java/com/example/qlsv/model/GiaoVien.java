package com.example.qlsv.model;

import com.google.gson.annotations.SerializedName;

/**
 * GiaoVien - đồng bộ với GiaoVienDto trong Swagger API mới.
 * - bỏ matKhau, lopPhuTrach
 * - thêm hocHam, hocVi, email, dienThoai
 */
public class GiaoVien {

    @SerializedName("maGV")
    private String maGV;

    @SerializedName("hoTen")
    private String hoTen;

    @SerializedName("hocHam")
    private String hocHam;

    @SerializedName("hocVi")
    private String hocVi;

    @SerializedName("email")
    private String email;

    @SerializedName("dienThoai")
    private String dienThoai;

    public GiaoVien() {}

    public GiaoVien(String maGV, String hoTen, String hocHam, String hocVi,
                    String email, String dienThoai) {
        this.maGV = maGV;
        this.hoTen = hoTen;
        this.hocHam = hocHam;
        this.hocVi = hocVi;
        this.email = email;
        this.dienThoai = dienThoai;
    }

    public String getMaGV() { return maGV; }
    public void setMaGV(String maGV) { this.maGV = maGV; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getHocHam() { return hocHam; }
    public void setHocHam(String hocHam) { this.hocHam = hocHam; }

    public String getHocVi() { return hocVi; }
    public void setHocVi(String hocVi) { this.hocVi = hocVi; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDienThoai() { return dienThoai; }
    public void setDienThoai(String dienThoai) { this.dienThoai = dienThoai; }
}
