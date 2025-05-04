package com.example.myplantcare.adapters;

import android.content.Context;
import android.net.Uri;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myplantcare.R;
import com.example.myplantcare.models.DetailNote;

import java.util.List;

public class DetailNoteAdapter extends RecyclerView.Adapter<DetailNoteAdapter.NoteViewHolder> {

    public interface OnImageClickListener {
        void onImageClick(int position);
    }

    private final Context context;
    private final List<DetailNote> notes;
    private final OnImageClickListener imageClickListener;

    public DetailNoteAdapter(Context context,
                             List<DetailNote> notes,
                             OnImageClickListener listener) {
        this.context = context;
        this.notes = notes;
        this.imageClickListener = listener;
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        ImageView imageNote;
        TextView textAddImage;
        EditText editNoteContent;
        TextWatcher currentWatcher;

        public NoteViewHolder(View itemView) {
            super(itemView);
            imageNote       = itemView.findViewById(R.id.image_note);
            textAddImage    = itemView.findViewById(R.id.text_add_image);
            editNoteContent = itemView.findViewById(R.id.edit_note_content);
        }
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                             int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_detail_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final NoteViewHolder holder, int position) {
        final DetailNote note = notes.get(position);

        // 1. Hiển thị ảnh
        if (note.hasImage()) {
            holder.imageNote.setImageURI(Uri.parse(note.getImageUrl()));
            holder.textAddImage.setVisibility(View.GONE);
        } else {
            holder.imageNote.setImageResource(R.drawable.ic_add);
            holder.textAddImage.setVisibility(View.VISIBLE);
        }

        // 2. Gỡ TextWatcher cũ
        if (holder.currentWatcher != null) {
            holder.editNoteContent.removeTextChangedListener(holder.currentWatcher);
        }

        // 3. Đặt nội dung text
        String currentText = note.getText() != null ? note.getText() : "";
        if (!holder.editNoteContent.getText().toString().equals(currentText)) {
            holder.editNoteContent.setText(currentText);
        }

        // 4. Đính TextWatcher mới
        holder.currentWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override
            public void afterTextChanged(Editable s) {
                note.setText(s.toString());
            }
        };
        holder.editNoteContent.addTextChangedListener(holder.currentWatcher);

        // 5. Xử lý mất focus: nếu cả text + ảnh trống thì xoá item
        holder.editNoteContent.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                boolean emptyText = note.getText().trim().isEmpty();
                boolean noImage   = note.getImageUrl() == null || note.getImageUrl().isEmpty();
                if (emptyText && noImage) {
                    int pos = holder.getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        notes.remove(pos);
                        notifyItemRemoved(pos);
                    }
                }
            }
        });

        // 6. Xử lý click thêm ảnh
        holder.imageNote.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && imageClickListener != null) {
                imageClickListener.onImageClick(pos);
            }
        });

        // xử lý đọc ảnh
        if (!TextUtils.isEmpty(note.getImageUrl())) {
            Glide.with(context)
                    .load(note.getImageUrl())
                    .into(holder.imageNote);
            holder.textAddImage.setVisibility(View.GONE);
        } else {
            holder.imageNote.setImageResource(R.drawable.ic_add);
            holder.textAddImage.setVisibility(View.VISIBLE);
        }

    }


    @Override
    public int getItemCount() {
        return notes.size();
    }

    /** Thêm một item mới vào cuối danh sách */
    public void addNote(DetailNote note) {
        notes.add(note);
        notifyItemInserted(notes.size() - 1);
    }

    /** Cập nhật imageUrl tại vị trí */
    public void setImageUrlAt(int position, String imageUrl) {
        if (position >= 0 && position < notes.size()) {
            notes.get(position).setImageUrl(imageUrl);
            notifyItemChanged(position);
        }
    }

    /** Trả về danh sách hiện tại */
    public List<DetailNote> getNotes() {
        return notes;
    }
}
