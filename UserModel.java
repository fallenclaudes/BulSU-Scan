package com.example.qrcodes;

public class UserModel {

    private String name, number, email;


    public UserModel(String emails, String names, String number) {
        this.name = names;
        this.email = emails;
        this.number = number;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

