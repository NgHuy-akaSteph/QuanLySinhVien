package com.example.qlsv.model;

import com.google.gson.annotations.SerializedName;

/**
 * Request body cho POST /api/TaiKhoan/cap-tai-khoan.
 * Swagger: { maGV, userName, passWord, vaiTro }
 */
public class TaiKhoanCreateRequest {

    @SerializedName("maGV")
    private String maGV;

    @SerializedName("userName")
    private String userName;

    @SerializedName("passWord")
    private String passWord;

    @SerializedName("vaiTro")
    private String vaiTro;

    public TaiKhoanCreateRequest() {}

    public TaiKhoanCreateRequest(String maGV, String userName, String passWord, String vaiTro) {
        this.maGV = maGV;
        this.userName = userName;
        this.passWord = passWord;
        this.vaiTro = vaiTro;
    }

    public String getMaGV() { return maGV; }
    public void setMaGV(String maGV) { this.maGV = maGV; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getPassWord() { return passWord; }
    public void setPassWord(String passWord) { this.passWord = passWord; }

    public String getVaiTro() { return vaiTro; }
    public void setVaiTro(String vaiTro) { this.vaiTro = vaiTro; }
}
