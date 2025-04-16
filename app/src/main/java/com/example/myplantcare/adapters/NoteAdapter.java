package com.example.myplantcare.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myplantcare.R;
import com.example.myplantcare.models.Note;

import java.util.ArrayList;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Note note);
    }

    private List<Note> noteList;
    private Context context;
    private OnItemClickListener itemClickListener; // Thêm listener

    public NoteAdapter(Context context, List<Note> noteList) {
        this.context = context;
        this.noteList = noteList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) { // Phương thức setter
        this.itemClickListener = listener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = noteList.get(position);
        holder.tvNoteTitle.setText(note.getTitle());
        holder.tvNoteContent.setText(note.getContent());
        holder.tvNoteDate.setText("Tạo ngày " + note.getDate());

        // Sự kiện xóa ghi chú
        holder.ivDeleteNote.setOnClickListener(v -> {
            noteList.remove(position);
            notifyItemRemoved(position);
            Toast.makeText(context, "Đã xóa ghi chú", Toast.LENGTH_SHORT).show();
        });

        // Sự kiện click vào item
        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(note);
            }
        });
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }


    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView tvNoteTitle, tvNoteContent, tvNoteDate;
        ImageView ivDeleteNote;
        View itemView; // Thêm biến này

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView; // Lưu trữ itemView
            tvNoteTitle = itemView.findViewById(R.id.tvNoteTitle);
            tvNoteContent = itemView.findViewById(R.id.tvNoteContent);
            tvNoteDate = itemView.findViewById(R.id.tvNoteDate);
            ivDeleteNote = itemView.findViewById(R.id.ivDeleteNote);
        }
    }

    // Trong NoteAdapter.java
    public void setNotes(List<Note> newNoteList) {
        this.noteList = newNoteList;
        notifyDataSetChanged();
    }
}