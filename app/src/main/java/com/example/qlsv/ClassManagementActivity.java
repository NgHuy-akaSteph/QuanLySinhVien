package com.example.qlsv;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.qlsv.adapter.LopAdapter;
import com.example.qlsv.api.ApiClient;
import com.example.qlsv.model.Lop;
import com.example.qlsv.model.LopCreateRequest;
import com.example.qlsv.model.PhanCongGvcnRequest;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ClassManagementActivity extends AppCompatActivity {

    private ListView lvLop;
    private Button btnThemLop;
    private LopAdapter adapter;
    private final List<Lop> dsLop = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_management);
        getSupportActionBar().setTitle("Quản lý Lớp");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        lvLop = findViewById(R.id.lvLop);
        btnThemLop = findViewById(R.id.btnThemLop);

        adapter = new LopAdapter(this, dsLop);
        lvLop.setAdapter(adapter);

        btnThemLop.setOnClickListener(v -> moDialogForm(null));

        lvLop.setOnItemClickListener((p, view, pos, id) -> moDialogForm(dsLop.get(pos)));
        lvLop.setOnItemLongClickListener((p, view, pos, id) -> {
            confirmXoa(dsLop.get(pos));
            return true;
        });

        napDanhSach();
    }

    private void napDanhSach() {
        ApiClient.getApiService().getAllLop().enqueue(new Callback<List<Lop>>() {
            @Override
            public void onResponse(Call<List<Lop>> call, Response<List<Lop>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    dsLop.clear();
                    dsLop.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(ClassManagementActivity.this,
                            "Tải danh sách lớp thất bại", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<List<Lop>> call, Throwable t) {
                Toast.makeText(ClassManagementActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void moDialogForm(Lop lop) {
        View view = getLayoutInflater().inflate(R.layout.dialog_class_form, null);
        EditText edtLop = view.findViewById(R.id.edtLopForm);
        EditText edtNam = view.findViewById(R.id.edtNamNhapHocForm);
        EditText edtMaGv = view.findViewById(R.id.edtMaGvPhanCongForm);

        boolean isEdit = lop != null;
        if (isEdit) {
            edtLop.setText(lop.getLop());
            edtLop.setEnabled(false);
            if (lop.getNamNhapHoc() != null) edtNam.setText(String.valueOf(lop.getNamNhapHoc()));
            if (lop.getMaGVCN() != null) edtMaGv.setText(lop.getMaGVCN());
        }

        new AlertDialog.Builder(this)
                .setTitle(isEdit ? "Sửa / Phân công GVCN" : "Thêm lớp")
                .setView(view)
                .setPositiveButton("Lưu", (d, w) -> {
                    String tenLop = edtLop.getText().toString().trim();
                    int nam = 0;
                    try {
                        nam = Integer.parseInt(edtNam.getText().toString().trim());
                    } catch (NumberFormatException ignored) {}
                    if (tenLop.isEmpty() || nam == 0) {
                        Toast.makeText(this, "Tên lớp và năm nhập học bắt buộc",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (isEdit) {
                        String maGV = edtMaGv.getText().toString().trim();
                        if (!maGV.isEmpty()) {
                            phanCongGv(tenLop, maGV);
                        } else {
                            Toast.makeText(this, "Đã lưu (không đổi GVCN)",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        ApiClient.getApiService().createLop(new LopCreateRequest(tenLop, nam))
                                .enqueue(new Callback<Lop>() {
                                    @Override
                                    public void onResponse(Call<Lop> call, Response<Lop> response) {
                                        if (response.isSuccessful()) {
                                            Toast.makeText(ClassManagementActivity.this,
                                                    "Thêm lớp thành công",
                                                    Toast.LENGTH_SHORT).show();
                                            napDanhSach();
                                        } else {
                                            Toast.makeText(ClassManagementActivity.this,
                                                    "Thêm thất bại (mã " + response.code() + ")",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    @Override
                                    public void onFailure(Call<Lop> call, Throwable t) {
                                        Toast.makeText(ClassManagementActivity.this,
                                                "Lỗi: " + t.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void phanCongGv(String lop, String maGV) {
        ApiClient.getApiService().phanCongGvcn(lop, new PhanCongGvcnRequest(maGV))
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(ClassManagementActivity.this,
                                    "Phân công GVCN thành công", Toast.LENGTH_SHORT).show();
                            napDanhSach();
                        } else {
                            Toast.makeText(ClassManagementActivity.this, "Phân công thất bại",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(ClassManagementActivity.this,
                                "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void confirmXoa(Lop lop) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa lớp?")
                .setMessage("Xóa lớp " + lop.getLop() + "?")
                .setPositiveButton("Xóa", (d, w) -> {
                    ApiClient.getApiService().deleteLop(lop.getLop())
                            .enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call,
                                                       Response<ResponseBody> response) {
                                    if (response.isSuccessful()) {
                                        Toast.makeText(ClassManagementActivity.this, "Đã xóa",
                                                Toast.LENGTH_SHORT).show();
                                        napDanhSach();
                                    } else {
                                        Toast.makeText(ClassManagementActivity.this,
                                                "Xóa thất bại", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                @Override
                                public void onFailure(Call<ResponseBody> call, Throwable t) {
                                    Toast.makeText(ClassManagementActivity.this,
                                            "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
