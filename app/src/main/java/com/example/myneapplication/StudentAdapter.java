package com.example.myneapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    private final List<Student> fullList;
    private final List<Student> displayedList;

    public StudentAdapter(List<Student> students) {
        this.fullList = new ArrayList<>(students);
        this.displayedList = new ArrayList<>(students);
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        Student student = displayedList.get(position);
        holder.tvName.setText(student.getName());
        holder.ivAvatar.setImageResource(student.getAvatarResId());

        Context context = holder.itemView.getContext();
        if (student.isPresent()) {
            holder.tvStatusChip.setText(R.string.present);
            holder.tvStatusChip.setTextColor(ContextCompat.getColor(context, R.color.present_text));
            holder.tvStatusChip.setBackgroundResource(R.drawable.bg_chip_present);
        } else {
            holder.tvStatusChip.setText(R.string.absent);
            holder.tvStatusChip.setTextColor(ContextCompat.getColor(context, R.color.absent_text));
            holder.tvStatusChip.setBackgroundResource(R.drawable.bg_chip_absent);
        }

        View.OnClickListener toggleStatus = v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                student.setPresent(!student.isPresent());
                notifyItemChanged(pos);
            }
        };

        holder.tvStatusChip.setOnClickListener(toggleStatus);
        holder.itemView.setOnClickListener(toggleStatus);
    }

    @Override
    public int getItemCount() {
        return displayedList.size();
    }

    public void filter(String query) {
        displayedList.clear();
        if (query == null || query.trim().isEmpty()) {
            displayedList.addAll(fullList);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            for (Student student : fullList) {
                if (student.getName().toLowerCase().contains(lowerQuery)) {
                    displayedList.add(student);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void markAllPresent() {
        for (Student student : fullList) {
            student.setPresent(true);
        }
        notifyDataSetChanged();
    }

    public static class StudentViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivAvatar;
        final TextView tvName;
        final TextView tvStatusChip;

        StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvName = itemView.findViewById(R.id.tv_student_name);
            tvStatusChip = itemView.findViewById(R.id.tv_status_chip);
        }
    }
}
