//package com.example.myplantcare.adapters;
//import android.content.Context;
//import android.net.Uri;
//import android.text.Editable;
//import android.text.TextWatcher;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.EditText;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//import com.example.myplantcare.models.DetailNote;
//import com.example.myplantcare.R;
//
//import java.util.List;
//
//public class DetailNoteAdapter extends RecyclerView.Adapter<DetailNoteAdapter.NoteViewHolder> {
//
//    public interface OnImageClickListener {
//        void onImageClick(int position);
//    }
//
//    private Context context;
//    private List<DetailNote> notes;
//    private OnImageClickListener imageClickListener;
//
//    public DetailNoteAdapter(Context context, List<DetailNote> notes, OnImageClickListener listener) {
//        this.context = context;
//        this.notes = notes;
//        this.imageClickListener = listener;
//    }
//
//    public static class NoteViewHolder extends RecyclerView.ViewHolder {
//        ImageView imageNote;
//        TextView textAddImage;
//        EditText editNoteContent;
//
//        public NoteViewHolder(View itemView) {
//            super(itemView);
//            imageNote = itemView.findViewById(R.id.image_note);
//            textAddImage = itemView.findViewById(R.id.text_add_image);
//            editNoteContent = itemView.findViewById(R.id.edit_note_content);
//        }
//    }
//
//    @NonNull
//    @Override
//    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(context).inflate(R.layout.item_detail_note, parent, false);
//        return new NoteViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull final NoteViewHolder holder, int position) {
//        final DetailNote note = notes.get(position);
//
//        if (note.getImageUri() != null) {
//            holder.imageNote.setImageURI(note.getImageUri());
//            holder.textAddImage.setVisibility(View.GONE);
//        } else {
//            holder.imageNote.setImageResource(R.drawable.ic_add); // icon thêm ảnh
//            holder.textAddImage.setVisibility(View.VISIBLE);
//        }
//
//        holder.imageNote.setOnClickListener(v -> {
//            if (imageClickListener != null) {
//                imageClickListener.onImageClick(position);
//            }
//        });
//
//        holder.editNoteContent.setText(note.getNoteText());
//
//        holder.editNoteContent.addTextChangedListener(new TextWatcher() {
//            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                note.setNoteText(s.toString());
//            }
//        });
//    }
//
//
//    @Override
//    public int getItemCount() {
//        return notes.size();
//    }
//
//    // Thêm item mới
//    public void addNote(DetailNote note) {
//        notes.add(note);
//        notifyItemInserted(notes.size() - 1);
//    }
//
//    // Cập nhật ảnh cho item ở vị trí cụ thể
//    public void setImageUriAt(int position, Uri imageUri) {
//        if (position >= 0 && position < notes.size()) {
//            notes.get(position).setImageUri(imageUri);
//            notifyItemChanged(position);
//        }
//    }
//
//    // Lấy danh sách ghi chú
//    public List<DetailNote> getNotes() {
//        return notes;
//    }
//
//}

