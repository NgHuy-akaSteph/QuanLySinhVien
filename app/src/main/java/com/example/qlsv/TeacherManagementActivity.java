package com.example.qlsv;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.qlsv.adapter.GiaoVienAdapter;
import com.example.qlsv.api.ApiClient;
import com.example.qlsv.model.GiaoVien;
import com.example.qlsv.util.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherManagementActivity extends AppCompatActivity {

    private ListView lvGiaoVien;
    private Button btnThemGv;
    private GiaoVienAdapter adapter;
    private final List<GiaoVien> dsGiaoVien = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_management);
        getSupportActionBar().setTitle("Quản lý Giáo viên");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        lvGiaoVien = findViewById(R.id.lvGiaoVien);
        btnThemGv = findViewById(R.id.btnThemGv);

        adapter = new GiaoVienAdapter(this, dsGiaoVien);
        lvGiaoVien.setAdapter(adapter);

        btnThemGv.setOnClickListener(v -> moDialogForm(null));

        lvGiaoVien.setOnItemClickListener((p, v, pos, id) -> moDialogForm(dsGiaoVien.get(pos)));
        lvGiaoVien.setOnItemLongClickListener((p, v, pos, id) -> {
            confirmXoa(dsGiaoVien.get(pos));
            return true;
        });

        napDanhSach();
    }

    private void napDanhSach() {
        ApiClient.getApiService().getAllGiaoVien().enqueue(new Callback<List<GiaoVien>>() {
            @Override
            public void onResponse(Call<List<GiaoVien>> call, Response<List<GiaoVien>> response) {
                if (response.code() == 401) {
                    Toast.makeText(TeacherManagementActivity.this,
                            "Phiên hết hạn", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                if (response.isSuccessful() && response.body() != null) {
                    dsGiaoVien.clear();
                    dsGiaoVien.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(TeacherManagementActivity.this,
                            "Tải danh sách GV thất bại", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<List<GiaoVien>> call, Throwable t) {
                Toast.makeText(TeacherManagementActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void moDialogForm(GiaoVien gv) {
        View view = getLayoutInflater().inflate(R.layout.dialog_teacher_form, null);
        EditText edtMaGV = view.findViewById(R.id.edtMaGVForm);
        EditText edtHoTen = view.findViewById(R.id.edtHoTenForm);
        EditText edtHocHam = view.findViewById(R.id.edtHocHamForm);
        EditText edtHocVi = view.findViewById(R.id.edtHocViForm);
        EditText edtEmail = view.findViewById(R.id.edtEmailForm);
        EditText edtDienThoai = view.findViewById(R.id.edtDienThoaiForm);

        boolean isEdit = gv != null;
        if (isEdit) {
            edtMaGV.setText(gv.getMaGV());
            edtMaGV.setEnabled(false);
            edtHoTen.setText(gv.getHoTen());
            edtHocHam.setText(gv.getHocHam());
            edtHocVi.setText(gv.getHocVi());
            edtEmail.setText(gv.getEmail());
            edtDienThoai.setText(gv.getDienThoai());
        }

        new AlertDialog.Builder(this)
                .setTitle(isEdit ? "Sửa giáo viên" : "Thêm giáo viên")
                .setView(view)
                .setPositiveButton("Lưu", (d, w) -> {
                    String ma = edtMaGV.getText().toString().trim();
                    String ten = edtHoTen.getText().toString().trim();
                    if (ma.isEmpty() || ten.isEmpty()) {
                        Toast.makeText(this, "Mã GV và họ tên bắt buộc", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    GiaoVien gvMoi = new GiaoVien(
                            ma, ten,
                            edtHocHam.getText().toString().trim(),
                            edtHocVi.getText().toString().trim(),
                            edtEmail.getText().toString().trim(),
                            edtDienThoai.getText().toString().trim());
                    if (isEdit) {
                        goiApiUpdate(ma, gvMoi);
                    } else {
                        goiApiCreate(gvMoi);
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void goiApiCreate(GiaoVien gv) {
        ApiClient.getApiService().createGiaoVien(gv).enqueue(new Callback<GiaoVien>() {
            @Override
            public void onResponse(Call<GiaoVien> call, Response<GiaoVien> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(TeacherManagementActivity.this, "Thêm thành công",
                            Toast.LENGTH_SHORT).show();
                    napDanhSach();
                } else {
                    Toast.makeText(TeacherManagementActivity.this,
                            "Thêm thất bại (mã " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<GiaoVien> call, Throwable t) {
                Toast.makeText(TeacherManagementActivity.this,
                        "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goiApiUpdate(String maGV, GiaoVien gv) {
        ApiClient.getApiService().updateGiaoVien(maGV, gv).enqueue(new Callback<GiaoVien>() {
            @Override
            public void onResponse(Call<GiaoVien> call, Response<GiaoVien> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(TeacherManagementActivity.this, "Cập nhật thành công",
                            Toast.LENGTH_SHORT).show();
                    napDanhSach();
                } else {
                    Toast.makeText(TeacherManagementActivity.this, "Cập nhật thất bại",
                            Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<GiaoVien> call, Throwable t) {
                Toast.makeText(TeacherManagementActivity.this,
                        "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmXoa(GiaoVien gv) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa giáo viên?")
                .setMessage("Xóa " + gv.getMaGV() + " - " + gv.getHoTen() + "?")
                .setPositiveButton("Xóa", (d, w) -> {
                    ApiClient.getApiService().deleteGiaoVien(gv.getMaGV())
                            .enqueue(new Callback<okhttp3.ResponseBody>() {
                                @Override
                                public void onResponse(Call<okhttp3.ResponseBody> call,
                                                       Response<okhttp3.ResponseBody> response) {
                                    if (response.isSuccessful()) {
                                        Toast.makeText(TeacherManagementActivity.this, "Đã xóa",
                                                Toast.LENGTH_SHORT).show();
                                        napDanhSach();
                                    } else {
                                        Toast.makeText(TeacherManagementActivity.this,
                                                "Xóa thất bại", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                @Override
                                public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {
                                    Toast.makeText(TeacherManagementActivity.this,
                                            "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
