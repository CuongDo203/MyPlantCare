//package com.example.myplantcare.adapters;
//
//import android.annotation.SuppressLint;
//import android.app.AlertDialog;
//import android.content.Context;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.myplantcare.R;
//import com.example.myplantcare.models.DetailNote;
//import com.example.myplantcare.models.Note;
//import com.example.myplantcare.models.NoteSectionItem;
//import com.google.firebase.firestore.FirebaseFirestore;
//
//import java.util.List;
//
//public class NoteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
//
//    private static final int TYPE_HEADER = 0;
//    private static final int TYPE_ITEM = 1;
//
//    private List<NoteSectionItem> noteSectionItemList;
//    private Context context;
//    private OnItemClickListener itemClickListener;
//
//    private String userId;
//    private String myPlantId;
//
//    public NoteAdapter(Context context, List<NoteSectionItem> noteSectionItemList, String userId, String myPlantId) {
//        this.context = context;
//        this.noteSectionItemList = noteSectionItemList;
//        this.userId = userId;
//        this.myPlantId = myPlantId;
//    }
//
//    public void setOnItemClickListener(OnItemClickListener listener) {
//        this.itemClickListener = listener;
//    }
//
//    @NonNull
//    @Override
//    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        if (viewType == TYPE_HEADER) {
//            View view = LayoutInflater.from(context).inflate(R.layout.item_section_header, parent, false);
//            return new HeaderViewHolder(view);
//        } else {
//            View view = LayoutInflater.from(context).inflate(R.layout.item_note, parent, false);
//            return new NoteViewHolder(view);
//        }
//    }
//
//    @SuppressLint("SetTextI18n")
//    @Override
//    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
//        NoteSectionItem sectionItem = noteSectionItemList.get(position);
//
//        if (sectionItem.isHeader()) {
//            HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
//            headerViewHolder.tvSectionTitle.setText(sectionItem.getHeaderTitle());
//        } else {
//            NoteViewHolder noteViewHolder = (NoteViewHolder) holder;
//            Note note = sectionItem.getNote();
//
//            // 1. Tiêu đề
//            noteViewHolder.tvNoteTitle.setText(note.getTitle());
//
//            // 2. Lấy summary = dòng đầu tiên của content (List<DetailNote>)
//            List<DetailNote> items = note.getContent();
//            String summary = "";
//            if (items != null && !items.isEmpty()) {
//                summary = items.get(0).getText();
//            }
//            noteViewHolder.tvNoteContent.setText(summary);
//
//            // 3. Ngày đã format sẵn trong model
//            noteViewHolder.tvNoteDate.setText("Tạo ngày " + note.getFormattedDate());
//
//            // 4. Xử lý xóa
//            noteViewHolder.ivDeleteNote.setOnClickListener(v -> {
//                showDeleteConfirmationDialog(note.getId(), position);
//                Log.d("NoteAdapter", "Note deleted, position: " + position + ", noteId: " + note.getId());
//            });
//
//            // 5. Click mở Detail
//            noteViewHolder.itemView.setOnClickListener(v -> {
//                if (itemClickListener != null) {
//                    itemClickListener.onItemClick(note);
//                }
//            });
//        }
//    }
//
//
//    @Override
//    public int getItemCount() {
//        return noteSectionItemList.size();
//    }
//
//    @Override
//    public int getItemViewType(int position) {
//        return noteSectionItemList.get(position).getType();
//    }
//
//    public static class NoteViewHolder extends RecyclerView.ViewHolder {
//        TextView tvNoteTitle, tvNoteContent, tvNoteDate;
//        ImageView ivDeleteNote;
//        View itemView;
//
//        public NoteViewHolder(@NonNull View itemView) {
//            super(itemView);
//            this.itemView = itemView;
//            tvNoteTitle = itemView.findViewById(R.id.tvNoteTitle);
//            tvNoteContent = itemView.findViewById(R.id.tvNoteContent);
//            tvNoteDate = itemView.findViewById(R.id.tvNoteDate);
//            ivDeleteNote = itemView.findViewById(R.id.ivDeleteNote);
//        }
//    }
//
//    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
//        TextView tvSectionTitle;
//
//        public HeaderViewHolder(@NonNull View itemView) {
//            super(itemView);
//            tvSectionTitle = itemView.findViewById(R.id.tvSectionTitle);
//        }
//    }
//
//    @SuppressLint("NotifyDataSetChanged")
//    public void setNotes(List<NoteSectionItem> newNoteSectionItemList) {
//        this.noteSectionItemList.clear();
//        this.noteSectionItemList.addAll(newNoteSectionItemList);
//        notifyDataSetChanged();
//    }
//
//    public interface OnItemClickListener {
//        void onItemClick(Note note);
//    }
//
//    private void showDeleteConfirmationDialog(String noteId, int position) {
//        AlertDialog dialog = new AlertDialog.Builder(context)
//                .setTitle("Xóa ghi chú")
//                .setMessage("Bạn có chắc muốn xóa ghi chú này?")
//                .setPositiveButton("Xóa", (dialogInterface, which) -> {
//                    deleteNoteFromFirestore(noteId, position);
//                    Log.d("NoteAdapter", "Note deleted, position: " + position + ", noteId: " + noteId);
//                })
//                .setNegativeButton("Hủy", null)
//                .create(); // dùng create() thay vì show() ngay lập tức
//
//        dialog.setOnShowListener(d -> {
//            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(context.getResources().getColor(R.color.green)); // đổi màu nút Xóa
//            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(context.getResources().getColor(R.color.green)); // đổi màu nút Hủy
//        });
//        dialog.show();
//    }
//
//
//    private void deleteNoteFromFirestore(String noteId, int position) {
//        if (userId == null || noteId == null) return;
//
//        FirebaseFirestore.getInstance()
//                .collection("users")
//                .document(userId)
//                .collection("my_plants") // Giữ nguyên đường dẫn này
//                .document(myPlantId != null ? myPlantId : "defaultPlantId") // Sử dụng giá trị mặc định nếu myPlantId null
//                .collection("notes")
//                .document(noteId)
//                .delete()
//                .addOnSuccessListener(aVoid -> {
//
//                    Log.d("deleteNote", "Note deleted successfully: " + noteId);
//                    // Xóa ghi chú ở vị trí hiện tại trong danh sách
//                    noteSectionItemList.remove(position);
//
//                    // Kiểm tra và xóa header nếu không còn ghi chú nào thuộc header đó
//                    if (position - 1 >= 0 && noteSectionItemList.get(position - 1).isHeader()) {
//                        if (position >= noteSectionItemList.size() || noteSectionItemList.get(position).isHeader()) {
//                            noteSectionItemList.remove(position - 1);
//                            notifyItemRemoved(position - 1);
//                        }
//                    }
//
//                    // Cập nhật RecyclerView sau khi xóa
//                    notifyItemRemoved(position);
//                    Toast.makeText(context, "Đã xóa ghi chú", Toast.LENGTH_SHORT).show();
//                })
//                .addOnFailureListener(e -> {
//                    Toast.makeText(context, "Lỗi xóa ghi chú: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                });
//    }
//}

