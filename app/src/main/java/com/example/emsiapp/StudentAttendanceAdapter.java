package com.example.emsiapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StudentAttendanceAdapter extends RecyclerView.Adapter<StudentAttendanceAdapter.StudentViewHolder> {

    private List<MarkAbsenceActivity.Student> studentList;

    public StudentAttendanceAdapter(List<MarkAbsenceActivity.Student> studentList) {
        this.studentList = studentList;
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        MarkAbsenceActivity.Student student = studentList.get(position);
        holder.studentNameTextView.setText(student.getName());
        holder.presentCheckBox.setChecked(student.isPresent());

        // Ajouter un écouteur pour mettre à jour l'état de présence lorsque la case est cochée
        holder.presentCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            student.setPresent(isChecked);
        });
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    static class StudentViewHolder extends RecyclerView.ViewHolder {
        TextView studentNameTextView;
        CheckBox presentCheckBox;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            studentNameTextView = itemView.findViewById(R.id.studentNameTextView);
            presentCheckBox = itemView.findViewById(R.id.presentCheckBox);
        }
    }
}