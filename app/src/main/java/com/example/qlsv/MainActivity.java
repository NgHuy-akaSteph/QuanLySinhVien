package com.example.qlsv;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.qlsv.adapter.SinhVienAdapter;
import com.example.qlsv.database.AppDatabase;
import com.example.qlsv.model.SinhVien;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ImageView imgAvatar;
    private Button btnChonAnh, btnThem, btnLuu, btnHuy, btnXoa, btnThoat;
    private EditText edtMaSV, edtHoTen, edtNamSinh, edtDiaChi, edtLop;
    private ListView lvSinhVien;
    private SearchView searchView;

    private AppDatabase db;
    private SinhVienAdapter adapter;

    private String userRole = "";
    private String userId = "";
    private String hoTenUser = "";
    private String lopPhuTrach = "";

    // null => đang ở chế độ THÊM MỚI; khác null => đang xem/sửa sinh viên này
    private SinhVien sinhVienDangChon = null;
    private Uri anhUriHienTai = null;

    private ActivityResultLauncher<String[]> chonAnhLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = AppDatabase.getInstance(this);

        docDuLieuTuIntent();
        thietLapActionBar();

        dangKyChonAnhLauncher();
        anhXaView();
        ganSuKienChoView();

        if (LoginActivity.ROLE_STUDENT.equals(userRole)) {
            thietLapGiaoDienSinhVien();
        } else {
            napDanhSachSinhVien();
            chuyenSangCheDoThemMoi();
        }
    }

    private void docDuLieuTuIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            userRole = intent.getStringExtra(LoginActivity.EXTRA_ROLE);
            userId = intent.getStringExtra(LoginActivity.EXTRA_USER_ID);
            hoTenUser = intent.getStringExtra(LoginActivity.EXTRA_HO_TEN);
            lopPhuTrach = intent.getStringExtra(LoginActivity.EXTRA_LOP);
        }
        if (userRole == null) userRole = "";
        if (userId == null) userId = "";
        if (hoTenUser == null) hoTenUser = "";
        if (lopPhuTrach == null) lopPhuTrach = "";
    }

    private void thietLapActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            if (LoginActivity.ROLE_STUDENT.equals(userRole)) {
                actionBar.setTitle("Hồ Sơ Sinh Viên");
                actionBar.setSubtitle("SV: " + hoTenUser + " - Lớp: " + lopPhuTrach);
            } else {
                actionBar.setTitle("Quản Lý Sinh Viên");
                if (!TextUtils.isEmpty(lopPhuTrach)) {
                    actionBar.setSubtitle("GV: " + hoTenUser + " - Lớp: " + lopPhuTrach);
                } else {
                    actionBar.setSubtitle("GV: " + hoTenUser);
                }
            }
        }
    }

    private void thietLapGiaoDienSinhVien() {
        SinhVien sv = db.sinhVienDao().getSinhVienByMaSV(userId);

        if (sv != null) {
            hienThiLenForm(sv);
        }

        btnChonAnh.setEnabled(false);
        edtMaSV.setEnabled(false);
        edtHoTen.setEnabled(false);
        edtNamSinh.setEnabled(false);
        edtDiaChi.setEnabled(false);
        edtLop.setEnabled(false);

        btnThem.setVisibility(View.GONE);
        btnLuu.setVisibility(View.GONE);
        btnHuy.setVisibility(View.GONE);
        btnXoa.setVisibility(View.GONE);
    }

    // ------------------ Khởi tạo ------------------

    private void anhXaView() {
        imgAvatar = findViewById(R.id.imgAvatar);
        btnChonAnh = findViewById(R.id.btnChonAnh);
        btnThem = findViewById(R.id.btnThem);
        btnLuu = findViewById(R.id.btnLuu);
        btnHuy = findViewById(R.id.btnHuy);
        btnXoa = findViewById(R.id.btnXoa);
        btnThoat = findViewById(R.id.btnThoat);

        edtMaSV = findViewById(R.id.edtMaSV);
        edtHoTen = findViewById(R.id.edtHoTen);
        edtNamSinh = findViewById(R.id.edtNamSinh);
        edtDiaChi = findViewById(R.id.edtDiaChi);
        edtLop = findViewById(R.id.edtLop);

        lvSinhVien = findViewById(R.id.lvSinhVien);
        searchView = findViewById(R.id.searchView);
    }

    private void dangKyChonAnhLauncher() {
        chonAnhLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri != null) {
                        try {
                            getContentResolver().takePersistableUriPermission(
                                    uri,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                            );
                        } catch (SecurityException ignored) {
                        }
                        anhUriHienTai = uri;
                        imgAvatar.setImageURI(uri);
                    }
                }
        );
    }

    private void kiemTraVaXinQuyen() {
        String quyenCanXin;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            quyenCanXin = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            quyenCanXin = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(this, quyenCanXin)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{quyenCanXin}, 100);
        } else {
            chonAnhLauncher.launch(new String[]{"image/*"});
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            chonAnhLauncher.launch(new String[]{"image/*"});
        } else {
            Toast.makeText(this, "Cần quyền truy cập ảnh để tiếp tục", Toast.LENGTH_SHORT).show();
        }
    }

    private void ganSuKienChoView() {
        btnChonAnh.setOnClickListener(v -> kiemTraVaXinQuyen());

        btnThem.setOnClickListener(v -> chuyenSangCheDoThemMoi());
        btnHuy.setOnClickListener(v -> chuyenSangCheDoThemMoi());
        btnLuu.setOnClickListener(v -> xuLyLuu());
        btnXoa.setOnClickListener(v -> xuLyXoa());
        if (btnThoat != null) {
            btnThoat.setOnClickListener(v -> finishAffinity());
        }

        lvSinhVien.setOnItemClickListener((parent, view, position, id) -> {
            if (LoginActivity.ROLE_TEACHER.equals(userRole)) {
                SinhVien sv = adapter.getItem(position);
                hienThiLenForm(sv);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                timKiemTheoLop(newText);
                return true;
            }
        });
    }

    // ------------------ Nạp dữ liệu ------------------

    private void napDanhSachSinhVien() {
        List<SinhVien> danhSach;
        if (!TextUtils.isEmpty(lopPhuTrach)) {
            danhSach = db.sinhVienDao().getSinhVienTheoLop(lopPhuTrach);
        } else {
            danhSach = db.sinhVienDao().getAllSinhVien();
        }
        loadDanhSachLenListView(danhSach);
    }

    private void loadDanhSachLenListView(List<SinhVien> danhSach) {
        if (adapter == null) {
            adapter = new SinhVienAdapter(this, danhSach);
            lvSinhVien.setAdapter(adapter);
        } else {
            adapter.capNhatDuLieu(danhSach);
        }
    }

    private void timKiemTheoLop(String tuKhoa) {
        List<SinhVien> danhSach;
        if (TextUtils.isEmpty(tuKhoa)) {
            napDanhSachSinhVien();
            return;
        }

        if (!TextUtils.isEmpty(lopPhuTrach)) {
            danhSach = db.sinhVienDao().timTheoLopCuaGV(lopPhuTrach, tuKhoa);
        } else {
            danhSach = db.sinhVienDao().timTheoLop(tuKhoa);
        }
        loadDanhSachLenListView(danhSach);
    }

    // ------------------ Các chế độ của Form ------------------

    private void chuyenSangCheDoThemMoi() {
        if (LoginActivity.ROLE_STUDENT.equals(userRole)) return;

        sinhVienDangChon = null;
        anhUriHienTai = null;

        edtMaSV.setText("");
        edtHoTen.setText("");
        edtNamSinh.setText("");
        edtDiaChi.setText("");

        if (!TextUtils.isEmpty(lopPhuTrach)) {
            edtLop.setText(lopPhuTrach);
            edtLop.setEnabled(false);
        } else {
            edtLop.setText("");
            edtLop.setEnabled(true);
        }

        imgAvatar.setImageResource(android.R.drawable.ic_menu_gallery);

        edtMaSV.setEnabled(true);
        edtHoTen.setEnabled(true);
        edtNamSinh.setEnabled(true);
        edtDiaChi.setEnabled(true);
    }

    private void hienThiLenForm(SinhVien sv) {
        sinhVienDangChon = sv;

        edtMaSV.setText(sv.getMaSV());
        edtHoTen.setText(sv.getHoTen());
        edtNamSinh.setText(String.valueOf(sv.getNamSinh()));
        edtDiaChi.setText(sv.getDiaChi());
        edtLop.setText(sv.getLop());

        if (LoginActivity.ROLE_TEACHER.equals(userRole)) {
            edtMaSV.setEnabled(false);
            edtHoTen.setEnabled(false);
            edtNamSinh.setEnabled(false);
            edtDiaChi.setEnabled(true);

            if (!TextUtils.isEmpty(lopPhuTrach)) {
                edtLop.setEnabled(false);
            } else {
                edtLop.setEnabled(true);
            }
        }

        String anh = sv.getAnhURI();
        if (!TextUtils.isEmpty(anh)) {
            try {
                anhUriHienTai = Uri.parse(anh);
                imgAvatar.setImageURI(anhUriHienTai);
            } catch (SecurityException e) {
                anhUriHienTai = null;
                imgAvatar.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
            anhUriHienTai = null;
            imgAvatar.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }

    // ------------------ Lưu (Thêm mới / Cập nhật) ------------------

    private void xuLyLuu() {
        String maSV = edtMaSV.getText().toString().trim();
        String hoTen = edtHoTen.getText().toString().trim();
        String namSinhStr = edtNamSinh.getText().toString().trim();
        String diaChi = edtDiaChi.getText().toString().trim();
        String lop = edtLop.getText().toString().trim();

        if (TextUtils.isEmpty(maSV) || TextUtils.isEmpty(hoTen)
                || TextUtils.isEmpty(namSinhStr) || TextUtils.isEmpty(diaChi)
                || TextUtils.isEmpty(lop)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        int namSinh;
        try {
            namSinh = Integer.parseInt(namSinhStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Năm sinh phải là số", Toast.LENGTH_SHORT).show();
            return;
        }

        String anhURI = anhUriHienTai != null ? anhUriHienTai.toString() : "";
        SinhVien sv = new SinhVien(maSV, hoTen, namSinh, diaChi, lop, anhURI, maSV);

        if (sinhVienDangChon == null) {
            long result = db.sinhVienDao().insertSinhVien(sv);
            if (result == -1 || result == 0) {
                Toast.makeText(this, "Mã SV \"" + maSV + "\" đã tồn tại", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "Thêm sinh viên thành công", Toast.LENGTH_SHORT).show();
        } else {
            int rows = db.sinhVienDao().updateSinhVienByMaSV(maSV, hoTen, namSinh, diaChi, lop, anhURI);
            if (rows <= 0) {
                Toast.makeText(this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
        }

        napDanhSachSinhVien();
        chuyenSangCheDoThemMoi();
    }

    // ------------------ Xóa ------------------

    private void xuLyXoa() {
        if (sinhVienDangChon == null) {
            Toast.makeText(this, "Vui lòng chọn 1 sinh viên trong danh sách để xóa", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    db.sinhVienDao().deleteSinhVien(sinhVienDangChon.getMaSV());
                    Toast.makeText(this, "Đã xóa sinh viên", Toast.LENGTH_SHORT).show();
                    napDanhSachSinhVien();
                    chuyenSangCheDoThemMoi();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // ------------------ Menu Options (Đăng xuất / Back / Thoát) ------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return true;
        } else if (itemId == R.id.action_dang_xuat) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return true;
        } else if (itemId == R.id.action_thoat) {
            finishAffinity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
