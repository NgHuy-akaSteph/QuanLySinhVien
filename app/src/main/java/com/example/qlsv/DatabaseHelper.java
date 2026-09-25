package com.example.qlsv;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLiteOpenHelper cho CSDL QuanLySinhVien.db.
 * Bảng tb_sinhvien:
 *  id       INTEGER PRIMARY KEY AUTOINCREMENT
 *  maSV     TEXT UNIQUE NOT NULL
 *  hoTen    TEXT
 *  namSinh  INTEGER
 *  diaChi   TEXT
 *  lop      TEXT
 *  anhURI   TEXT
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "QuanLySinhVien.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE_NAME = "tb_sinhvien";
    public static final String COL_ID = "id";
    public static final String COL_MASV = "maSV";
    public static final String COL_HOTEN = "hoTen";
    public static final String COL_NAMSINH = "namSinh";
    public static final String COL_DIACHI = "diaChi";
    public static final String COL_LOP = "lop";
    public static final String COL_ANHURI = "anhURI";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_MASV + " TEXT UNIQUE NOT NULL, " +
                COL_HOTEN + " TEXT, " +
                COL_NAMSINH + " INTEGER, " +
                COL_DIACHI + " TEXT, " +
                COL_LOP + " TEXT, " +
                COL_ANHURI + " TEXT)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    /**
     * Thêm sinh viên mới.
     * SQLiteDatabase#insert tự trả về -1 nếu vi phạm ràng buộc UNIQUE (trùng maSV),
     * thay vì ném exception, nên đây chính là cách bắt lỗi trùng mã theo yêu cầu đề bài.
     *
     * @return id của dòng vừa thêm, hoặc -1 nếu thất bại (trùng maSV).
     */
    public long insertSinhVien(SinhVien sv) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = layValues(sv);
        return db.insert(TABLE_NAME, null, values);
    }

    /**
     * Cập nhật sinh viên, dùng maSV làm khóa tìm kiếm (maSV không được sửa).
     *
     * @return số dòng bị ảnh hưởng.
     */
    public int updateSinhVien(SinhVien sv) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = layValues(sv);
        return db.update(TABLE_NAME, values, COL_MASV + " = ?", new String[]{sv.getMaSV()});
    }

    public int deleteSinhVien(String maSV) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_NAME, COL_MASV + " = ?", new String[]{maSV});
    }

    public List<SinhVien> getAllSinhVien() {
        List<SinhVien> ds = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_NAME, null, null, null, null, null, COL_ID + " DESC");
        while (c.moveToNext()) {
            ds.add(docTuCursor(c));
        }
        c.close();
        return ds;
    }

    /**
     * Tìm kiếm thời gian thực theo cột Lớp (LIKE %tuKhoa%).
     */
    public List<SinhVien> timTheoLop(String tuKhoa) {
        List<SinhVien> ds = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_NAME, null,
                COL_LOP + " LIKE ?", new String[]{"%" + tuKhoa + "%"},
                null, null, COL_ID + " DESC");
        while (c.moveToNext()) {
            ds.add(docTuCursor(c));
        }
        c.close();
        return ds;
    }

    private ContentValues layValues(SinhVien sv) {
        ContentValues values = new ContentValues();
        values.put(COL_MASV, sv.getMaSV());
        values.put(COL_HOTEN, sv.getHoTen());
        values.put(COL_NAMSINH, sv.getNamSinh());
        values.put(COL_DIACHI, sv.getDiaChi());
        values.put(COL_LOP, sv.getLop());
        values.put(COL_ANHURI, sv.getAnhURI());
        return values;
    }

    private SinhVien docTuCursor(Cursor c) {
        SinhVien sv = new SinhVien();
        sv.setId(c.getLong(c.getColumnIndexOrThrow(COL_ID)));
        sv.setMaSV(c.getString(c.getColumnIndexOrThrow(COL_MASV)));
        sv.setHoTen(c.getString(c.getColumnIndexOrThrow(COL_HOTEN)));
        sv.setNamSinh(c.getInt(c.getColumnIndexOrThrow(COL_NAMSINH)));
        sv.setDiaChi(c.getString(c.getColumnIndexOrThrow(COL_DIACHI)));
        sv.setLop(c.getString(c.getColumnIndexOrThrow(COL_LOP)));
        sv.setAnhURI(c.getString(c.getColumnIndexOrThrow(COL_ANHURI)));
        return sv;
    }
}
