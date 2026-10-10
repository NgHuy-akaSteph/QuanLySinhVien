package com.example.qlsv.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.qlsv.R;
import com.example.qlsv.model.GiaoVien;

import java.util.List;

public class GiaoVienAdapter extends BaseAdapter {
    private final List<GiaoVien> data;
    private final LayoutInflater inflater;

    public GiaoVienAdapter(Context ctx, List<GiaoVien> data) {
        this.data = data;
        this.inflater = LayoutInflater.from(ctx);
    }

    @Override
    public int getCount() { return data != null ? data.size() : 0; }

    @Override
    public GiaoVien getItem(int position) { return data.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    private static class Holder {
        TextView tvLine1, tvLine2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder h;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_sinhvien, parent, false);
            h = new Holder();
            h.tvLine1 = convertView.findViewById(R.id.tvDong1);
            h.tvLine2 = convertView.findViewById(R.id.tvDong2);
            convertView.setTag(h);
        } else {
            h = (Holder) convertView.getTag();
        }
        GiaoVien gv = data.get(position);
        h.tvLine1.setText(String.format("%s - %s",
                gv.getMaGV() != null ? gv.getMaGV() : "",
                gv.getHoTen() != null ? gv.getHoTen() : ""));
        h.tvLine2.setText(String.format("Học hàm: %s | Email: %s",
                gv.getHocHam() != null ? gv.getHocHam() : "",
                gv.getEmail() != null ? gv.getEmail() : ""));
        return convertView;
    }
}
