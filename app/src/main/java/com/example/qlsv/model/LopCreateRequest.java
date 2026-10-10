package com.example.qlsv.model;

import com.google.gson.annotations.SerializedName;

/**
 * Request body cho POST /api/Lop.
 * Swagger: { lop, namNhapHoc }
 */
public class LopCreateRequest {

    @SerializedName("lop")
    private String lop;

    @SerializedName("namNhapHoc")
    private int namNhapHoc;

    public LopCreateRequest() {}

    public LopCreateRequest(String lop, int namNhapHoc) {
        this.lop = lop;
        this.namNhapHoc = namNhapHoc;
    }

    public String getLop() { return lop; }
    public void setLop(String lop) { this.lop = lop; }

    public int getNamNhapHoc() { return namNhapHoc; }
    public void setNamNhapHoc(int namNhapHoc) { this.namNhapHoc = namNhapHoc; }
}
