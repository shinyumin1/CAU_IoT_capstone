package com.example.attendence;

public class TakePost {
    private String subject;
    private String professor;
    private String classroom;
    private String schedule;
    public TakePost() {} // Firestore 직렬화용

    public TakePost(String subject, String professor,String classroom,String schedule) {
        this.subject = subject;
        this.professor = professor;
        this.classroom = classroom;
        this.schedule = schedule;
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
}