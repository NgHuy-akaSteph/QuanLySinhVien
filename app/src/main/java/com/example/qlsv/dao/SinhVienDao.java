package com.example.qlsv.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.qlsv.model.SinhVien;

import java.util.List;

@Dao
public interface SinhVienDao {

    @Query("SELECT * FROM tb_sinhvien WHERE UPPER(maSV) = UPPER(:maSV) AND matKhau = :matKhau LIMIT 1")
    SinhVien loginStudent(String maSV, String matKhau);

    @Query("SELECT * FROM tb_sinhvien WHERE UPPER(maSV) = UPPER(:maSV) LIMIT 1")
    SinhVien getSinhVienByMaSV(String maSV);

    @Query("SELECT * FROM tb_sinhvien ORDER BY id DESC")
    List<SinhVien> getAllSinhVien();

    @Query("SELECT * FROM tb_sinhvien WHERE lop = :lop ORDER BY id DESC")
    List<SinhVien> getSinhVienTheoLop(String lop);

    @Query("SELECT * FROM tb_sinhvien WHERE lop LIKE '%' || :tuKhoa || '%' ORDER BY id DESC")
    List<SinhVien> timTheoLop(String tuKhoa);

    @Query("SELECT * FROM tb_sinhvien WHERE lop = :lopPhuTrach AND lop LIKE '%' || :tuKhoa || '%' ORDER BY id DESC")
    List<SinhVien> timTheoLopCuaGV(String lopPhuTrach, String tuKhoa);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertSinhVien(SinhVien sv);

    @Query("UPDATE tb_sinhvien SET hoTen = :hoTen, namSinh = :namSinh, diaChi = :diaChi, lop = :lop, anhURI = :anhURI WHERE maSV = :maSV")
    int updateSinhVienByMaSV(String maSV, String hoTen, int namSinh, String diaChi, String lop, String anhURI);

    @Query("DELETE FROM tb_sinhvien WHERE maSV = :maSV")
    int deleteSinhVien(String maSV);
}
