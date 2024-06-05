package com.example.qrcodes;

public class UserModelTeacher {

    private String emails;
    private String names;
    private String midtermStartDate;
    private String midtermEndDate;
    private String finalsStartDate;
    private String finalsEndDate;

    // Default constructor required for Firestore
    public UserModelTeacher() {
        // Empty constructor
    }

    // Constructor with parameters
    public UserModelTeacher(String emails, String names, String midtermStartDate, String midtermEndDate, String finalsStartDate, String finalsEndDate) {
        this.emails = emails;
        this.names = names;
        this.midtermStartDate = midtermStartDate;
        this.midtermEndDate = midtermEndDate;
        this.finalsStartDate = finalsStartDate;
        this.finalsEndDate = finalsEndDate;
    }

    // Getter and setter methods

    public String getEmails() {
        return emails;
    }

    public void setEmails(String emails) {
        this.emails = emails;
    }

    public String getNames() {
        return names;
    }

    public void setNames(String names) {
        this.names = names;
    }

    public String getMidtermStartDate() {
        return midtermStartDate;
    }

    public void setMidtermStartDate(String midtermStartDate) {
        this.midtermStartDate = midtermStartDate;
    }

    public String getMidtermEndDate() {
        return midtermEndDate;
    }

    public void setMidtermEndDate(String midtermEndDate) {
        this.midtermEndDate = midtermEndDate;
    }

    public String getFinalsStartDate() {
        return finalsStartDate;
    }

    public void setFinalsStartDate(String finalsStartDate) {
        this.finalsStartDate = finalsStartDate;
    }

    public String getFinalsEndDate() {
        return finalsEndDate;
    }

    public void setFinalsEndDate(String finalsEndDate) {
        this.finalsEndDate = finalsEndDate;
    }
}
