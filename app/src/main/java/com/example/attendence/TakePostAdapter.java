package com.example.attendence;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TakePostAdapter extends RecyclerView.Adapter<TakePostAdapter.ViewHolder> {

    private Context context;
    private List<TakePost> takeList;
    private boolean isStudent;
    private boolean showButtons;


    public TakePostAdapter(Context context, List<TakePost> takeList, boolean isStudent,  boolean showButtons) {
        this.context = context;
        this.takeList = takeList;
        this.isStudent = isStudent;
        this.showButtons = showButtons;
    }

    public TakePostAdapter(Context context, List<TakePost> takeList, boolean isStudent) {
        this(context, takeList, isStudent, true);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_take_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TakePost post = takeList.get(position);
        holder.subject.setText(post.getSubject());
        holder.professor.setText(post.getProfessor());
        holder.classroom.setText(post.getClassroom());
        holder.schedule.setText(post.getSchedule());

        if (!showButtons) {
            holder.btnSelectSeat.setVisibility(View.GONE);
            holder.btnSeatStatus.setVisibility(View.GONE);
            holder.statusSpinner.setVisibility(View.GONE);
        } else if (isStudent) {
            holder.btnSelectSeat.setVisibility(View.VISIBLE);
            holder.btnSeatStatus.setVisibility(View.GONE);
            holder.statusSpinner.setVisibility(View.GONE);
            holder.btnSelectSeat.setOnClickListener(v -> {
                Intent intent = new Intent(context, SelectSeatActivity.class);
                intent.putExtra("과목명", post.getSubject());
                intent.putExtra("교수명", post.getProfessor());
                intent.putExtra("강의실", post.getClassroom());
                intent.putExtra("시간", post.getSchedule());
                context.startActivity(intent);
            });
        } else {
            holder.btnSelectSeat.setVisibility(View.GONE);
            holder.btnSeatStatus.setVisibility(View.VISIBLE);
            holder.statusSpinner.setVisibility(View.VISIBLE);
            holder.btnSeatStatus.setOnClickListener(v -> {
                Intent intent = new Intent(context, SeatStatusActivity.class);
                intent.putExtra("과목명", post.getSubject());
                intent.putExtra("시간", post.getSchedule());
                intent.putExtra("강의실", post.getClassroom());
                context.startActivity(intent);
            });
            //spinner 세팅
            ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                    context,
                    R.array.standard_spinner,
                    android.R.layout.simple_spinner_item
            );
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            holder.statusSpinner.setAdapter(spinnerAdapter);
            //spinner 리스너 세팅

        }
    }

    @Override
    public int getItemCount() {
        return takeList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView subject, professor,classroom,schedule;
        Button btnSelectSeat, btnSeatStatus;
        Spinner statusSpinner;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            subject = itemView.findViewById(R.id.tv_subject);
            professor = itemView.findViewById(R.id.tv_professor);
            classroom = itemView.findViewById(R.id.tv_classroom);
            schedule = itemView.findViewById(R.id.tv_schedule);
            btnSelectSeat = itemView.findViewById(R.id.btn_select_seat);
            btnSeatStatus = itemView.findViewById(R.id.btn_seat_status);
            statusSpinner = itemView.findViewById(R.id.stand_spinner);
        }
    }
}