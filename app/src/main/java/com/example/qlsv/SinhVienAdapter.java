package com.example.qlsv;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Custom BaseAdapter cho ListView, dùng layout item_sinhvien.xml.
 * Mỗi dòng chỉ hiển thị:
 *   Dòng 1 (in đậm): [Mã SV] - [Họ tên]
 *   Dòng 2: Lớp: [Tên lớp]
 */
public class SinhVienAdapter extends BaseAdapter {

    private List<SinhVien> danhSach;
    private final LayoutInflater inflater;

    public SinhVienAdapter(Context context, List<SinhVien> danhSach) {
        this.danhSach = danhSach;
        this.inflater = LayoutInflater.from(context);
    }

    public void capNhatDuLieu(List<SinhVien> danhSachMoi) {
        this.danhSach = danhSachMoi;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return danhSach.size();
    }

    @Override
    public SinhVien getItem(int position) {
        return danhSach.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private static class ViewHolder {
        TextView tvDong1;
        TextView tvDong2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_sinhvien, parent, false);
            holder = new ViewHolder();
            holder.tvDong1 = convertView.findViewById(R.id.tvDong1);
            holder.tvDong2 = convertView.findViewById(R.id.tvDong2);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        SinhVien sv = danhSach.get(position);
        holder.tvDong1.setText(String.format("%s - %s", sv.getMaSV(), sv.getHoTen()));
        holder.tvDong2.setText(String.format("Lớp: %s", sv.getLop()));

        return convertView;
    }
}
