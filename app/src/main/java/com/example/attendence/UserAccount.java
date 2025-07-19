package com.example.attendence;

public class UserAccount {
    private String userName;
    private String userId;
    private String pwd;

    // Firestore를 위한 기본 생성자
    public UserAccount() {}

    // 매개변수 생성자
    public UserAccount(String userName, String userId,String pwd) {
        this.userName = userName;
        this.userId = userId;
        this.pwd = pwd;
    }

    // Getter와 Setter
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId){
        this.userId = userId;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }
}