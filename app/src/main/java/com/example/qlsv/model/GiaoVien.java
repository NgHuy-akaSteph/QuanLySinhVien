package com.example.qlsv.model;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "tb_giaovien", indices = {@Index(value = {"maGV"}, unique = true)})
public class GiaoVien {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String maGV;
    private String matKhau;
    private String hoTen;
    private String lopPhuTrach;

    public GiaoVien() {}

    public GiaoVien(String maGV, String matKhau, String hoTen, String lopPhuTrach) {
        this.maGV = maGV;
        this.matKhau = matKhau;
        this.hoTen = hoTen;
        this.lopPhuTrach = lopPhuTrach;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMaGV() {
        return maGV;
    }

    public void setMaGV(String maGV) {
        this.maGV = maGV;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getLopPhuTrach() {
        return lopPhuTrach;
    }

    public void setLopPhuTrach(String lopPhuTrach) {
        this.lopPhuTrach = lopPhuTrach;
    }
}