//package com.example.myplantcare.adapters;
//
//import android.content.Context;
//import android.net.Uri;
//import android.text.Editable;
//import android.text.TextWatcher;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.EditText;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//import com.example.myplantcare.models.DetailNote;
//import com.example.myplantcare.R;
//
//import java.util.List;
//
//public class DetailNoteAdapter extends RecyclerView.Adapter<DetailNoteAdapter.NoteViewHolder> {
//
//    public interface OnImageClickListener {
//        void onImageClick(int position);
//    }
//
//    private Context context;
//    private List<DetailNote> notes;
//    private OnImageClickListener imageClickListener;
//
//    public DetailNoteAdapter(Context context, List<DetailNote> notes, OnImageClickListener listener) {
//        this.context = context;
//        this.notes = notes;
//        this.imageClickListener = listener;
//    }
//
//    public static class NoteViewHolder extends RecyclerView.ViewHolder {
//        ImageView imageNote;
//        TextView textAddImage;
//        EditText editNoteContent;
//        TextWatcher currentWatcher;
//
//        public NoteViewHolder(View itemView) {
//            super(itemView);
//            imageNote = itemView.findViewById(R.id.image_note);
//            textAddImage = itemView.findViewById(R.id.text_add_image);
//            editNoteContent = itemView.findViewById(R.id.edit_note_content);
//        }
//    }
//
//    @NonNull
//    @Override
//    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(context).inflate(R.layout.item_detail_note, parent, false);
//        return new NoteViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull final NoteViewHolder holder, int position) {
//        final DetailNote note = notes.get(position);
//
//        // Reset ảnh cũ nếu có
//        if (note.getImageUri() != null) {
//            holder.imageNote.setImageURI(note.getImageUri());
//            holder.textAddImage.setVisibility(View.GONE);
//        } else {
//            holder.imageNote.setImageDrawable(null); // Xóa ảnh cũ
//            holder.imageNote.setImageResource(R.drawable.ic_add);
//            holder.textAddImage.setVisibility(View.VISIBLE);
//        }
//
//        // Set click ảnh
//        holder.imageNote.setOnClickListener(v -> {
//            if (imageClickListener != null) {
//                imageClickListener.onImageClick(holder.getAdapterPosition());
//            }
//        });
//
//        // Gỡ bỏ TextWatcher cũ (nếu có)
//        if (holder.currentWatcher != null) {
//            holder.editNoteContent.removeTextChangedListener(holder.currentWatcher);
//        }
//
//        // Set nội dung và gắn TextWatcher mới
//        if (!holder.editNoteContent.getText().toString().equals(note.getNoteText())) {
//            holder.editNoteContent.setText(note.getNoteText());
//        }
//
//        holder.currentWatcher = new TextWatcher() {
//            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                note.setNoteText(s.toString());
//            }
//        };
//
//        holder.editNoteContent.addTextChangedListener(holder.currentWatcher);
//    }
//
//    @Override
//    public int getItemCount() {
//        return notes.size();
//    }
//
//    public void addNote(DetailNote note) {
//        notes.add(note);
//        notifyItemInserted(notes.size() - 1);
//    }
//
//    public void setImageUriAt(int position, Uri imageUri) {
//        if (position >= 0 && position < notes.size()) {
//            notes.get(position).setImageUri(imageUri);
//            notifyItemChanged(position);
//        }
//    }
//
//    public List<DetailNote> getNotes() {
//        return notes;
//    }
//}
// DO NOT DELETE

package com.example.myplantcare.adapters;

import android.content.Context;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
    public void onBindViewHolder(@NonNull final NoteViewHolder holder,
                                 int position) {
        final DetailNote note = notes.get(position);

        // 1. Hiển thị ảnh nếu có
        if (note.hasImage()) {
            // load từ imageUrl (String) thành Uri
            Uri uri = Uri.parse(note.getImageUrl());
            holder.imageNote.setImageURI(uri);
            holder.textAddImage.setVisibility(View.GONE);
        } else {
            holder.imageNote.setImageResource(R.drawable.ic_add);
            holder.textAddImage.setVisibility(View.VISIBLE);
        }

        // 2. Click ảnh
        holder.imageNote.setOnClickListener(v -> {
            if (imageClickListener != null) {
                imageClickListener.onImageClick(holder.getAdapterPosition());
            }
        });

        // 3. Xoá TextWatcher cũ trước khi đặt nội dung mới
        if (holder.currentWatcher != null) {
            holder.editNoteContent.removeTextChangedListener(holder.currentWatcher);
        }

        // 4. Đặt text từ note.getText()
        String currentText = note.getText() != null ? note.getText() : "";
        if (!holder.editNoteContent.getText().toString().equals(currentText)) {
            holder.editNoteContent.setText(currentText);
        }

        // 5. Tạo và gắn TextWatcher mới để cập nhật vào note.setText(...)
        holder.currentWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override
            public void afterTextChanged(Editable s) {
                note.setText(s.toString());
            }
        };
        holder.editNoteContent.addTextChangedListener(holder.currentWatcher);
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
