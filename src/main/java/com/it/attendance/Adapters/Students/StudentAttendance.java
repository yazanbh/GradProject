package com.it.attendance.Adapters.Students;

public class StudentAttendance {
    private String day;
    private String month;
    private String year;
    private String email;
    private String IsPresent;
    private String name;

    // Getters and setters (omitted for brevity)


    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getEmail() {
        return email.substring(0,email.indexOf("@"));
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getIsPresent() {
        return IsPresent;
    }

    public void setIsPresent(String isPresent) {
        IsPresent = isPresent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}