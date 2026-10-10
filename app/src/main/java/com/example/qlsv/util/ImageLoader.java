package com.example.qlsv.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.widget.ImageView;

import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Tải ảnh từ HTTP URL về ImageView (không dùng thư viện ngoài).
 * - Có cache trong bộ nhớ (LruCache).
 * - Nén khi tải (inSampleSize) để tránh OOM.
 * - Bo góc 16dp.
 *
 * Cú pháp: ImageLoader.loadInto(imageView, "https://...")
 *          ImageLoader.loadRounded(imageView, "https://...", 16f)
 */
public final class ImageLoader {

    private static final LruCache<String, Bitmap> CACHE = new LruCache<>(24);
    private static final ExecutorService EXEC = Executors.newFixedThreadPool(3);
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    // Tag key để gắn URL lên ImageView -> tránh nhầm ảnh khi ListView tái sử dụng view.
    // Dùng int key bất kỳ (>= 0x01000000 để tránh đụng built-in tag của framework).
    private static final int TAG_URL = 0x7f010001;

    private ImageLoader() {}

    public static void loadInto(ImageView view, String url) {
        if (view == null) return;
        view.setTag(TAG_URL, url);
        if (url == null || url.isEmpty()) {
            view.setImageDrawable(null);
            return;
        }
        Bitmap cached = CACHE.get(url);
        if (cached != null) {
            view.setImageBitmap(cached);
            return;
        }
        final String finalUrl = url;
        EXEC.submit(() -> {
            Bitmap bmp = downloadBitmap(finalUrl);
            if (bmp != null) {
                CACHE.put(finalUrl, bmp);
            }
            MAIN.post(() -> {
                // Chỉ set nếu view vẫn đang dùng URL này
                Object tag = view.getTag(TAG_URL);
                if (tag != null && tag.equals(finalUrl)) {
                    if (bmp != null) {
                        view.setImageBitmap(bmp);
                    } else {
                        view.setImageDrawable(null);
                    }
                }
            });
        });
    }

    public static void loadRounded(ImageView view, String url, float cornerDp) {
        if (view == null) return;
        view.setTag(TAG_URL, url);
        if (url == null || url.isEmpty()) {
            view.setImageDrawable(null);
            return;
        }
        Bitmap cached = CACHE.get(url);
        if (cached != null) {
            applyRounded(view, cached, cornerDp);
            return;
        }
        final String finalUrl = url;
        final float finalCorner = cornerDp;
        EXEC.submit(() -> {
            Bitmap bmp = downloadBitmap(finalUrl);
            if (bmp != null) {
                CACHE.put(finalUrl, bmp);
            }
            MAIN.post(() -> {
                Object tag = view.getTag(TAG_URL);
                if (tag != null && tag.equals(finalUrl) && bmp != null) {
                    applyRounded(view, bmp, finalCorner);
                }
            });
        });
    }

    private static void applyRounded(ImageView view, Bitmap bmp, float cornerDp) {
        RoundedBitmapDrawable rbd = RoundedBitmapDrawableFactory.create(
                view.getResources(), bmp);
        rbd.setCornerRadius(dpToPx(view.getContext(), cornerDp));
        rbd.setAntiAlias(true);
        view.setImageDrawable(rbd);
    }

    private static float dpToPx(Context ctx, float dp) {
        return dp * ctx.getResources().getDisplayMetrics().density;
    }

    private static Bitmap downloadBitmap(String urlStr) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);
            conn.setRequestProperty("ngrok-skip-browser-warning", "true");
            conn.connect();
            if (conn.getResponseCode() != 200) return null;
            try (InputStream is = conn.getInputStream()) {
                // Giải mã với inSampleSize an toàn
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(is, null, opts);

                opts.inSampleSize = calculateInSampleSize(opts, 800, 800);
                opts.inJustDecodeBounds = false;
                try (InputStream is2 = conn.getInputStream()) {
                    return BitmapFactory.decodeStream(is2, null, opts);
                }
            }
        } catch (Exception e) {
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private static int calculateInSampleSize(BitmapFactory.Options opts, int reqW, int reqH) {
        int h = opts.outHeight;
        int w = opts.outWidth;
        int inSampleSize = 1;
        if (h > reqH || w > reqW) {
            int halfH = h / 2;
            int halfW = w / 2;
            while ((halfH / inSampleSize) >= reqH && (halfW / inSampleSize) >= reqW) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public static void clearCache() {
        CACHE.evictAll();
    }
}
