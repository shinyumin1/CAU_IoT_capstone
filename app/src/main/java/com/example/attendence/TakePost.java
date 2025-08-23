package com.example.attendence;

public class TakePost {
    private String subject;
    private String professor;
    private String classroom;
    private String schedule;
    private String attendenceStandard;
    public TakePost() {} // Firestore 직렬화용

    public TakePost(String subject, String professor,String classroom,String schedule,String attendenceStandard) {
        this.subject = subject;
        this.professor = professor;
        this.classroom = classroom;
        this.schedule = schedule;
        this.attendenceStandard = attendenceStandard;
    }

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
}