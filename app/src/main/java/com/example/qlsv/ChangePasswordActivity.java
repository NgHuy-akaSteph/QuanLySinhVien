package com.example.qlsv;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.qlsv.api.ApiClient;
import com.example.qlsv.model.ChangePasswordRequest;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText edtOld, edtNew, edtConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        getSupportActionBar().setTitle("Đổi mật khẩu");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        edtOld = findViewById(R.id.edtOldPassword);
        edtNew = findViewById(R.id.edtNewPassword);
        edtConfirm = findViewById(R.id.edtConfirmPassword);
        Button btnDoi = findViewById(R.id.btnDoiMatKhau);

        btnDoi.setOnClickListener(v -> doiMatKhau());
    }

    private void doiMatKhau() {
        String oldP = edtOld.getText().toString();
        String newP = edtNew.getText().toString();
        String cfP = edtConfirm.getText().toString();

        if (TextUtils.isEmpty(oldP) || TextUtils.isEmpty(newP) || TextUtils.isEmpty(cfP)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!newP.equals(cfP)) {
            Toast.makeText(this, "Mật khẩu mới và xác nhận không khớp", Toast.LENGTH_SHORT).show();
            return;
        }
        if (newP.length() < 3) {
            Toast.makeText(this, "Mật khẩu mới quá ngắn", Toast.LENGTH_SHORT).show();
            return;
        }

        ChangePasswordRequest req = new ChangePasswordRequest(oldP, newP, cfP);
        ApiClient.getApiService().doiMatKhau(req).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ChangePasswordActivity.this,
                            "Đổi mật khẩu thành công. Vui lòng đăng nhập lại.",
                            Toast.LENGTH_LONG).show();
                    // Xóa session -> quay về Login
                    if (ApiClient.getSessionManager() != null) {
                        ApiClient.getSessionManager().clear();
                    }
                    finish();
                } else {
                    String msg = response.code() == 401
                            ? "Mật khẩu cũ không đúng"
                            : "Đổi thất bại (mã " + response.code() + ")";
                    Toast.makeText(ChangePasswordActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(ChangePasswordActivity.this,
                        "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
