package com.example.study_app.ui.Subject.Adapter;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.study_app.R;
import com.example.study_app.ui.Subject.Model.Subject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class SubjectBulkImportAdapter extends RecyclerView.Adapter<SubjectBulkImportAdapter.ViewHolder> {

    private final ArrayList<Subject> subjectList;

    public SubjectBulkImportAdapter(ArrayList<Subject> subjectList) {
        this.subjectList = subjectList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.subject_item_bulk_import, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Subject subject = subjectList.get(position);
        holder.bind(subject, position);
    }

    @Override
    public int getItemCount() {
        return subjectList.size();
    }

    public void addRow() {
        subjectList.add(new Subject());
        notifyItemInserted(subjectList.size() - 1);
    }

    public ArrayList<Subject> getSubjectList() {
        return subjectList;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvIndex;
        EditText etMaHP, etTenGV, etPhongHoc;
        TextView tvNgayBatDau, tvGioBatDau, tvGioKetThuc;
        CardView viewMauSac;
        View innerViewMauSac;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIndex = itemView.findViewById(R.id.tv_index);
            etMaHP = itemView.findViewById(R.id.et_ma_hp);
            etTenGV = itemView.findViewById(R.id.et_ten_gv);
            tvNgayBatDau = itemView.findViewById(R.id.tv_ngay_bat_dau);
            tvGioBatDau = itemView.findViewById(R.id.tv_gio_bat_dau);
            tvGioKetThuc = itemView.findViewById(R.id.tv_gio_ket_thuc);
            etPhongHoc = itemView.findViewById(R.id.et_phong_hoc);
            viewMauSac = itemView.findViewById(R.id.view_mau_sac);
            innerViewMauSac = itemView.findViewById(R.id.inner_view_mau_sac);
        }

        void bind(final Subject subject, final int position) {
            tvIndex.setText(String.valueOf(position + 1));
            etMaHP.setText(subject.maHp);
            etTenGV.setText(subject.tenGv);
            etPhongHoc.setText(subject.phongHoc);

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            if (subject.ngayBatDau != null) {
                tvNgayBatDau.setText(dateFormat.format(subject.ngayBatDau));
            }

            if (subject.mauSac != null) {
                try {
                    innerViewMauSac.setBackgroundColor(Color.parseColor(subject.mauSac));
                } catch (IllegalArgumentException e) {
                    innerViewMauSac.setBackgroundColor(Color.GRAY);
                }
            }

            java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("HH:mm",
                    java.util.Locale.getDefault());
            if (subject.gioBatDau != null) {
                tvGioBatDau.setText(timeFormat.format(subject.gioBatDau));
            } else {
                tvGioBatDau.setText("");
            }
            if (subject.gioKetThuc != null) {
                tvGioKetThuc.setText(timeFormat.format(subject.gioKetThuc));
            } else {
                tvGioKetThuc.setText("");
            }

            etMaHP.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    subject.maHp = s.toString();
                }
            });

            etTenGV.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    subject.tenGv = s.toString();
                }
            });

            etPhongHoc.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    subject.phongHoc = s.toString();
                }
            });

            tvNgayBatDau.setOnClickListener(v -> showDatePickerDialog(v.getContext(), subject, tvNgayBatDau));
            tvGioBatDau.setOnClickListener(v -> showTimePickerDialog(v.getContext(), subject, true, tvGioBatDau));
            tvGioKetThuc.setOnClickListener(v -> showTimePickerDialog(v.getContext(), subject, false, tvGioKetThuc));
            viewMauSac.setOnClickListener(v -> showColorPickerDialog(v.getContext(), subject, innerViewMauSac));
        }

        private void showDatePickerDialog(Context context, final Subject subject, final TextView tvNgayBatDau) {
            Calendar calendar = Calendar.getInstance();
            if (subject.ngayBatDau != null) {
                calendar.setTime(subject.ngayBatDau);
            }
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(context,
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        Calendar newDate = Calendar.getInstance();
                        newDate.set(year1, monthOfYear, dayOfMonth);
                        subject.ngayBatDau = newDate.getTime();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        tvNgayBatDau.setText(dateFormat.format(subject.ngayBatDau));
                    }, year, month, day);
            datePickerDialog.show();
        }

        private void showColorPickerDialog(Context context, final Subject subject, final View colorView) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            LayoutInflater inflater = LayoutInflater.from(context);
            View dialogView = inflater.inflate(R.layout.dialog_color_picker, null);
            builder.setView(dialogView);
            builder.setTitle("Chọn màu");

            final AlertDialog dialog = builder.create();
            final GridLayout colorGrid = dialogView.findViewById(R.id.color_grid);

            String[] colors;
            try {
                colors = context.getResources().getStringArray(R.array.subject_colors);
            } catch (Exception e) {
                Toast.makeText(context, "Lỗi tải danh sách màu", Toast.LENGTH_SHORT).show();
                colors = new String[] { "#CCCCCC" };
            }

            for (String colorHex : colors) {
                ImageView colorSwatch = new ImageView(context);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = 100;
                params.height = 100;
                params.setMargins(16, 16, 16, 16);
                colorSwatch.setLayoutParams(params);

                GradientDrawable background = new GradientDrawable();
                background.setShape(GradientDrawable.OVAL);
                background.setColor(Color.parseColor(colorHex));
                background.setStroke(4, Color.TRANSPARENT);

                if (colorHex.equalsIgnoreCase(subject.mauSac)) {
                    background.setStroke(10, Color.BLACK);
                }

                colorSwatch.setBackground(background);
                colorSwatch.setTag(colorHex);

                colorSwatch.setOnClickListener(v -> {
                    subject.mauSac = colorHex;
                    colorView.setBackgroundColor(Color.parseColor(colorHex));
                    dialog.dismiss();
                });

                colorGrid.addView(colorSwatch);
            }

            dialog.show();
        }

        private void showTimePickerDialog(Context context, final Subject subject, final boolean isStart,
                final TextView target) {
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            int hour = calendar.get(java.util.Calendar.HOUR_OF_DAY);
            int minute = calendar.get(java.util.Calendar.MINUTE);

            android.app.TimePickerDialog dialog = new android.app.TimePickerDialog(context, (view, h, m) -> {
                String value = String.format(java.util.Locale.getDefault(), "%02d:%02d", h, m);
                target.setText(value);
                try {
                    java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("HH:mm",
                            java.util.Locale.getDefault());
                    java.util.Date d = fmt.parse(value);
                    if (isStart)
                        subject.gioBatDau = d;
                    else
                        subject.gioKetThuc = d;
                } catch (java.text.ParseException ignored) {
                }
            }, hour, minute, true);
            dialog.show();
        }
    }
}
