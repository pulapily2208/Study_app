package com.example.study_app.ui.Notes.Adapters;

import android.content.Context;
import android.net.Uri;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.study_app.R;
import com.example.study_app.ui.Notes.Model.Note;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    private final List<Note> notes;
    private final Context context;
    private OnNoteClickListener onNoteClickListener;

    public interface OnNoteClickListener {
        void onNoteClick(Note note);
    }

    public void setOnNoteClickListener(OnNoteClickListener listener) {
        this.onNoteClickListener = listener;
    }

    public NotesAdapter(List<Note> notes, Context context) {
        this.notes = notes;
        this.context = context;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.notes_item, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notes.get(position);

        holder.tvTitle.setText(note.getTitle());
        holder.tvContent.setText(Html.fromHtml(note.getBody(), Html.FROM_HTML_MODE_LEGACY));

        try {
            long timestamp = Long.parseLong(note.getCreated_at());
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            holder.tvDate.setText(sdf.format(new Date(timestamp)));
        } catch (Exception e) {
            holder.tvDate.setText(note.getCreated_at());
        }

        if (note.getColor_tag() != null && !note.getColor_tag().isEmpty()) {
            try {
                holder.cardView.setCardBackgroundColor(android.graphics.Color.parseColor(note.getColor_tag()));
            } catch (IllegalArgumentException ignored) {}
        }

        // Hiển thị ảnh nếu có
        if (note.getImagePath() != null && !note.getImagePath().isEmpty()) {
            holder.imageAnh.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(Uri.parse(note.getImagePath()))
                    .into(holder.imageAnh);
        } else {
            holder.imageAnh.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (onNoteClickListener != null) {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    onNoteClickListener.onNoteClick(notes.get(pos));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent, tvDate;
        ImageView imageAnh;
        CardView cardView;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTieuDe);
            tvContent = itemView.findViewById(R.id.tvNoiDung);
            tvDate = itemView.findViewById(R.id.tvNgay);
            imageAnh = itemView.findViewById(R.id.imgAnh);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }
}
