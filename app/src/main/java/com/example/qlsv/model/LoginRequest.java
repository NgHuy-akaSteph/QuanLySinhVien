package com.example.qlsv.model;

import com.google.gson.annotations.SerializedName;

/**
 * Request body cho POST /api/TaiKhoan/login.
 * Swagger: { userName, passWord }
 */
public class LoginRequest {

    @SerializedName("userName")
    private String userName;

    @SerializedName("passWord")
    private String passWord;

    public LoginRequest() {}

    public LoginRequest(String userName, String passWord) {
        this.userName = userName;
        this.passWord = passWord;
    }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getPassWord() { return passWord; }
    public void setPassWord(String passWord) { this.passWord = passWord; }
}
