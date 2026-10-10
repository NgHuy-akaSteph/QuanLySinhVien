package com.example.qlsv;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.qlsv.adapter.TaiKhoanAdapter;
import com.example.qlsv.api.ApiClient;
import com.example.qlsv.model.TaiKhoan;
import com.example.qlsv.model.TaiKhoanCreateRequest;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountManagementActivity extends AppCompatActivity {

    private ListView lvTaiKhoan;
    private Button btnCapTaiKhoan;
    private TaiKhoanAdapter adapter;
    private final List<TaiKhoan> dsTaiKhoan = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_management);
        getSupportActionBar().setTitle("Quản lý Tài khoản");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        lvTaiKhoan = findViewById(R.id.lvTaiKhoan);
        btnCapTaiKhoan = findViewById(R.id.btnCapTaiKhoan);

        adapter = new TaiKhoanAdapter(this, dsTaiKhoan);
        lvTaiKhoan.setAdapter(adapter);

        btnCapTaiKhoan.setOnClickListener(v -> moDialogCap());

        lvTaiKhoan.setOnItemLongClickListener((p, view, pos, id) -> {
            confirmThuHoi(dsTaiKhoan.get(pos));
            return true;
        });

        napDanhSach();
    }

    private void napDanhSach() {
        ApiClient.getApiService().getDanhSachTaiKhoan()
                .enqueue(new Callback<List<TaiKhoan>>() {
                    @Override
                    public void onResponse(Call<List<TaiKhoan>> call,
                                           Response<List<TaiKhoan>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            dsTaiKhoan.clear();
                            dsTaiKhoan.addAll(response.body());
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(AccountManagementActivity.this,
                                    "Tải danh sách thất bại", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<List<TaiKhoan>> call, Throwable t) {
                        Toast.makeText(AccountManagementActivity.this,
                                "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void moDialogCap() {
        View view = getLayoutInflater().inflate(R.layout.dialog_account_create, null);
        EditText edtMaGV = view.findViewById(R.id.edtMaGVCap);
        EditText edtUserName = view.findViewById(R.id.edtUserNameCap);
        EditText edtPassWord = view.findViewById(R.id.edtPassWordCap);
        EditText edtVaiTro = view.findViewById(R.id.edtVaiTroCap);

        new AlertDialog.Builder(this)
                .setTitle("Cấp tài khoản")
                .setView(view)
                .setPositiveButton("Cấp", (d, w) -> {
                    String ma = edtMaGV.getText().toString().trim();
                    String user = edtUserName.getText().toString().trim();
                    String pass = edtPassWord.getText().toString().trim();
                    String vaiTro = edtVaiTro.getText().toString().trim();
                    if (ma.isEmpty() || user.isEmpty() || pass.isEmpty() || vaiTro.isEmpty()) {
                        Toast.makeText(this, "Nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    TaiKhoanCreateRequest req = new TaiKhoanCreateRequest(ma, user, pass, vaiTro);
                    ApiClient.getApiService().capTaiKhoan(req)
                            .enqueue(new Callback<TaiKhoan>() {
                                @Override
                                public void onResponse(Call<TaiKhoan> call,
                                                       Response<TaiKhoan> response) {
                                    if (response.isSuccessful()) {
                                        Toast.makeText(AccountManagementActivity.this,
                                                "Cấp tài khoản thành công",
                                                Toast.LENGTH_SHORT).show();
                                        napDanhSach();
                                    } else {
                                        Toast.makeText(AccountManagementActivity.this,
                                                "Cấp thất bại (mã " + response.code() + ")",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                                @Override
                                public void onFailure(Call<TaiKhoan> call, Throwable t) {
                                    Toast.makeText(AccountManagementActivity.this,
                                            "Lỗi: " + t.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void confirmThuHoi(TaiKhoan tk) {
        new AlertDialog.Builder(this)
                .setTitle("Thu hồi tài khoản?")
                .setMessage("Thu hồi tài khoản của " + tk.getMaGV() + "?")
                .setPositiveButton("Thu hồi", (d, w) -> {
                    ApiClient.getApiService().thuHoiTaiKhoan(tk.getMaGV())
                            .enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call,
                                                       Response<ResponseBody> response) {
                                    if (response.isSuccessful()) {
                                        Toast.makeText(AccountManagementActivity.this,
                                                "Đã thu hồi", Toast.LENGTH_SHORT).show();
                                        napDanhSach();
                                    } else {
                                        Toast.makeText(AccountManagementActivity.this,
                                                "Thu hồi thất bại", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                @Override
                                public void onFailure(Call<ResponseBody> call, Throwable t) {
                                    Toast.makeText(AccountManagementActivity.this,
                                            "Lỗi: " + t.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
