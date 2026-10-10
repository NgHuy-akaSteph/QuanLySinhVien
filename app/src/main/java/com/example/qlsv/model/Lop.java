package com.example.qlsv.model;

import com.google.gson.annotations.SerializedName;

/**
 * Lớp học - response từ GET /api/Lop.
 * Schema có thể gồm lop, namNhapHoc, maGVCN (nếu đã phân công).
 */
public class Lop {

    @SerializedName("lop")
    private String lop;

    @SerializedName("namNhapHoc")
    private Integer namNhapHoc;

    @SerializedName("maGVCN")
    private String maGVCN;

    @SerializedName("siSo")
    private Integer siSo;

    public Lop() {}

    public Lop(String lop, Integer namNhapHoc) {
        this.lop = lop;
        this.namNhapHoc = namNhapHoc;
    }

    public String getLop() { return lop; }
    public void setLop(String lop) { this.lop = lop; }

    public Integer getNamNhapHoc() { return namNhapHoc; }
    public void setNamNhapHoc(Integer namNhapHoc) { this.namNhapHoc = namNhapHoc; }

    public String getMaGVCN() { return maGVCN; }
    public void setMaGVCN(String maGVCN) { this.maGVCN = maGVCN; }

    public Integer getSiSo() { return siSo; }
    public void setSiSo(Integer siSo) { this.siSo = siSo; }
}
