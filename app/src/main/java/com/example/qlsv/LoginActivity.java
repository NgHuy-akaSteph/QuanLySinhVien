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
import com.example.qlsv.api.ApiService;
import com.example.qlsv.model.LoginRequest;
import com.example.qlsv.model.LoginResponse;
import com.example.qlsv.repository.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    public static final String EXTRA_ROLE = "EXTRA_ROLE";
    public static final String EXTRA_USER_ID = "EXTRA_USER_ID";
    public static final String EXTRA_HO_TEN = "EXTRA_HO_TEN";
    public static final String EXTRA_LOP = "EXTRA_LOP";

    // Giữ hằng số ROLE_ để tương thích ngược với MainActivity
    public static final String ROLE_STUDENT = "STUDENT";
    public static final String ROLE_TEACHER = "TEACHER";
    public static final String ROLE_ADMIN = "ADMIN";

    private LinearLayout panelLogin;
    private EditText edtMaGV, edtMatKhau;

    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Khởi tạo ApiClient (cần cho AuthInterceptor)
        ApiClient.init(getApplicationContext());
        session = ApiClient.getSessionManager();

        // Nếu đã có session hợp lệ -> vào thẳng MainActivity
        if (session != null && session.isLoggedIn()) {
            chuyenSangMainActivity(
                    session.getVaiTro(),
                    session.getMaLienQuan(),
                    session.getHoTen(),
                    session.getLop()
            );
            return;
        }

        anhXaView();
        ganSuKien();
    }

    private void anhXaView() {
        panelLogin = findViewById(R.id.panelLogin);
        edtMaGV = findViewById(R.id.edtMaGV);
        edtMatKhau = findViewById(R.id.edtMatKhau);
    }

    private void ganSuKien() {
        Button btnDangNhap = findViewById(R.id.btnDangNhap);
        Button btnChuyenDangKy = findViewById(R.id.btnChuyenDangKy);
        Button btnThucHienDangKy = findViewById(R.id.btnThucHienDangKy);
        Button btnChuyenDangNhap = findViewById(R.id.btnChuyenDangNhap);

        btnDangNhap.setOnClickListener(v -> xuLyDangNhap());

        // Panel đăng ký GV vẫn giữ (lưu SharedPreferences) - admin sẽ cấp tài khoản thật qua API
        btnChuyenDangKy.setOnClickListener(v -> {
            panelLogin.setVisibility(View.GONE);
            LinearLayout panelRegister = findViewById(R.id.panelRegister);
            if (panelRegister != null) panelRegister.setVisibility(View.VISIBLE);
        });
        if (btnChuyenDangNhap != null) {
            btnChuyenDangNhap.setOnClickListener(v -> {
                LinearLayout panelRegister = findViewById(R.id.panelRegister);
                if (panelRegister != null) panelRegister.setVisibility(View.GONE);
                panelLogin.setVisibility(View.VISIBLE);
            });
        }
        if (btnThucHienDangKy != null) {
            btnThucHienDangKy.setOnClickListener(v -> xuLyDangKyGiangVien());
        }
    }

    private void xuLyDangNhap() {
        String userName = edtMaGV.getText().toString().trim();
        String matKhau = edtMatKhau.getText().toString().trim();

        if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(matKhau)) {
            Toast.makeText(this, getString(R.string.nhap_day_du), Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService api = ApiClient.getApiService();
        api.login(new LoginRequest(userName, matKhau)).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse res = response.body();

                    String vaiTro = res.getVaiTro();
                    String maLienQuan = res.getMaLienQuan();
                    if (maLienQuan == null || maLienQuan.isEmpty()) {
                        maLienQuan = userName; // fallback dùng userName
                    }
                    String hoTen = res.getHoTen();
                    if (hoTen == null || hoTen.isEmpty()) {
                        hoTen = userName;
                    }
                    String lop = res.getLop();
                    if (lop == null) lop = "";

                    // Lưu session
                    if (session != null) {
                        session.saveSession(res.getToken(), userName, vaiTro, maLienQuan, hoTen, lop);
                    }

                    // Map vaiTro -> role MainActivity hiểu
                    String roleForMain = mapVaiTroToRole(vaiTro);

                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                    chuyenSangMainActivity(roleForMain, maLienQuan, hoTen, lop);
                } else {
                    String msg = response.code() == 401
                            ? "Tài khoản hoặc mật khẩu không đúng!"
                            : "Đăng nhập thất bại (code " + response.code() + ")";
                    Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this,
                        "Lỗi kết nối API: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String mapVaiTroToRole(String vaiTro) {
        if (vaiTro == null) return ROLE_STUDENT;
        String v = vaiTro.toUpperCase();
        if (v.contains("ADMIN")) return ROLE_ADMIN;
        if (v.contains("TEACHER") || v.contains("GV") || v.contains("GIANG")) return ROLE_TEACHER;
        return ROLE_STUDENT;
    }

    private void xuLyDangKyGiangVien() {
        // Giữ logic cũ: đăng ký GV vào SharedPreferences - chờ admin cấp tài khoản API.
        EditText edtRegMaGV = findViewById(R.id.edtRegMaGV);
        EditText edtRegMatKhau = findViewById(R.id.edtRegMatKhau);
        EditText edtRegHoTen = findViewById(R.id.edtRegHoTen);
        EditText edtRegLopPhuTrach = findViewById(R.id.edtRegLopPhuTrach);

        String maGV = edtRegMaGV.getText().toString().trim().toUpperCase();
        String matKhau = edtRegMatKhau.getText().toString().trim();
        String hoTen = edtRegHoTen.getText().toString().trim();
        String lopPhuTrach = edtRegLopPhuTrach.getText().toString().trim();

        if (TextUtils.isEmpty(maGV) || TextUtils.isEmpty(matKhau)
                || TextUtils.isEmpty(hoTen) || TextUtils.isEmpty(lopPhuTrach)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin đăng ký!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lưu vào SharedPreferences để dùng tạm khi admin chưa cấp tài khoản.
        // Ở phiên bản này vẫn dùng TeacherManager cũ để tương thích ngược.
        com.example.qlsv.repository.TeacherManager tm =
                com.example.qlsv.repository.TeacherManager.getInstance(this);
        com.example.qlsv.model.GiaoVien gvMoi =
                new com.example.qlsv.model.GiaoVien(maGV, hoTen, "", "", "", "");
        boolean success = tm.registerTeacherCompat(gvMoi, matKhau, lopPhuTrach);

        if (success) {
            Toast.makeText(this, "Đăng ký tạm thành công. Chờ admin cấp tài khoản API!", Toast.LENGTH_SHORT).show();
            edtRegMaGV.setText("");
            edtRegMatKhau.setText("");
            edtRegHoTen.setText("");
            edtRegLopPhuTrach.setText("");

            LinearLayout panelRegister = findViewById(R.id.panelRegister);
            if (panelRegister != null) panelRegister.setVisibility(View.GONE);
            panelLogin.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, "Mã giảng viên \"" + maGV + "\" đã tồn tại!", Toast.LENGTH_SHORT).show();
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
