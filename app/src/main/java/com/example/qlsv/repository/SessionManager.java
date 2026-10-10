package com.example.qlsv.repository;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Quản lý phiên đăng nhập: token, userName, vaiTro, maLienQuan, hoTen, lop.
 * Lưu trong SharedPreferences.
 */
public class SessionManager {

    private static final String PREF_NAME = "session_prefs";
    private static final String KEY_TOKEN = "key_token";
    private static final String KEY_USER_NAME = "key_user_name";
    private static final String KEY_VAI_TRO = "key_vai_tro";
    private static final String KEY_MA_LIEN_QUAN = "key_ma_lien_quan";
    private static final String KEY_HO_TEN = "key_ho_ten";
    private static final String KEY_LOP = "key_lop";

    public static final String ROLE_STUDENT = "STUDENT";
    public static final String ROLE_TEACHER = "TEACHER";
    public static final String ROLE_ADMIN = "ADMIN";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        this.prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(String token, String userName, String vaiTro,
                            String maLienQuan, String hoTen, String lop) {
        prefs.edit()
                .putString(KEY_TOKEN, token == null ? "" : token)
                .putString(KEY_USER_NAME, userName == null ? "" : userName)
                .putString(KEY_VAI_TRO, vaiTro == null ? "" : vaiTro)
                .putString(KEY_MA_LIEN_QUAN, maLienQuan == null ? "" : maLienQuan)
                .putString(KEY_HO_TEN, hoTen == null ? "" : hoTen)
                .putString(KEY_LOP, lop == null ? "" : lop)
                .apply();
    }

    public void clear() {
        prefs.edit().clear().apply();
    }

    public String getToken() { return prefs.getString(KEY_TOKEN, ""); }
    public String getUserName() { return prefs.getString(KEY_USER_NAME, ""); }
    public String getVaiTro() { return prefs.getString(KEY_VAI_TRO, ""); }
    public String getMaLienQuan() { return prefs.getString(KEY_MA_LIEN_QUAN, ""); }
    public String getHoTen() { return prefs.getString(KEY_HO_TEN, ""); }
    public String getLop() { return prefs.getString(KEY_LOP, ""); }

    public boolean isLoggedIn() {
        String token = getToken();
        String vaiTro = getVaiTro();
        return token != null && !token.isEmpty()
                && vaiTro != null && !vaiTro.isEmpty();
    }

    public boolean isAdmin() { return ROLE_ADMIN.equalsIgnoreCase(getVaiTro()); }
    public boolean isTeacher() { return ROLE_TEACHER.equalsIgnoreCase(getVaiTro()); }
    public boolean isStudent() { return ROLE_STUDENT.equalsIgnoreCase(getVaiTro()); }
}
