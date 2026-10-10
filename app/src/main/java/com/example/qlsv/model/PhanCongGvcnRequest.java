package com.example.qlsv.model;

import com.google.gson.annotations.SerializedName;

/**
 * Request body cho PUT /api/Lop/{lop}/phan-cong-gvcn.
 * Swagger: { maGV }
 */
public class PhanCongGvcnRequest {

    @SerializedName("maGV")
    private String maGV;

    public PhanCongGvcnRequest() {}

    public PhanCongGvcnRequest(String maGV) {
        this.maGV = maGV;
    }

    public String getMaGV() { return maGV; }
    public void setMaGV(String maGV) { this.maGV = maGV; }
}
