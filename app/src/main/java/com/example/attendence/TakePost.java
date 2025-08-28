package com.example.attendence;

import java.util.HashMap;

public class TakePost {
    private String studentId; // 학생
    private String id ;
    private String subject;
    private String professor;
    private String profId;
    private String classroom;
    private String schedule;
    private String attendenceStandard;
    // 사유 추가
    private String studentReasons;
    //학생에서 자신의 출결 상태를 볼 수 있도록함
    private String studentAttendenceStatus;
    //실시간 시간
    private String currentTime;
    public TakePost() {} // Firestore 직렬화용

    public TakePost(String subject, String professor, String classroom, String schedule,
                    String attendenceStandard, String id, String profId) {
        this.id = id;
        this.subject = subject;
        this.professor = professor;
        this.profId = profId;
        this.classroom = classroom;
        this.schedule = schedule;
        this.attendenceStandard = attendenceStandard;
        this.studentReasons = "";
        this.studentAttendenceStatus="아직없음";

    }
    public String getStudentId() {return studentId;}
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
    public String getId() {return id;}
    public void setId(String id) { this.id = id;}
    public String getSubject() {
        return subject;
    }

    public String getProfessor() {
        return professor;
    }

    public String getClassroom() {
        return classroom;
    }

    public String getSchedule() { return schedule;}

    public String getAttendenceStandard() { return attendenceStandard;}
    public void setAttendenceStandard(String attendenceStandard) {
        this.attendenceStandard = attendenceStandard;
    }
    public String getProfId() { return profId; }
    public void setProfessorId(String profId) { this.profId = profId; }
    public String getStudentReasons() { return studentReasons; }
    public void setStudentReasons(String studentReasons) { this.studentReasons = studentReasons; }


    public String getStudentAttendenceStatus() {
        return studentAttendenceStatus;
    }

    public void setStudentAttendenceStatus(String studentAttendenceStatus) {
        this.studentAttendenceStatus = studentAttendenceStatus;
    }

    public String getCurrentTime() { return currentTime; }
    public void setCurrentTime(String currentTime) { this.currentTime = currentTime; }
}