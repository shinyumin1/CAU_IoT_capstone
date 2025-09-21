package com.example.attendence;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TakePostAdapter extends RecyclerView.Adapter<TakePostAdapter.ViewHolder> {

    private Context context;
    private List<TakePost> takeList;
    private boolean isStudent;
    private boolean showButtons;
    private String currentPage;
    private String selectedDateId;
    private String userId;
    private int selectedPosition = RecyclerView.NO_POSITION;
    private OnProfessorStudentClickListener profStudentClickListener;
    private String bluetoothTime;
    private String bluetoothStatus;
    private String bluetoothWeek;
    private String bluetoothData;
    public void setBluetoothData (String data){
        if (data == null || data.isEmpty()) return;
        String[] parts = data.split("/");
        if(parts.length < 3) return;
        this.bluetoothWeek = parts[0];
        this.bluetoothTime = parts[1];
        this.bluetoothStatus = parts[2];
        Log.d("TakePostAdapter", "Bluetooth 데이터 수신: time=" + bluetoothTime + ", status=" + bluetoothStatus);
        for (TakePost post: takeList){
            post.setCurrentTime(bluetoothTime);
            post.setStudentAttendenceStatus(bluetoothStatus);
        }
        notifyDataSetChanged();
    }

    public void setOnProfessorStudentClickListener(OnProfessorStudentClickListener listener){
        this.profStudentClickListener = listener;
    }



    public interface OnSpinnerItemSelectedListener {
        void onItemSelected(TakePost post, String selectedStandard);
    }

    private OnSpinnerItemSelectedListener spinnerListener;

    public void setOnSpinnerItemSelectedListener(OnSpinnerItemSelectedListener listener) {
        this.spinnerListener = listener;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    public TakePostAdapter(Context context, List<TakePost> takeList, boolean isStudent,  boolean showButtons,String currentPage) {
        this.context = context;
        this.takeList = takeList;
        this.isStudent = isStudent;
        this.showButtons = showButtons;
        this.currentPage = currentPage;
    }

    public TakePostAdapter(Context context, List<TakePost> takeList, boolean isStudent, String currentPage) {
        this(context, takeList, isStudent, true, currentPage);
    }
    public  enum ProfessorViewType {
        BUTTON, SPINNER
    }
    private ProfessorViewType professorViewType = ProfessorViewType.BUTTON;

    public void setProfessorViewType(ProfessorViewType type) {
        this.professorViewType = type;
    }
    public interface OnstudentAppealClickListener {
        void onAppealClick(TakePost post);
    }
    private  OnstudentAppealClickListener appealListener;

    public void setOnStudentAppealClickListener(OnstudentAppealClickListener listener){
        this.appealListener = listener;
    }
    public interface OnprofesseorClickListener {
        void onProfClick (TakePost post);
    }
    private OnprofesseorClickListener professorClickListener;

    public void serOnProfessorClickListener(OnprofesseorClickListener listener){
        this.professorClickListener = listener;
    }
    public void updateStudentStatus(String date, String time, String status) {
        for (int i = 0; i < takeList.size(); i++) {
            TakePost post = takeList.get(i);
            if (post.getSchedule() !=null && post.getSchedule().contains(date)) { // 요일/시간 비교
                post.setCurrentTime(time);
                post.setStudentAttendenceStatus(status);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public interface OnProfessorStudentClickListener {
        void onClick(TakePost post);
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

        holder.itemView.setBackgroundResource(R.drawable.bg_item_default);

        if ("출석".equals(post.getStudentAttendenceStatus())) {
            holder.itemView.setVisibility(View.GONE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
            return;
        }

        if (position==selectedPosition){
            holder.itemView.setBackgroundResource(R.drawable.bg_item_selected);
        }
        //학생별 사유
        String studentId = post.getStudentId();
        String reason = post.getStudentReasons();

        if (studentId != null) {
            if (reason != null && !reason.isEmpty()) {
                holder.reasonText.setText("      " + studentId + " : " + reason);
            } else {
                holder.reasonText.setText("      " + studentId + " : 사유 없음");
            }
            holder.reasonText.setVisibility(View.VISIBLE);
        } else {
            holder.reasonText.setVisibility(View.GONE);
        }

        if (!showButtons) {
            holder.btnSelectSeat.setVisibility(View.GONE);
            holder.btnSeatStatus.setVisibility(View.GONE);
            holder.profAttendSpinner.setVisibility(View.GONE);
            holder.btnStudAttend.setVisibility(View.GONE);
            holder.standSpinner.setVisibility(View.GONE);
            holder.reasonText.setVisibility(View.GONE);

        } else if (isStudent) {
            holder.profAttendSpinner.setVisibility(View.GONE);
            holder.btnSeatStatus.setVisibility(View.GONE);
            holder.standSpinner.setVisibility(View.GONE);
            holder.reasonText.setVisibility(View.GONE);

            if ("HOME".equals(currentPage)) {
                holder.btnSelectSeat.setVisibility(View.VISIBLE);
                holder.btnStudAttend.setVisibility(View.GONE);
                holder.btnSelectSeat.setOnClickListener(v -> {
                    Intent intent = new Intent(context, SelectSeatActivity.class);
                    intent.putExtra("takeId", post.getId());
                    intent.putExtra("과목명", post.getSubject());
                    intent.putExtra("교수명", post.getProfessor());
                    intent.putExtra("강의실", post.getClassroom());
                    intent.putExtra("시간", post.getSchedule());
                    if (context instanceof Activity) {
                        ((Activity) context).startActivity(intent);
                    } else {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }
                });
            } else if("ATTEND".equals(currentPage)) {
                holder.btnSelectSeat.setVisibility(View.GONE);

                if(post.getStudentAttendenceStatus() != null && !post.getStudentAttendenceStatus().isEmpty()){
                    holder.btnStudAttend.setVisibility(View.VISIBLE);
                    if (post.getStudentAttendenceStatus() != null && !post.getStudentAttendenceStatus().isEmpty()) {
                        holder.btnStudAttend.setVisibility(View.VISIBLE);

                        if ("출결 완료".equals(post.getStudentAttendenceStatus())) {
                            // 이미 완료된 경우 → 그대로 상태 표시
                            holder.btnStudAttend.setText(post.getStudentAttendenceStatus());
                        } else if("지각".equals(post.getStudentAttendenceStatus())){
                            holder.btnStudAttend.setText(post.getStudentAttendenceStatus());
                        } else{
                            // 완료가 아닐 경우 → 블루투스에서 받은 데이터로 표시
                            holder.btnStudAttend.setText(bluetoothData);
                        }
                    }

                }
                else {
                    // Firestore에서 출결 상태 불러오기
                    if(userId != null && !userId.isEmpty()) {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        String dateId = (selectedDateId != null) ? selectedDateId
                                : new java.text.SimpleDateFormat("yyMMdd", java.util.Locale.KOREAN).format(new java.util.Date());

                        db.collection("users")
                                .document(userId)
                                .collection("takes")
                                .document(post.getId()) // 수강 과목 id
                                .collection("date")
                                .document(dateId)
                                .get()
                                .addOnSuccessListener(document -> {
                                    String status = "미기록";
                                    if(document.exists()) {
                                        String dbStatus = document.getString("status"); // status : 출석/결석/지각 => 출석 미인정은 추후 고려
                                        if(dbStatus != null && !dbStatus.isEmpty()) {
                                            status = dbStatus;
                                        }
                                    }
                                    post.setStudentAttendenceStatus(status);
                                    holder.btnStudAttend.setText(status + "\n(" + post.getCurrentTime() + ")");
                                    holder.btnStudAttend.setVisibility(View.VISIBLE);
                                })
                                .addOnFailureListener(e -> {
                                    holder.btnStudAttend.setText("불러오기 실패");
                                    holder.btnStudAttend.setVisibility(View.VISIBLE);
                                });

                    }
                }
                holder.btnStudAttend.setOnClickListener(v -> {
                    if (appealListener != null) {
                        appealListener.onAppealClick(post);  // Fragment에서 등록한 리스너 호출
                    }
                });
            }

        } else {
            //교수님
            /*
            *
            *
            * */
            holder.btnSelectSeat.setVisibility(View.GONE);
            holder.btnStudAttend.setVisibility(View.GONE);

            if ("HOME".equals(currentPage)) {
                holder.btnSeatStatus.setVisibility(View.VISIBLE);
                holder.profAttendSpinner.setVisibility(View.GONE);
                holder.standSpinner.setVisibility(View.GONE);
                holder.reasonText.setVisibility(View.GONE);
                holder.btnSeatStatus.setOnClickListener(v -> {
                    Intent intent = new Intent(context, SeatStatusActivity.class);
                    intent.putExtra("과목명", post.getSubject());
                    intent.putExtra("시간", post.getSchedule());
                    intent.putExtra("강의실", post.getClassroom());
                    intent.putExtra("professorId",userId);
                    intent.putExtra("lectureId",post.getId());
                    context.startActivity(intent);
                });
            } else if ("ATTEND".equals(currentPage)) {
                holder.btnSeatStatus.setVisibility(View.GONE);
                holder.standSpinner.setVisibility(View.GONE);

                holder.reasonText.setVisibility(position == selectedPosition ? View.VISIBLE : View.GONE);
                holder.itemView.setBackgroundResource(position == selectedPosition ?
                        R.drawable.bg_item_selected : R.drawable.bg_item_default);

                if (post.getStudentId() != null) {
                    holder.itemView.setOnClickListener(v -> {
                        int prevPosition = selectedPosition;
                        selectedPosition = holder.getAdapterPosition();
                        notifyItemChanged(prevPosition);
                        notifyItemChanged(selectedPosition);

                        if (profStudentClickListener != null) {
                            profStudentClickListener.onClick(post);
                        }

                        String takeId = post.getId();
                        String profId = post.getProfId();

                        if (selectedDateId != null && studentId != null && profId != null) {
                            FirebaseFirestore db = FirebaseFirestore.getInstance();

                            // 학생 DB 업데이트
                            db.collection("users")
                                    .document(studentId)
                                    .collection("takes")
                                    .document(takeId)
                                    .collection("date")
                                    .document(selectedDateId)
                                    .update("status", "출석")
                                    .addOnSuccessListener(aVoid -> {
                                        // UI 업데이트
                                        post.setStudentAttendenceStatus("출석");
                                        notifyItemChanged(selectedPosition);
                                        Toast.makeText(context, studentId + " 학생 출석 처리됨", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(context, "출석 처리 실패: " + studentId, Toast.LENGTH_SHORT).show();
                                        // 실패 시 UI 롤백
                                        post.setStudentAttendenceStatus(null);
                                        notifyItemChanged(selectedPosition);
                                    });

                            // 교수 DB 업데이트
                            db.collection("users")
                                    .document(profId)
                                    .collection("lecture")
                                    .document(takeId)
                                    .collection("date")
                                    .document(selectedDateId)
                                    .collection("attendance")
                                    .document(studentId)
                                    .update("status", "출석")
                                    .addOnFailureListener(e -> Log.w("TakePostAdapter", "교수 DB 출석 업데이트 실패", e));
                        }
                    });
                } else {
                    holder.itemView.setOnClickListener(null);
                }

//                    if (professorClickListener != null){
//                        professorClickListener.onProfClick(post);
//                    }
                /*
                holder.btnSeatStatus.setVisibility(View.GONE);
                //holder.profAttendSpinner.setVisibility(View.VISIBLE);
                holder.standSpinner.setVisibility(View.GONE);


                //spinner 세팅
                ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                        context,
                        R.array.attend_status_spinner,
                        R.layout.spinner_item_custom
                );
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                holder.profAttendSpinner.setAdapter(spinnerAdapter);
                //리스너제거
                holder.profAttendSpinner.setOnClickListener(null);*/

//                holder.itemView.setOnClickListener(v ->  {
//                    if(holder.reasonText.getVisibility()  ==  View.GONE) {
//                        holder.reasonText.setVisibility(View.VISIBLE);
//                    } else {
//                        holder.reasonText.setVisibility(View.GONE);
//                    }
//                });

                if (position == selectedPosition) {
                    holder.itemView.setBackgroundResource(R.drawable.bg_item_selected);
                } else {
                    holder.itemView.setBackgroundResource(R.drawable.bg_item_default);
                }
                /*
                //현재 DB값과 일치하는 위치 선택
                if (post.getAttendenceStandard() != null) {
                    int pos = spinnerAdapter.getPosition(post.getAttendenceStandard());
                    if (pos >= 0) holder.profAttendSpinner.setSelection(pos);
                }
                //리스너 다시 등록
                holder.profAttendSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String selected = parent.getItemAtPosition(position).toString();
                        if(spinnerListener != null){
                            spinnerListener.onItemSelected(post, selected);
                        }
                        //학생쪽으로 반영할 수있도록
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        String studentId = post.getStudentId();
                        String takeId = post.getId();
                        if(studentId !=null &&  takeId !=null){
                            db.collection("users")
                                    .document(studentId)
                                    .collection("takes")
                                    .document(takeId)
                                    .collection("date")
                                    .document(selectedDateId)
                                    .update("status", selected)
                                    .addOnSuccessListener(aVoid->{
                                        Log.d("교수님 출결현황","학생 출결 기준 업데이트 성공:" + selected);
                                    })
                                    .addOnFailureListener(e->{
                                        Log.w("교수님 출결 현황", "학생 출결 기준 업데이트 실패" ,e);
                                    });
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });

                * **/

            }else {
                holder.btnSeatStatus.setVisibility(View.GONE);
                holder.standSpinner.setVisibility(View.VISIBLE);
                //spinner 세팅
                ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                        context,
                        R.array.standard_spinner,
                        R.layout.spinner_item_custom
                );
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                holder.standSpinner.setAdapter(spinnerAdapter);
                // 리스너 제거
                holder.standSpinner.setOnItemSelectedListener(null);
                //현재 DB 값과 일치하는 위치 선택
                if (post.getAttendenceStandard() != null) {
                    int pos = spinnerAdapter.getPosition(post.getAttendenceStandard());
                    if (pos >= 0) holder.standSpinner.setSelection(pos);
                }

                // 리스너 다시 등록
                holder.standSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String selected = parent.getItemAtPosition(position).toString();
                        if (spinnerListener != null) {
                            spinnerListener.onItemSelected(post, selected);
                        }

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }
        }
    }
    @Override
    public int getItemCount() {
        return takeList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView subject, professor,classroom,schedule, reasonText;
        Button btnSelectSeat, btnSeatStatus, btnStudAttend;
        Spinner standSpinner, profAttendSpinner ;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            subject = itemView.findViewById(R.id.tv_subject);
            professor = itemView.findViewById(R.id.tv_professor);
            classroom = itemView.findViewById(R.id.tv_classroom);
            schedule = itemView.findViewById(R.id.tv_schedule);
            btnSelectSeat = itemView.findViewById(R.id.btn_select_seat);
            btnSeatStatus = itemView.findViewById(R.id.btn_seat_status);
            standSpinner = itemView.findViewById(R.id.stand_spinner);
            profAttendSpinner = itemView.findViewById(R.id.prof_attend_status_spinner);
            btnStudAttend = itemView.findViewById(R.id.stud_attendencd_status);
            reasonText  = itemView.findViewById(R.id.reason_text);

        }
    }
    public void setSelectedDate(String dateId) {
        this.selectedDateId = dateId;
    }
}