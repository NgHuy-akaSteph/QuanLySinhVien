package com.example.qlsv.model;

import com.google.gson.annotations.SerializedName;

/**
 * Request body cho POST /api/TaiKhoan/doi-mat-khau.
 * Swagger: { oldPassword, newPassword, confirmPassword }
 */
public class ChangePasswordRequest {

    @SerializedName("oldPassword")
    private String oldPassword;

    @SerializedName("newPassword")
    private String newPassword;

    @SerializedName("confirmPassword")
    private String confirmPassword;

    public ChangePasswordRequest() {}

    public ChangePasswordRequest(String oldPassword, String newPassword, String confirmPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
    }

    public String getOldPassword() { return oldPassword; }
    public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
