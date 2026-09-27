package com.example.qlsv.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.qlsv.model.GiaoVien;

import java.util.List;

@Dao
public interface GiaoVienDao {

    @Query("SELECT * FROM tb_giaovien WHERE UPPER(maGV) = UPPER(:maGV) AND matKhau = :matKhau LIMIT 1")
    GiaoVien login(String maGV, String matKhau);

    @Query("SELECT * FROM tb_giaovien WHERE UPPER(maGV) = UPPER(:maGV) LIMIT 1")
    GiaoVien getGiaoVienByMaGV(String maGV);

    @Query("SELECT * FROM tb_giaovien")
    List<GiaoVien> getAllGiaoVien();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertGiaoVien(GiaoVien gv);
}
