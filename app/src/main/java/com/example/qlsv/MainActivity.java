package com.example.qlsv;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ImageView imgAvatar;
    private Button btnChonAnh, btnThem, btnLuu, btnHuy, btnXoa, btnThoat;
    private EditText edtMaSV, edtHoTen, edtNamSinh, edtDiaChi, edtLop;
    private ListView lvSinhVien;
    private SearchView searchView;

    private DatabaseHelper dbHelper;
    private SinhVienAdapter adapter;

    // null => đang ở chế độ THÊM MỚI; khác null => đang xem/sửa sinh viên này
    private SinhVien sinhVienDangChon = null;
    // Uri ảnh đại diện đang chọn cho form hiện tại
    private Uri anhUriHienTai = null;

    private ActivityResultLauncher<String[]> chonAnhLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        // Đăng ký ActivityResultLauncher cho việc chọn ảnh
        dangKyChonAnhLauncher();
        anhXaView();
        ganSuKienChoView();

        loadDanhSachLenListView(dbHelper.getAllSinhVien());
        chuyenSangCheDoThemMoi();
    }

    /**
     * Đăng ký ActivityResultLauncher dùng chung cho cả SAF và runtime permission.
     * SAF: gọi takePersistableUriPermission để lưu quyền vĩnh viễn.
     * Runtime permission: đã có quyền từ trước, không cần takePersistableUriPermission.
     */
    private void dangKyChonAnhLauncher() {
        chonAnhLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri != null) {
                        // Lưu quyền vĩnh viễn để ảnh vẫn hiển thị sau khi restart app
                        try {
                            getContentResolver().takePersistableUriPermission(
                                    uri,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                            );
                        } catch (SecurityException e) {
                            // SAF: không xin được quyền vĩnh viễn -> vẫn dùng được ảnh trong phiên
                            // Runtime permission: bình thường, quyền đã được cấp rồi
                        }
                        anhUriHienTai = uri;
                        imgAvatar.setImageURI(uri);
                    }
                }
        );
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

    /**
     * Kiểm tra và xin quyền truy cập ảnh theo cách runtime permission (hướng dẫn của thầy).
     * - Android 13+ (API 33+): xin quyền READ_MEDIA_IMAGES
     * - Android 6.0 - 12 (API 23-32): xin quyền READ_EXTERNAL_STORAGE
     * - Android < 6.0: tự động mở picker ảnh (không cần xin quyền)
     */
    private void kiemTraVaXinQuyen() {
        String quyenCanXin;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            quyenCanXin = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            quyenCanXin = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(this, quyenCanXin)
                != PackageManager.PERMISSION_GRANTED) {
            // Chưa có quyền -> xin quyền
            ActivityCompat.requestPermissions(this,
                    new String[]{quyenCanXin}, 100);
        } else {
            // Đã có quyền -> mở picker ảnh
            chonAnhLauncher.launch(new String[]{"image/*"});
        }
    }

    /**
     * Xử lý kết quả khi user trả lời hộp thoại xin quyền.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
            int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // User đồng ý cấp quyền -> mở picker ảnh
            chonAnhLauncher.launch(new String[]{"image/*"});
        } else {
            // User từ chối
            Toast.makeText(this, "Cần quyền truy cập ảnh để tiếp tục", Toast.LENGTH_SHORT).show();
        }
    }

    private void ganSuKienChoView() {
        // Cách runtime permission (theo hướng dẫn của thầy):
        btnChonAnh.setOnClickListener(v -> kiemTraVaXinQuyen());

        /* =====================================================================
         * CÁCH SAF (Storage Access Framework) - Optional
         * =====================================================================
         * Nếu muốn dùng SAF thay vì runtime permission, làm theo các bước:
         * 1. Comment dòng btnChonAnh.setOnClickListener bên trên
         * 2. Bỏ comment dòng bên dưới:
         */
        // btnChonAnh.setOnClickListener(v -> chonAnhLauncher.launch(new String[]{"image/*"}));
        /* ===================================================================== */

        btnThem.setOnClickListener(v -> chuyenSangCheDoThemMoi());
        btnHuy.setOnClickListener(v -> chuyenSangCheDoThemMoi());
        btnLuu.setOnClickListener(v -> xuLyLuu());
        btnXoa.setOnClickListener(v -> xuLyXoa());
        btnThoat.setOnClickListener(v -> finishAffinity());

        lvSinhVien.setOnItemClickListener((parent, view, position, id) -> {
            SinhVien sv = adapter.getItem(position);
            hienThiLenForm(sv);
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
            loadDanhSachLenListView(dbHelper.getAllSinhVien());
        } else {
            loadDanhSachLenListView(dbHelper.timTheoLop(tuKhoa));
        }
    }

    // ------------------ Các chế độ của Form ------------------

    /**
     * Reset form về trạng thái trống, sẵn sàng nhập sinh viên mới.
     * Mở khóa toàn bộ EditText.
     */
    private void chuyenSangCheDoThemMoi() {
        sinhVienDangChon = null;
        anhUriHienTai = null;

        edtMaSV.setText("");
        edtHoTen.setText("");
        edtNamSinh.setText("");
        edtDiaChi.setText("");
        edtLop.setText("");
        imgAvatar.setImageResource(android.R.drawable.ic_menu_gallery);

        edtMaSV.setEnabled(true);
        edtHoTen.setEnabled(true);
        edtNamSinh.setEnabled(true);
        edtDiaChi.setEnabled(true);
        edtLop.setEnabled(true);
    }

    /**
     * Đổ dữ liệu 1 sinh viên (được click trên ListView) lên form.
     * Khóa Mã SV / Họ tên / Năm sinh, chỉ cho sửa Địa chỉ, Lớp và đổi Ảnh.
     */
    private void hienThiLenForm(SinhVien sv) {
        sinhVienDangChon = sv;

        edtMaSV.setText(sv.getMaSV());
        edtHoTen.setText(sv.getHoTen());
        edtNamSinh.setText(String.valueOf(sv.getNamSinh()));
        edtDiaChi.setText(sv.getDiaChi());
        edtLop.setText(sv.getLop());

        edtMaSV.setEnabled(false);
        edtHoTen.setEnabled(false);
        edtNamSinh.setEnabled(false);
        edtDiaChi.setEnabled(true);
        edtLop.setEnabled(true);

        String anh = sv.getAnhURI();
        if (!TextUtils.isEmpty(anh)) {
            try {
                anhUriHienTai = Uri.parse(anh);
                imgAvatar.setImageURI(anhUriHienTai);
            } catch (SecurityException e) {
                // Quyền đã bị thu hồi (vd: ảnh gốc đã bị xóa) -> dùng ảnh mặc định
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
            // Chế độ thêm mới
            long result = dbHelper.insertSinhVien(sv);
            if (result == -1) {
                Toast.makeText(this, "Mã SV \"" + maSV + "\" đã tồn tại", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "Thêm sinh viên thành công", Toast.LENGTH_SHORT).show();
        } else {
            // Chế độ cập nhật, dùng maSV làm khóa tìm kiếm (maSV không đổi được)
            int rows = dbHelper.updateSinhVien(sv);
            if (rows <= 0) {
                Toast.makeText(this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
        }

        loadDanhSachLenListView(dbHelper.getAllSinhVien());
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
                    dbHelper.deleteSinhVien(sinhVienDangChon.getMaSV());
                    Toast.makeText(this, "Đã xóa sinh viên", Toast.LENGTH_SHORT).show();
                    loadDanhSachLenListView(dbHelper.getAllSinhVien());
                    chuyenSangCheDoThemMoi();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    /* =====================================================================
     * CÁCH SAF (Storage Access Framework) - Optional
     * =====================================================================
     * Cách SAF không cần xin quyền READ_EXTERNAL_STORAGE, và cho phép xin quyền
     * truy cập vĩnh viễn (takePersistableUriPermission) để ảnh vẫn hiển thị được
     * trên ListView sau khi tắt/mở lại app, tránh crash SecurityException.
     *
     * Nếu muốn dùng SAF thay vì runtime permission:
     * 1. Comment toàn bộ method kiemTraVaXinQuyen()
     * 2. Comment method onRequestPermissionsResult()
     * 3. Trong ganSuKienChoView(), comment dòng kiemTraVaXinQuyen() 
     *    và bỏ comment dòng chonAnhLauncher.launch()
     *
     * private void dangKyChonAnhLauncher_SAF() {
     *     chonAnhLauncher = registerForActivityResult(
     *             new ActivityResultContracts.OpenDocument(),
     *             uri -> {
     *                 if (uri != null) {
     *                     try {
     *                         getContentResolver().takePersistableUriPermission(
     *                                 uri,
     *                                 Intent.FLAG_GRANT_READ_URI_PERMISSION
     *                         );
     *                     } catch (SecurityException e) {
     *                         Toast.makeText(this, "Không xin được quyền truy cập ảnh vĩnh viễn", Toast.LENGTH_SHORT).show();
     *                     }
     *                     anhUriHienTai = uri;
     *                     imgAvatar.setImageURI(uri);
     *                 }
     *             }
     *     );
     * }
     * ===================================================================== */
}
