package com.example.qlsv.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.qlsv.model.GiaoVien;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TeacherManager {

    private static final String PREF_NAME = "teacher_prefs";
    private static final String KEY_TEACHERS = "key_teachers";

    private static TeacherManager instance;
    private final SharedPreferences prefs;
    private final Gson gson;

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
        // Đảm bảo luôn có 1 GV mặc định GV01/GV01 cho lần đầu dùng app.
        // Nếu user đã có data cũ (GV001/...) thì vẫn giữ nguyên, chỉ thêm GV01 nếu thiếu.
        List<GiaoVien> teachers = getAllTeachers();
        boolean hasDefault = false;
        for (GiaoVien gv : teachers) {
            if ("GV01".equalsIgnoreCase(gv.getMaGV())) {
                hasDefault = true;
                break;
            }
        }
        if (!hasDefault) {
            teachers.add(new GiaoVien("GV01", "GV01", "Giáo viên mặc định", ""));
            saveAllTeachers(teachers);
        }
    }

    public List<GiaoVien> getAllTeachers() {
        String json = prefs.getString(KEY_TEACHERS, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<GiaoVien>>() {}.getType();
        List<GiaoVien> list = gson.fromJson(json, type);
        return list != null ? list : new ArrayList<>();
    }

    private void saveAllTeachers(List<GiaoVien> teachers) {
        String json = gson.toJson(teachers);
        prefs.edit().putString(KEY_TEACHERS, json).apply();
    }

    public GiaoVien login(String maGV, String matKhau) {
        if (maGV == null || matKhau == null) return null;
        List<GiaoVien> teachers = getAllTeachers();
        for (GiaoVien gv : teachers) {
            if (maGV.equalsIgnoreCase(gv.getMaGV()) && matKhau.equalsIgnoreCase(gv.getMatKhau())) {
                return gv;
            }
        }
        return null;
    }

    public boolean registerTeacher(GiaoVien newGv) {
        if (newGv == null || newGv.getMaGV() == null) return false;
        List<GiaoVien> teachers = getAllTeachers();
        for (GiaoVien gv : teachers) {
            if (newGv.getMaGV().equalsIgnoreCase(gv.getMaGV())) {
                return false; // Already exists
            }
        }
        teachers.add(newGv);
        saveAllTeachers(teachers);
        return true;
    }
}