package com.example.myplantcare.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.example.myplantcare.models.DetailNote;
import com.example.myplantcare.models.Note;
import com.example.myplantcare.models.NoteSectionItem;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NoteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM   = 1;

    private final List<NoteSectionItem> noteSectionItemList;
    private final Context context;
    private OnItemClickListener itemClickListener;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final String userId;

    public NoteAdapter(Context context,
                       List<NoteSectionItem> noteSectionItemList,
                       String userId) {
        this.context = context;
        this.noteSectionItemList = noteSectionItemList;
        this.userId = userId;      // gán ở đây
    }

    public interface OnItemClickListener {
        void onItemClick(Note note);
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return noteSectionItemList.get(position).getType();
    }

    @Override
    public int getItemCount() {
        return noteSectionItemList.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == TYPE_HEADER) {
            View v = inflater.inflate(R.layout.item_section_header, parent, false);
            return new HeaderViewHolder(v);
        } else {
            View v = inflater.inflate(R.layout.item_note, parent, false);
            return new NoteViewHolder(v);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(
            @NonNull RecyclerView.ViewHolder holder, int position) {

        NoteSectionItem sectionItem = noteSectionItemList.get(position);
        if (sectionItem.isHeader()) {
            HeaderViewHolder hvh = (HeaderViewHolder) holder;
            hvh.tvSectionTitle.setText(sectionItem.getHeaderTitle());
        } else {
            NoteViewHolder vh = (NoteViewHolder) holder;
            Note note = sectionItem.getNote();

            // 1. Title
            vh.tvNoteTitle.setText(note.getTitle());

            // 2. Summary = dòng đầu tiên của content
            List<DetailNote> items = note.getContent();
            String summary = "";
            if (items != null && !items.isEmpty()) {
                summary = items.get(0).getText();
            }
            vh.tvNoteContent.setText(summary);

            // 3. Date đã format
            vh.tvNoteDate.setText("Cập nhật lần cuối " + note.getFormattedDate());

            // 4. Delete
            vh.ivDeleteNote.setOnClickListener(v -> {
                showDeleteConfirmationDialog(note, position);
            });

            // 5. Click mở Detail
            vh.itemView.setOnClickListener(v -> {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(note);
                }
            });
        }
    }

    private void showDeleteConfirmationDialog(Note note, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Xóa ghi chú")
                .setMessage("Bạn có chắc chắn muốn xóa ghi chú này không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    deleteNoteFromFirestore(note, position);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteNoteFromFirestore(Note note, int position) {
        String userId      = this.userId;      // bạn phải set userId vào Note khi load
        String plantId     = note.getMyPlantId();
        String noteId      = note.getId();

        if (userId == null || plantId == null || noteId == null) {
            Toast.makeText(context, "Không thể xóa ghi chú.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(userId)
                .collection("my_plants").document(plantId)
                .collection("notes").document(noteId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Xóa item và header nếu cần
                    noteSectionItemList.remove(position);
                    // nếu trước đó là header và sau cùng cũng header, remove header
                    if (position > 0
                            && noteSectionItemList.get(position - 1).isHeader()
                            && (position >= noteSectionItemList.size()
                            || noteSectionItemList.get(position).isHeader())) {
                        noteSectionItemList.remove(position - 1);
                        notifyItemRemoved(position - 1);
                    }
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Đã xóa ghi chú", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context,
                            "Lỗi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setNotes(List<NoteSectionItem> newList) {
        noteSectionItemList.clear();
        noteSectionItemList.addAll(newList);
        notifyDataSetChanged();
    }

    // ViewHolders
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        final TextView tvSectionTitle;
        HeaderViewHolder(View v) {
            super(v);
            tvSectionTitle = v.findViewById(R.id.tvSectionTitle);
        }
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        final TextView tvNoteTitle, tvNoteContent, tvNoteDate;
        final ImageView ivDeleteNote;
        final View itemView;
        NoteViewHolder(View v) {
            super(v);
            itemView       = v;
            tvNoteTitle    = v.findViewById(R.id.tvNoteTitle);
            tvNoteContent  = v.findViewById(R.id.tvNoteContent);
            tvNoteDate     = v.findViewById(R.id.tvNoteDate);
            ivDeleteNote   = v.findViewById(R.id.ivDeleteNote);
        }
    }
}
