package com.example.myplantcare.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
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
import com.example.myplantcare.models.NoteSectionItem;

import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private List<NoteSectionItem> noteSectionItemList;
    private Context context;
    private OnItemClickListener itemClickListener;

    public NoteAdapter(Context context, List<NoteSectionItem> noteSectionItemList) {
        this.context = context;
        this.noteSectionItemList = noteSectionItemList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_section_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_note, parent, false);
            return new NoteViewHolder(view);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        NoteSectionItem sectionItem = noteSectionItemList.get(position);

        if (sectionItem.isHeader()) {
            assert holder instanceof HeaderViewHolder;
            HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
            if (headerViewHolder.tvSectionTitle == null) {
                Log.e("NoteAdapter", "tvSectionTitle is null!!");
            } else {
                headerViewHolder.tvSectionTitle.setText(sectionItem.getHeaderTitle());
            }
        } else {
            assert holder instanceof NoteViewHolder;
            NoteViewHolder noteViewHolder = (NoteViewHolder) holder;
            Note note = sectionItem.getNote();
            noteViewHolder.tvNoteTitle.setText(note.getTitle());
            noteViewHolder.tvNoteContent.setText(note.getContent());
            noteViewHolder.tvNoteDate.setText("Tạo ngày " + note.getDate());

            noteViewHolder.ivDeleteNote.setOnClickListener(v -> {
                noteSectionItemList.remove(position);
                notifyItemRemoved(position);
                Toast.makeText(context, "Đã xóa ghi chú", Toast.LENGTH_SHORT).show();
            });

            noteViewHolder.itemView.setOnClickListener(v -> {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(note);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return noteSectionItemList.size();
    }

    @Override
    public int getItemViewType(int position) {
        int type = noteSectionItemList.get(position).getType();
        Log.d("NoteAdapter", "getItemViewType: position=" + position + ", type=" + type);
        return type;
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView tvNoteTitle, tvNoteContent, tvNoteDate;
        ImageView ivDeleteNote;
        View itemView;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            tvNoteTitle = itemView.findViewById(R.id.tvNoteTitle);
            tvNoteContent = itemView.findViewById(R.id.tvNoteContent);
            tvNoteDate = itemView.findViewById(R.id.tvNoteDate);
            ivDeleteNote = itemView.findViewById(R.id.ivDeleteNote);
        }
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvSectionTitle;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSectionTitle = itemView.findViewById(R.id.tvSectionTitle);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setNotes(List<NoteSectionItem> newNoteSectionItemList) {
        this.noteSectionItemList = newNoteSectionItemList;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(Note note);
    }
}
