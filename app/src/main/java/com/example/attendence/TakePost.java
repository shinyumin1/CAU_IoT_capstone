package com.example.attendence;

public class TakePost {
    private String id ;
    private String subject;
    private String professor;
    private String profId;
    private String classroom;
    private String schedule;
    private String attendenceStandard;
    private String reason;
    //화면에 따라 버튼이 달라지도록
    public TakePost() {} // Firestore 직렬화용

    public TakePost(String subject, String professor,String classroom,String schedule,String attendenceStandard,String id,String ProfId) {
        this.id = id;
        this.subject = subject;
        this.professor = professor;
        this.profId = profId;
        this.classroom = classroom;
        this.schedule = schedule;
        this.attendenceStandard = attendenceStandard;
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
    public  String getReason() {return reason;}
    public void setReason(String reason) {this.reason = reason;}
    public String getProfId() { return profId; }
    public void setProfessorId(String profId) { this.profId = profId; }
}