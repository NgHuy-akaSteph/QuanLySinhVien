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

import com.example.qlsv.adapter.SinhVienAdapter;
import com.example.qlsv.api.ApiClient;
import com.example.qlsv.model.SinhVien;
import com.example.qlsv.repository.SessionManager;
import com.example.qlsv.util.ImageLoader;
import com.example.qlsv.util.ImageUploader;

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
    private EditText edtMaSV, edtHoTen, edtNgaySinh, edtDienThoai, edtEmail, edtLop;
    private LinearLayout panelForm, panelProfile;
    private ImageView imgAvatarSv;
    private TextView tvMaSvProfile, tvHoTenProfile, tvNgaySinhProfile,
            tvLopProfile, tvDienThoaiProfile, tvEmailProfile;
    private ListView lvSinhVien;
    private SearchView searchView;

    private SinhVienAdapter adapter;

    private String userRole = "";
    private String userId = "";
    private String hoTenUser = "";
    private String lopPhuTrach = "";

    private SinhVien sinhVienDangChon = null;
    private Uri anhUriHienTai = null;
    /** URL ảnh từ server sau khi upload (gắn vào SinhVien.anhURI khi lưu). */
    private String anhUrlDaUpload = null;

    private ActivityResultLauncher<String[]> chonAnhLauncher;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Đảm bảo ApiClient đã init (trường hợp vào trực tiếp MainActivity)
        ApiClient.init(getApplicationContext());
        session = ApiClient.getSessionManager();

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
            } else if (LoginActivity.ROLE_ADMIN.equals(userRole)) {
                actionBar.setTitle("Quản Trị Hệ Thống");
                actionBar.setSubtitle("Admin: " + hoTenUser);
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
        panelForm.setVisibility(View.GONE);
        searchView.setVisibility(View.GONE);
        lvSinhVien.setVisibility(View.GONE);

        panelProfile.setVisibility(View.VISIBLE);

        ApiClient.getApiService().getSinhVienById(userId).enqueue(new Callback<SinhVien>() {
            @Override
            public void onResponse(Call<SinhVien> call, Response<SinhVien> response) {
                if (response.isSuccessful() && response.body() != null) {
                    hienThiLenProfile(response.body());
                } else if (response.code() == 401) {
                    xuLyTokenHetHan();
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
        tvNgaySinhProfile.setText(formatNgaySinh(sv.getNgaySinh()));
        tvLopProfile.setText(sv.getLop() != null ? sv.getLop() : "—");
        tvDienThoaiProfile.setText(sv.getDienThoai() != null ? sv.getDienThoai() : "—");
        tvEmailProfile.setText(sv.getEmail() != null ? sv.getEmail() : "—");

        // Load ảnh từ URL (nếu là http(s)) hoặc fallback nếu là content://
        String anh = sv.getAnhURI();
        if (!TextUtils.isEmpty(anh)) {
            if (anh.startsWith("http://") || anh.startsWith("https://")) {
                ImageLoader.loadRounded(imgAvatarSv, anh, 16f);
            } else {
                try {
                    imgAvatarSv.setImageURI(Uri.parse(anh));
                } catch (Exception e) {
                    imgAvatarSv.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            }
        } else {
            imgAvatarSv.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }

    /** Chuyển "2000-05-15T00:00:00" -> "2000-05-15" cho UI. */
    private String formatNgaySinh(String iso) {
        if (iso == null || iso.isEmpty()) return "—";
        int idx = iso.indexOf('T');
        return idx > 0 ? iso.substring(0, idx) : iso;
    }

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
        edtNgaySinh = findViewById(R.id.edtNgaySinh);
        edtDienThoai = findViewById(R.id.edtDienThoai);
        edtEmail = findViewById(R.id.edtEmail);
        edtLop = findViewById(R.id.edtLop);

        panelForm = findViewById(R.id.panelForm);
        panelProfile = findViewById(R.id.panelProfile);
        imgAvatarSv = findViewById(R.id.imgAvatarSv);
        tvMaSvProfile = findViewById(R.id.tvMaSvProfile);
        tvHoTenProfile = findViewById(R.id.tvHoTenProfile);
        tvNgaySinhProfile = findViewById(R.id.tvNgaySinhProfile);
        tvLopProfile = findViewById(R.id.tvLopProfile);
        tvDienThoaiProfile = findViewById(R.id.tvDienThoaiProfile);
        tvEmailProfile = findViewById(R.id.tvEmailProfile);

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
                                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
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
        if (requestCode == 100 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
            if (LoginActivity.ROLE_TEACHER.equals(userRole)
                    || LoginActivity.ROLE_ADMIN.equals(userRole)) {
                SinhVien sv = adapter.getItem(position);
                hienThiLenForm(sv);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }
            @Override
            public boolean onQueryTextChange(String newText) {
                timKiemTheoLop(newText);
                return true;
            }
        });
    }

    private void napDanhSachSinhVien() {
        ApiClient.getApiService().getAllSinhVien().enqueue(new Callback<List<SinhVien>>() {
            @Override
            public void onResponse(Call<List<SinhVien>> call, Response<List<SinhVien>> response) {
                if (response.code() == 401) {
                    xuLyTokenHetHan();
                    return;
                }
                if (response.isSuccessful() && response.body() != null) {
                    List<SinhVien> all = response.body();
                    List<SinhVien> result = new ArrayList<>();
                    if (!TextUtils.isEmpty(lopPhuTrach)
                            && !LoginActivity.ROLE_ADMIN.equals(userRole)) {
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
                    Toast.makeText(MainActivity.this,
                            "Không thể tải danh sách sinh viên từ API", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<SinhVien>> call, Throwable t) {
                Toast.makeText(MainActivity.this,
                        "Lỗi kết nối API: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
                if (response.code() == 401) {
                    xuLyTokenHetHan();
                    return;
                }
                if (response.isSuccessful() && response.body() != null) {
                    List<SinhVien> all = response.body();
                    List<SinhVien> result = new ArrayList<>();
                    String keywordLower = tuKhoa.toLowerCase();
                    for (SinhVien sv : all) {
                        boolean matchLopPhuTrach = TextUtils.isEmpty(lopPhuTrach)
                                || LoginActivity.ROLE_ADMIN.equals(userRole)
                                || (sv.getLop() != null
                                    && sv.getLop().equalsIgnoreCase(lopPhuTrach));
                        if (!matchLopPhuTrach) continue;

                        boolean matchLop = sv.getLop() != null
                                && sv.getLop().toLowerCase().contains(keywordLower);
                        boolean matchMaSV = sv.getMaSV() != null
                                && sv.getMaSV().toLowerCase().contains(keywordLower);
                        if (matchLop || matchMaSV) {
                            result.add(sv);
                        }
                    }
                    loadDanhSachLenListView(result);
                }
            }

            @Override
            public void onFailure(Call<List<SinhVien>> call, Throwable t) {
                Toast.makeText(MainActivity.this,
                        "Lỗi tìm kiếm: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void chuyenSangCheDoThemMoi() {
        if (LoginActivity.ROLE_STUDENT.equals(userRole)) return;

        sinhVienDangChon = null;
        anhUriHienTai = null;
        anhUrlDaUpload = null;

        edtMaSV.setText("");
        edtHoTen.setText("");
        edtNgaySinh.setText("");
        edtDienThoai.setText("");
        edtEmail.setText("");

        if (!TextUtils.isEmpty(lopPhuTrach)
                && !LoginActivity.ROLE_ADMIN.equals(userRole)) {
            edtLop.setText(lopPhuTrach);
            edtLop.setEnabled(false);
        } else {
            edtLop.setText("");
            edtLop.setEnabled(true);
        }

        imgAvatar.setImageResource(android.R.drawable.ic_menu_gallery);
        edtMaSV.setEnabled(true);
        edtHoTen.setEnabled(true);
        edtNgaySinh.setEnabled(true);
        edtDienThoai.setEnabled(true);
        edtEmail.setEnabled(true);
    }

    private void hienThiLenForm(SinhVien sv) {
        sinhVienDangChon = sv;
        anhUriHienTai = null;
        anhUrlDaUpload = null;

        edtMaSV.setText(sv.getMaSV() != null ? sv.getMaSV() : "");
        edtHoTen.setText(sv.getHoTen() != null ? sv.getHoTen() : "");
        edtNgaySinh.setText(sv.getNgaySinh() != null ? formatNgaySinh(sv.getNgaySinh()) : "");
        edtDienThoai.setText(sv.getDienThoai() != null ? sv.getDienThoai() : "");
        edtEmail.setText(sv.getEmail() != null ? sv.getEmail() : "");
        edtLop.setText(sv.getLop() != null ? sv.getLop() : "");

        if (LoginActivity.ROLE_TEACHER.equals(userRole)) {
            edtMaSV.setEnabled(false);
            edtHoTen.setEnabled(false);
            edtNgaySinh.setEnabled(false);
            edtDienThoai.setEnabled(true);
            edtEmail.setEnabled(true);
            if (!TextUtils.isEmpty(lopPhuTrach)) {
                edtLop.setEnabled(false);
            } else {
                edtLop.setEnabled(true);
            }
        }

        String anh = sv.getAnhURI();
        if (!TextUtils.isEmpty(anh)) {
            if (anh.startsWith("http://") || anh.startsWith("https://")) {
                ImageLoader.loadRounded(imgAvatar, anh, 16f);
            } else {
                try {
                    anhUriHienTai = Uri.parse(anh);
                    imgAvatar.setImageURI(anhUriHienTai);
                } catch (SecurityException e) {
                    anhUriHienTai = null;
                    imgAvatar.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            }
        } else {
            anhUriHienTai = null;
            imgAvatar.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }

    private void xuLyLuu() {
        String maSV = edtMaSV.getText().toString().trim();
        String hoTen = edtHoTen.getText().toString().trim();
        String ngaySinh = edtNgaySinh.getText().toString().trim();
        String dienThoai = edtDienThoai.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String lop = edtLop.getText().toString().trim();

        if (TextUtils.isEmpty(maSV) || TextUtils.isEmpty(hoTen)
                || TextUtils.isEmpty(ngaySinh) || TextUtils.isEmpty(lop)) {
            Toast.makeText(this, "Vui lòng nhập: mã SV, họ tên, ngày sinh, lớp",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Chuẩn hóa ngaySinh: nếu user nhập "2000-05-15" -> ISO "2000-05-15T00:00:00"
        String ngaySinhIso = chuanHoaNgaySinh(ngaySinh);

        // Nếu có ảnh local mới chọn -> upload trước
        if (anhUriHienTai != null && sinhVienDangChon != null) {
            uploadAvatarRoiLuu(maSV, hoTen, ngaySinhIso, dienThoai, email, lop);
            return;
        }
        // Thêm mới mà có ảnh -> upload
        if (anhUriHienTai != null && sinhVienDangChon == null) {
            uploadAvatarRoiLuu(maSV, hoTen, ngaySinhIso, dienThoai, email, lop);
            return;
        }

        SinhVien sv = new SinhVien(maSV, hoTen, ngaySinhIso, lop, dienThoai, email,
                anhUrlDaUpload != null ? anhUrlDaUpload : "");

        if (sinhVienDangChon == null) {
            goiApiCreate(sv);
        } else {
            goiApiUpdate(maSV, sv);
        }
    }

    /** "2000-05-15" -> "2000-05-15T00:00:00"; nếu đã có T thì giữ nguyên. */
    private String chuanHoaNgaySinh(String input) {
        if (input == null) return "";
        String s = input.trim();
        if (s.contains("T")) return s;
        if (s.length() == 10 && s.charAt(4) == '-' && s.charAt(7) == '-') {
            return s + "T00:00:00";
        }
        return s;
    }

    private void uploadAvatarRoiLuu(String maSV, String hoTen, String ngaySinhIso,
                                    String dienThoai, String email, String lop) {
        final Uri uri = anhUriHienTai;
        final String finalMa = maSV;
        ImageUploader.uploadAvatarSv(maSV, uri, this, new ImageUploader.UploadCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                anhUrlDaUpload = imageUrl;
                SinhVien sv = new SinhVien(finalMa, hoTen, ngaySinhIso, lop,
                        dienThoai, email, imageUrl);
                if (sinhVienDangChon == null) {
                    goiApiCreate(sv);
                } else {
                    goiApiUpdate(finalMa, sv);
                }
            }
            @Override
            public void onFailure(String error) {
                Toast.makeText(MainActivity.this,
                        "Upload ảnh thất bại: " + error, Toast.LENGTH_LONG).show();
                // Vẫn cho lưu SV không có ảnh
                SinhVien sv = new SinhVien(finalMa, hoTen, ngaySinhIso, lop,
                        dienThoai, email, "");
                if (sinhVienDangChon == null) {
                    goiApiCreate(sv);
                } else {
                    goiApiUpdate(finalMa, sv);
                }
            }
        });
    }

    private void goiApiCreate(SinhVien sv) {
        ApiClient.getApiService().createSinhVien(sv).enqueue(new Callback<SinhVien>() {
            @Override
            public void onResponse(Call<SinhVien> call, Response<SinhVien> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Thêm sinh viên thành công",
                            Toast.LENGTH_SHORT).show();
                    napDanhSachSinhVien();
                    chuyenSangCheDoThemMoi();
                } else if (response.code() == 401) {
                    xuLyTokenHetHan();
                } else {
                    Toast.makeText(MainActivity.this, "Thêm thất bại (mã SV có thể đã tồn tại)",
                            Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<SinhVien> call, Throwable t) {
                Toast.makeText(MainActivity.this,
                        "Lỗi kết nối API: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goiApiUpdate(String maSV, SinhVien sv) {
        ApiClient.getApiService().updateSinhVien(maSV, sv).enqueue(new Callback<SinhVien>() {
            @Override
            public void onResponse(Call<SinhVien> call, Response<SinhVien> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Cập nhật thành công",
                            Toast.LENGTH_SHORT).show();
                    napDanhSachSinhVien();
                    chuyenSangCheDoThemMoi();
                } else if (response.code() == 401) {
                    xuLyTokenHetHan();
                } else {
                    Toast.makeText(MainActivity.this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<SinhVien> call, Throwable t) {
                Toast.makeText(MainActivity.this,
                        "Lỗi kết nối API: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void xuLyXoa() {
        if (sinhVienDangChon == null) {
            Toast.makeText(this, "Vui lòng chọn 1 sinh viên trong danh sách để xóa",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    ApiClient.getApiService().deleteSinhVien(sinhVienDangChon.getMaSV())
                            .enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call,
                                                       Response<ResponseBody> response) {
                                    if (response.isSuccessful()) {
                                        Toast.makeText(MainActivity.this, "Đã xóa sinh viên",
                                                Toast.LENGTH_SHORT).show();
                                        napDanhSachSinhVien();
                                        chuyenSangCheDoThemMoi();
                                    } else if (response.code() == 401) {
                                        xuLyTokenHetHan();
                                    } else {
                                        Toast.makeText(MainActivity.this, "Xóa thất bại",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                                @Override
                                public void onFailure(Call<ResponseBody> call, Throwable t) {
                                    Toast.makeText(MainActivity.this,
                                            "Lỗi kết nối API: " + t.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        // Phân quyền: chỉ ADMIN mới thấy các mục quản lý
        boolean isAdmin = LoginActivity.ROLE_ADMIN.equals(userRole);
        menu.findItem(R.id.action_quan_ly_gv).setVisible(isAdmin);
        menu.findItem(R.id.action_quan_ly_lop).setVisible(isAdmin);
        menu.findItem(R.id.action_quan_ly_tai_khoan).setVisible(isAdmin);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            dangXuat();
            return true;
        } else if (itemId == R.id.action_dang_xuat) {
            dangXuat();
            return true;
        } else if (itemId == R.id.action_thoat) {
            finishAffinity();
            return true;
        } else if (itemId == R.id.action_doi_mat_khau) {
            startActivity(new Intent(this, ChangePasswordActivity.class));
            return true;
        } else if (itemId == R.id.action_quan_ly_gv) {
            startActivity(new Intent(this, TeacherManagementActivity.class));
            return true;
        } else if (itemId == R.id.action_quan_ly_lop) {
            startActivity(new Intent(this, ClassManagementActivity.class));
            return true;
        } else if (itemId == R.id.action_quan_ly_tai_khoan) {
            startActivity(new Intent(this, AccountManagementActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void dangXuat() {
        if (session != null) session.clear();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void xuLyTokenHetHan() {
        Toast.makeText(this, "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.",
                Toast.LENGTH_LONG).show();
        dangXuat();
    }
}
