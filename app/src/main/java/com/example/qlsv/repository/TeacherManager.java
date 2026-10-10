package com.example.qlsv.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.qlsv.model.GiaoVien;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Quản lý tài khoản GV cục bộ (SharedPreferences) - dùng làm fallback
 * khi admin chưa cấp tài khoản API. Lưu thêm matKhau và lopPhuTrach
 * vì GiaoVien model mới đã bỏ 2 trường này.
 */
public class TeacherManager {

    private static final String PREF_NAME = "teacher_prefs";
    private static final String KEY_TEACHERS = "key_teachers";

    private static TeacherManager instance;
    private final SharedPreferences prefs;
    private final Gson gson;

    /** DTO nội bộ để lưu thêm matKhau, lopPhuTrach (không có trong API GiaoVienDto). */
    public static class GiaoVienLocal {
        public String maGV;
        public String matKhau;
        public String hoTen;
        public String lopPhuTrach;
        public String hocHam;
        public String hocVi;
        public String email;
        public String dienThoai;

        public GiaoVienLocal() {}

        public GiaoVienLocal(String maGV, String matKhau, String hoTen, String lopPhuTrach) {
            this.maGV = maGV;
            this.matKhau = matKhau;
            this.hoTen = hoTen;
            this.lopPhuTrach = lopPhuTrach;
        }
    }

    private TeacherManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        initDefaultTeachers();
    }

    public static synchronized TeacherManager getInstance(Context context) {
        if (instance == null) {
            instance = new TeacherManager(context);
        }
        return instance;
    }

    private void initDefaultTeachers() {
        List<GiaoVienLocal> teachers = getAllLocal();
        boolean hasDefault = false;
        for (GiaoVienLocal gv : teachers) {
            if ("GV01".equalsIgnoreCase(gv.maGV)) {
                hasDefault = true;
                break;
            }
        }
        if (!hasDefault) {
            teachers.add(new GiaoVienLocal("GV01", "GV01", "Giáo viên mặc định", ""));
            saveAllLocal(teachers);
        }
    }

    private List<GiaoVienLocal> getAllLocal() {
        String json = prefs.getString(KEY_TEACHERS, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<GiaoVienLocal>>() {}.getType();
        List<GiaoVienLocal> list = gson.fromJson(json, type);
        return list != null ? list : new ArrayList<>();
    }

    private void saveAllLocal(List<GiaoVienLocal> list) {
        String json = gson.toJson(list);
        prefs.edit().putString(KEY_TEACHERS, json).apply();
    }

    /** Login với tài khoản local. */
    public GiaoVienLocal login(String maGV, String matKhau) {
        if (maGV == null || matKhau == null) return null;
        for (GiaoVienLocal gv : getAllLocal()) {
            if (maGV.equalsIgnoreCase(gv.maGV) && matKhau.equalsIgnoreCase(gv.matKhau)) {
                return gv;
            }
        }
        return null;
    }

    /** Đăng ký GV từ form - wrapper tương thích. */
    public boolean registerTeacherCompat(GiaoVien gv, String matKhau, String lopPhuTrach) {
        if (gv == null || gv.getMaGV() == null) return false;
        List<GiaoVienLocal> list = getAllLocal();
        for (GiaoVienLocal existing : list) {
            if (existing.maGV.equalsIgnoreCase(gv.getMaGV())) return false;
        }
        list.add(new GiaoVienLocal(
                gv.getMaGV(), matKhau, gv.getHoTen(), lopPhuTrach));
        saveAllLocal(list);
        return true;
    }
}
