package com.example.qlsv.model;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "tb_sinhvien", indices = {@Index(value = {"maSV"}, unique = true)})
public class SinhVien {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String maSV;
    private String hoTen;
    private int namSinh;
    private String diaChi;
    private String lop;
    private String anhURI;
    private String matKhau;

    public SinhVien() {}

    public SinhVien(String maSV, String hoTen, int namSinh, String diaChi, String lop, String anhURI, String matKhau) {
        this.maSV = maSV;
        this.hoTen = hoTen;
        this.namSinh = namSinh;
        this.diaChi = diaChi;
        this.lop = lop;
        this.anhURI = anhURI;
        this.matKhau = matKhau;
    }

    public SinhVien(String maSV, String hoTen, int namSinh, String diaChi, String lop, String anhURI) {
        this(maSV, hoTen, namSinh, diaChi, lop, anhURI, "123456");
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMaSV() {
        return maSV;
    }

    public void setMaSV(String maSV) {
        this.maSV = maSV;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public int getNamSinh() {
        return namSinh;
    }

    public void setNamSinh(int namSinh) {
        this.namSinh = namSinh;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public String getLop() {
        return lop;
    }

    public void setLop(String lop) {
        this.lop = lop;
    }

    public String getAnhURI() {
        return anhURI;
    }

    public void setAnhURI(String anhURI) {
        this.anhURI = anhURI;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }
}
