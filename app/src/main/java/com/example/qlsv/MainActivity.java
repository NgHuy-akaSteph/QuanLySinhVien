package com.example.qlsv;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.example.qlsv.adapter.SinhVienAdapter;
import com.example.qlsv.api.ApiClient;
import com.example.qlsv.model.SinhVien;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private ImageView imgAvatar;
    private Button btnChonAnh, btnThem, btnLuu, btnHuy, btnXoa;
    private ImageButton btnThoat;
    private EditText edtMaSV, edtHoTen, edtNamSinh, edtDiaChi, edtLop;
    private LinearLayout panelForm, panelProfile;
    private ImageView imgAvatarSv;
    private TextView tvMaSvProfile, tvHoTenProfile, tvNamSinhProfile, tvDiaChiProfile, tvLopProfile;
    private ListView lvSinhVien;
    private SearchView searchView;

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
        // Ẩn form chỉnh sửa + tìm kiếm + danh sách, hiện panel hồ sơ chỉ xem
        panelForm.setVisibility(View.GONE);
        searchView.setVisibility(View.GONE);
        lvSinhVien.setVisibility(View.GONE);

        panelProfile.setVisibility(View.VISIBLE);

        ApiClient.getApiService().getSinhVienById(userId).enqueue(new Callback<SinhVien>() {
            @Override
            public void onResponse(Call<SinhVien> call, Response<SinhVien> response) {
                if (response.isSuccessful() && response.body() != null) {
                    hienThiLenProfile(response.body());
                } else {
                    Toast.makeText(MainActivity.this, "Không tìm thấy thông tin sinh viên", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SinhVien> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Lỗi kết nối API: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void hienThiLenProfile(SinhVien sv) {
        tvMaSvProfile.setText(sv.getMaSV() != null ? sv.getMaSV() : "—");
        tvHoTenProfile.setText(sv.getHoTen() != null ? sv.getHoTen() : "—");
        tvNamSinhProfile.setText(String.valueOf(sv.getNamSinh()));
        tvDiaChiProfile.setText(sv.getDiaChi() != null ? sv.getDiaChi() : "—");
        tvLopProfile.setText(sv.getLop() != null ? sv.getLop() : "—");

        String anh = sv.getAnhURI();
        if (!TextUtils.isEmpty(anh)) {
            try {
                Uri uri = Uri.parse(anh);
                Bitmap bitmap = android.provider.MediaStore.Images.Media.getBitmap(
                        getContentResolver(), uri);
                RoundedBitmapDrawable rbd = RoundedBitmapDrawableFactory.create(
                        getResources(), bitmap);
                rbd.setCornerRadius(16f);
                rbd.setAntiAlias(true);
                imgAvatarSv.setImageDrawable(rbd);
            } catch (Exception e) {
                imgAvatarSv.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
            imgAvatarSv.setImageResource(android.R.drawable.ic_menu_gallery);
        }
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

        panelForm = findViewById(R.id.panelForm);
        panelProfile = findViewById(R.id.panelProfile);
        imgAvatarSv = findViewById(R.id.imgAvatarSv);
        tvMaSvProfile = findViewById(R.id.tvMaSvProfile);
        tvHoTenProfile = findViewById(R.id.tvHoTenProfile);
        tvNamSinhProfile = findViewById(R.id.tvNamSinhProfile);
        tvDiaChiProfile = findViewById(R.id.tvDiaChiProfile);
        tvLopProfile = findViewById(R.id.tvLopProfile);

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
        ApiClient.getApiService().getAllSinhVien().enqueue(new Callback<List<SinhVien>>() {
            @Override
            public void onResponse(Call<List<SinhVien>> call, Response<List<SinhVien>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<SinhVien> all = response.body();
                    List<SinhVien> result = new ArrayList<>();
                    if (!TextUtils.isEmpty(lopPhuTrach)) {
                        for (SinhVien sv : all) {
                            if (sv.getLop() != null && sv.getLop().equalsIgnoreCase(lopPhuTrach)) {
                                result.add(sv);
                            }
                        }
                    } else {
                        result.addAll(all);
                    }
                    loadDanhSachLenListView(result);
                } else {
                    Toast.makeText(MainActivity.this, "Không thể tải danh sách sinh viên từ API", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<SinhVien>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Lỗi kết nối API: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
        if (TextUtils.isEmpty(tuKhoa)) {
            napDanhSachSinhVien();
            return;
        }

        ApiClient.getApiService().getAllSinhVien().enqueue(new Callback<List<SinhVien>>() {
            @Override
            public void onResponse(Call<List<SinhVien>> call, Response<List<SinhVien>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<SinhVien> all = response.body();
                    List<SinhVien> result = new ArrayList<>();
                    String keywordLower = tuKhoa.toLowerCase();
                    for (SinhVien sv : all) {
                        boolean matchLopPhuTrach = TextUtils.isEmpty(lopPhuTrach) ||
                                (sv.getLop() != null && sv.getLop().equalsIgnoreCase(lopPhuTrach));
                        if (!matchLopPhuTrach) continue;

                        boolean matchLop = sv.getLop() != null && sv.getLop().toLowerCase().contains(keywordLower);
                        boolean matchMaSV = sv.getMaSV() != null && sv.getMaSV().toLowerCase().contains(keywordLower);
                        if (matchLop || matchMaSV) {
                            result.add(sv);
                        }
                    }
                    loadDanhSachLenListView(result);
                }
            }

            @Override
            public void onFailure(Call<List<SinhVien>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Lỗi tìm kiếm: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
        SinhVien sv = new SinhVien(maSV, hoTen, namSinh, diaChi, lop, anhURI);

        if (sinhVienDangChon == null) {
            ApiClient.getApiService().createSinhVien(sv).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(MainActivity.this, "Thêm sinh viên thành công", Toast.LENGTH_SHORT).show();
                        napDanhSachSinhVien();
                        chuyenSangCheDoThemMoi();
                    } else {
                        Toast.makeText(MainActivity.this, "Thêm thất bại hoặc mã SV đã tồn tại", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(MainActivity.this, "Lỗi kết nối API: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            ApiClient.getApiService().updateSinhVien(maSV, sv).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(MainActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                        napDanhSachSinhVien();
                        chuyenSangCheDoThemMoi();
                    } else {
                        Toast.makeText(MainActivity.this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(MainActivity.this, "Lỗi kết nối API: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
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
                    ApiClient.getApiService().deleteSinhVien(sinhVienDangChon.getMaSV()).enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(MainActivity.this, "Đã xóa sinh viên", Toast.LENGTH_SHORT).show();
                                napDanhSachSinhVien();
                                chuyenSangCheDoThemMoi();
                            } else {
                                Toast.makeText(MainActivity.this, "Xóa thất bại", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Toast.makeText(MainActivity.this, "Lỗi kết nối API: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
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
