package com.example.chatactivity;

public class Message {
    public String message;
    public String user;

    public Message(){}

    public Message(String texto, String usuario){
        this.user = usuario;
        this.message = texto;
    }
}
