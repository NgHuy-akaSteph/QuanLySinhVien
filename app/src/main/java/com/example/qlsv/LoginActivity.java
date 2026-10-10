package com.example.qlsv;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.qlsv.api.ApiClient;
import com.example.qlsv.model.GiaoVien;
import com.example.qlsv.model.SinhVien;
import com.example.qlsv.repository.TeacherManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    public static final String EXTRA_ROLE = "EXTRA_ROLE";
    public static final String EXTRA_USER_ID = "EXTRA_USER_ID";
    public static final String EXTRA_HO_TEN = "EXTRA_HO_TEN";
    public static final String EXTRA_LOP = "EXTRA_LOP";

    public static final String ROLE_STUDENT = "STUDENT";
    public static final String ROLE_TEACHER = "TEACHER";

    private LinearLayout panelLogin, panelRegister;
    private EditText edtMaGV, edtMatKhau;
    private EditText edtRegMaGV, edtRegMatKhau, edtRegHoTen, edtRegLopPhuTrach;

    private TeacherManager teacherManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        teacherManager = TeacherManager.getInstance(this);

        anhXaView();
        ganSuKien();
    }

    private void anhXaView() {
        panelLogin = findViewById(R.id.panelLogin);
        panelRegister = findViewById(R.id.panelRegister);

        edtMaGV = findViewById(R.id.edtMaGV);
        edtMatKhau = findViewById(R.id.edtMatKhau);

        edtRegMaGV = findViewById(R.id.edtRegMaGV);
        edtRegMatKhau = findViewById(R.id.edtRegMatKhau);
        edtRegHoTen = findViewById(R.id.edtRegHoTen);
        edtRegLopPhuTrach = findViewById(R.id.edtRegLopPhuTrach);
    }

    private void ganSuKien() {
        Button btnDangNhap = findViewById(R.id.btnDangNhap);
        Button btnChuyenDangKy = findViewById(R.id.btnChuyenDangKy);
        Button btnThucHienDangKy = findViewById(R.id.btnThucHienDangKy);
        Button btnChuyenDangNhap = findViewById(R.id.btnChuyenDangNhap);

        btnDangNhap.setOnClickListener(v -> xuLyDangNhap());

        btnChuyenDangKy.setOnClickListener(v -> {
            panelLogin.setVisibility(View.GONE);
            panelRegister.setVisibility(View.VISIBLE);
        });

        btnChuyenDangNhap.setOnClickListener(v -> {
            panelRegister.setVisibility(View.GONE);
            panelLogin.setVisibility(View.VISIBLE);
        });

        btnThucHienDangKy.setOnClickListener(v -> xuLyDangKyGiangVien());
    }

    private void xuLyDangKyGiangVien() {
        String maGV = edtRegMaGV.getText().toString().trim().toUpperCase();
        String matKhau = edtRegMatKhau.getText().toString().trim();
        String hoTen = edtRegHoTen.getText().toString().trim();
        String lopPhuTrach = edtRegLopPhuTrach.getText().toString().trim();

        if (TextUtils.isEmpty(maGV) || TextUtils.isEmpty(matKhau)
                || TextUtils.isEmpty(hoTen) || TextUtils.isEmpty(lopPhuTrach)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin đăng ký!", Toast.LENGTH_SHORT).show();
            return;
        }

        GiaoVien gvMoi = new GiaoVien(maGV, matKhau, hoTen, lopPhuTrach);
        boolean success = teacherManager.registerTeacher(gvMoi);

        if (success) {
            Toast.makeText(this, "Đăng ký Giảng viên thành công! Vui lòng đăng nhập.", Toast.LENGTH_SHORT).show();
            edtRegMaGV.setText("");
            edtRegMatKhau.setText("");
            edtRegHoTen.setText("");
            edtRegLopPhuTrach.setText("");

            panelRegister.setVisibility(View.GONE);
            panelLogin.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, "Mã giảng viên \"" + maGV + "\" đã tồn tại!", Toast.LENGTH_SHORT).show();
        }
    }

    private void xuLyDangNhap() {
        String username = edtMaGV.getText().toString().trim();
        String matKhau = edtMatKhau.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(matKhau)) {
            Toast.makeText(this, getString(R.string.nhap_day_du), Toast.LENGTH_SHORT).show();
            return;
        }

        String usernameUpper = username.toUpperCase();

        if (usernameUpper.startsWith("SV")) {
            ApiClient.getApiService().getSinhVienById(usernameUpper).enqueue(new Callback<SinhVien>() {
                @Override
                public void onResponse(Call<SinhVien> call, Response<SinhVien> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        SinhVien sv = response.body();
                        if (matKhau.equalsIgnoreCase(sv.getMaSV())) {
                            Toast.makeText(LoginActivity.this, "Xin chào Sinh viên " + sv.getHoTen(), Toast.LENGTH_SHORT).show();
                            chuyenSangMainActivity(ROLE_STUDENT, sv.getMaSV(), sv.getHoTen(), sv.getLop());
                        } else {
                            Toast.makeText(LoginActivity.this, "Mật khẩu không đúng!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Không tìm thấy mã sinh viên " + usernameUpper, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<SinhVien> call, Throwable t) {
                    Toast.makeText(LoginActivity.this, "Lỗi kết nối API: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            GiaoVien gv = teacherManager.login(usernameUpper, matKhau);
            if (gv != null) {
                Toast.makeText(this, "Xin chào " + gv.getHoTen(), Toast.LENGTH_SHORT).show();
                chuyenSangMainActivity(ROLE_TEACHER, gv.getMaGV(), gv.getHoTen(), gv.getLopPhuTrach());
            } else {
                Toast.makeText(this, getString(R.string.loi_dang_nhap), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void chuyenSangMainActivity(String role, String userId, String hoTen, String lop) {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra(EXTRA_ROLE, role);
        intent.putExtra(EXTRA_USER_ID, userId);
        intent.putExtra(EXTRA_HO_TEN, hoTen);
        intent.putExtra(EXTRA_LOP, lop);
        startActivity(intent);
        finish();
    }
}
