package com.example.myneapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.ViewHolder> {

    private final List<AttendanceRecord> fullList = new ArrayList<>();
    private final List<AttendanceRecord> displayedList = new ArrayList<>();

    public AttendanceAdapter() {}

    public void setRecords(List<AttendanceRecord> records) {
        fullList.clear();
        displayedList.clear();
        if (records != null) {
            fullList.addAll(records);
            displayedList.addAll(records);
        }
        notifyDataSetChanged();
    }

    public void filter(String query) {
        displayedList.clear();
        if (query == null || query.trim().isEmpty()) {
            displayedList.addAll(fullList);
        } else {
            String lower = query.toLowerCase(Locale.getDefault()).trim();
            for (AttendanceRecord item : fullList) {
                if (item.getStudentName().toLowerCase(Locale.getDefault()).contains(lower)
                        || item.getStudentId().toLowerCase(Locale.getDefault()).contains(lower)
                        || item.getSubjectName().toLowerCase(Locale.getDefault()).contains(lower)) {
                    displayedList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_attendance_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AttendanceRecord record = displayedList.get(position);

        String name = record.getStudentName();
        holder.tvStudentName.setText(name);

        String initial = (name != null && !name.trim().isEmpty())
                ? name.trim().substring(0, 1).toUpperCase(Locale.getDefault())
                : "?";
        holder.tvAvatarInitial.setText(initial);

        holder.tvStudentId.setText(record.getStudentId());
        holder.tvSubject.setText(record.getSubjectName());
        holder.tvTimestamp.setText(record.getFormattedDateTime());
    }

    @Override
    public int getItemCount() {
        return displayedList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvAvatarInitial;
        final TextView tvStudentName;
        final TextView tvStudentId;
        final TextView tvSubject;
        final TextView tvTimestamp;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatarInitial = itemView.findViewById(R.id.tvAvatarInitial);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvStudentId = itemView.findViewById(R.id.tvStudentId);
            tvSubject = itemView.findViewById(R.id.tvSubject);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }
    }
}
