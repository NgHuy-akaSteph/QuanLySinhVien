package com.example.qlsv.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.qlsv.dao.GiaoVienDao;
import com.example.qlsv.dao.SinhVienDao;
import com.example.qlsv.model.GiaoVien;
import com.example.qlsv.model.SinhVien;

import java.util.concurrent.Executors;

@Database(entities = {GiaoVien.class, SinhVien.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DB_NAME = "QuanLySinhVien_Room.db";
    private static volatile AppDatabase INSTANCE;

    public abstract GiaoVienDao giaoVienDao();
    public abstract SinhVienDao sinhVienDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DB_NAME
                    )
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .addCallback(new Callback() {
                        @Override
                        public void onCreate(@NonNull SupportSQLiteDatabase db) {
                            super.onCreate(db);
                            Executors.newSingleThreadExecutor().execute(() -> populateInitialData(getInstance(context)));
                        }
                    })
                    .build();
                }
            }
        }
        return INSTANCE;
    }

    private static void populateInitialData(AppDatabase db) {
        GiaoVienDao gvDao = db.giaoVienDao();
        SinhVienDao svDao = db.sinhVienDao();

        // Thêm giảng viên mẫu
        gvDao.insertGiaoVien(new GiaoVien("GV001", "123456", "Thầy Nguyễn Văn A", "CNTT1"));
        gvDao.insertGiaoVien(new GiaoVien("GV002", "123456", "Cô Trần Thị B", "DPT1"));

        // Thêm sinh viên mẫu (Tài khoản = Mật khẩu = maSV)
        svDao.insertSinhVien(new SinhVien("SV001", "Nguyễn Văn An", 2002, "Hà Nội", "CNTT1", "", "SV001"));
        svDao.insertSinhVien(new SinhVien("SV002", "Trần Thị Bình", 2003, "Hải Phòng", "CNTT1", "", "SV002"));
        svDao.insertSinhVien(new SinhVien("SV003", "Lê Hoàng Cường", 2002, "Đà Nẵng", "DPT1", "", "SV003"));
    }
}
