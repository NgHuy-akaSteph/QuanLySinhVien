package com.example.qlsv.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.example.qlsv.api.ApiClient;
import com.example.qlsv.api.ApiService;
import com.example.qlsv.model.UploadResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Tiện ích upload ảnh avatar lên server.
 * - uploadAvatarSv: POST /api/SinhVien/{maSV}/upload-avatar
 * - uploadAvatarGv: POST /api/GiaoVien/{maGV}/upload-avatar
 *
 * Luồng: Uri -> InputStream -> Bitmap -> nén JPEG (chất lượng 80) -> MultipartBody.Part
 * Callback trả về URL ảnh (string) hoặc null nếu lỗi.
 */
public final class ImageUploader {

    private static final int JPEG_QUALITY = 80;
    private static final String FIELD_NAME = "file";

    private ImageUploader() {}

    public interface UploadCallback {
        void onSuccess(String imageUrl);
        void onFailure(String error);
    }

    public static void uploadAvatarSv(String maSV, Uri imageUri, Context ctx, UploadCallback cb) {
        MultipartBody.Part part = buildImagePart(ctx, imageUri);
        if (part == null) {
            cb.onFailure("Không đọc được ảnh");
            return;
        }
        ApiClient.getApiService().uploadAvatarSv(maSV, part).enqueue(new Callback<UploadResponse>() {
            @Override
            public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String url = response.body().getImageUrl();
                    if (url != null && !url.isEmpty()) {
                        cb.onSuccess(url);
                    } else {
                        cb.onFailure("Server không trả URL ảnh");
                    }
                } else {
                    cb.onFailure("Upload thất bại (code " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<UploadResponse> call, Throwable t) {
                cb.onFailure("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public static void uploadAvatarGv(String maGV, Uri imageUri, Context ctx, UploadCallback cb) {
        MultipartBody.Part part = buildImagePart(ctx, imageUri);
        if (part == null) {
            cb.onFailure("Không đọc được ảnh");
            return;
        }
        ApiClient.getApiService().uploadAvatarGv(maGV, part).enqueue(new Callback<UploadResponse>() {
            @Override
            public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String url = response.body().getImageUrl();
                    if (url != null && !url.isEmpty()) {
                        cb.onSuccess(url);
                    } else {
                        cb.onFailure("Server không trả URL ảnh");
                    }
                } else {
                    cb.onFailure("Upload thất bại (code " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<UploadResponse> call, Throwable t) {
                cb.onFailure("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private static MultipartBody.Part buildImagePart(Context ctx, Uri imageUri) {
        try (InputStream is = ctx.getContentResolver().openInputStream(imageUri)) {
            if (is == null) return null;
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            if (bitmap == null) return null;

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, baos);
            byte[] bytes = baos.toByteArray();
            bitmap.recycle();

            RequestBody body = RequestBody.create(MediaType.parse("image/jpeg"), bytes);
            String fileName = "avatar_" + System.currentTimeMillis() + ".jpg";
            return MultipartBody.Part.createFormData(FIELD_NAME, fileName, body);
        } catch (IOException e) {
            return null;
        }
    }
}
