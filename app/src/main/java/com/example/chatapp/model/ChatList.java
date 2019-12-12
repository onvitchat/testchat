package com.example.chatapp.model;

import java.util.HashMap;
import java.util.Map;
//마지막채팅내용 및 각자 안읽은 메세지 개수 표시.
public class ChatList {
    private Map<String, Integer> users = new HashMap<>();
    private String chatName;
    private String lastChat;
    private Object timestamp;
    private String photo;

    public String getChatName() {
        return chatName;
    }

    public void setChatName(String chatName) {
        this.chatName = chatName;
    }

    public Map<String, Integer> getUsers() {
        return users;
    }

    public void setUsers(Map<String, Integer> users) {
        this.users = users;
    }

    public String getLastChat() {
        return lastChat;
    }

    public void setLastChat(String lastChat) {
        this.lastChat = lastChat;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }


    @Override
    public String toString() {
        return "ChatList{" +
                "users=" + users +
                ", chatName='" + chatName + '\'' +
                ", lastChat='" + lastChat + '\'' +
                ", timestamp=" + timestamp +
                ", photo='" + photo + '\'' +
                '}';
    }
}
