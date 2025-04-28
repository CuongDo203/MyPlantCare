package com.example.myplantcare.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private List<NoteSectionItem> noteSectionItemList;
    private Context context;
    private OnItemClickListener itemClickListener;

    private String userId;
    private String myPlantId;

    public NoteAdapter(Context context, List<NoteSectionItem> noteSectionItemList, String userId, String myPlantId) {
        this.context = context;
        this.noteSectionItemList = noteSectionItemList;
        this.userId = userId;
        this.myPlantId = myPlantId;
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
            HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
            headerViewHolder.tvSectionTitle.setText(sectionItem.getHeaderTitle());
        } else {
            NoteViewHolder noteViewHolder = (NoteViewHolder) holder;
            Note note = sectionItem.getNote();
            noteViewHolder.tvNoteTitle.setText(note.getTitle());
            noteViewHolder.tvNoteContent.setText(note.getContent());
            noteViewHolder.tvNoteDate.setText("Tạo ngày " + note.getDate());

            noteViewHolder.ivDeleteNote.setOnClickListener(v -> {
                showDeleteConfirmationDialog(note.getId(), position);
                Log.d("NoteAdapter", "Note deleted, position: " + position + ", noteId: " + note.getId());
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
        return noteSectionItemList.get(position).getType();
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
        this.noteSectionItemList.clear();
        this.noteSectionItemList.addAll(newNoteSectionItemList);
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(Note note);
    }

    private void showDeleteConfirmationDialog(String noteId, int position) {
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Xóa ghi chú")
                .setMessage("Bạn có chắc muốn xóa ghi chú này?")
                .setPositiveButton("Xóa", (dialogInterface, which) -> {
                    deleteNoteFromFirestore(noteId, position);
                    Log.d("NoteAdapter", "Note deleted, position: " + position + ", noteId: " + noteId);
                })
                .setNegativeButton("Hủy", null)
                .create(); // dùng create() thay vì show() ngay lập tức

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(context.getResources().getColor(R.color.green)); // đổi màu nút Xóa
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(context.getResources().getColor(R.color.green)); // đổi màu nút Hủy
        });
        dialog.show();
    }


    private void deleteNoteFromFirestore(String noteId, int position) {
        if (userId == null || noteId == null) return;

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("my_plants") // Giữ nguyên đường dẫn này
                .document(myPlantId != null ? myPlantId : "defaultPlantId") // Sử dụng giá trị mặc định nếu myPlantId null
                .collection("notes")
                .document(noteId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Xóa ghi chú ở vị trí hiện tại trong danh sách
                    noteSectionItemList.remove(position);

                    // Kiểm tra và xóa header nếu không còn ghi chú nào thuộc header đó
                    if (position - 1 >= 0 && noteSectionItemList.get(position - 1).isHeader()) {
                        if (position >= noteSectionItemList.size() || noteSectionItemList.get(position).isHeader()) {
                            noteSectionItemList.remove(position - 1);
                            notifyItemRemoved(position - 1);
                        }
                    }

                    // Cập nhật RecyclerView sau khi xóa
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Đã xóa ghi chú", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Lỗi xóa ghi chú: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


}
