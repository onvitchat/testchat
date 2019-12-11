package com.example.chatapp.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ChatModel {
    public Map<String, User> userInfo = new HashMap<>();
    public Map<String, Boolean> users = new HashMap<>(); // 채팅방 유저들
    public Map<String, Comment> comments = new HashMap<>(); //채팅방의 대화내용

    public static class Comment{
        public String uid;
        public String message;
        public Object timestamp;
        public String type;
        public Map<String, Object> readUsers = new HashMap<>();
        public Map<String, Object> existUser = new HashMap<>();

        @Override
        public String toString() {
            return "Comment{" +
                    "uid='" + uid + '\'' +
                    ", message='" + message + '\'' +
                    ", timestamp=" + timestamp +
                    ", type='" + type + '\'' +
                    ", readUsers=" + readUsers +
                    ", existUser=" + existUser +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Comment comment = (Comment) o;
            return timestamp.equals(comment.timestamp);
        }

        @Override
        public int hashCode() {
            return Objects.hash(timestamp);
        }
    }

    @Override
    public String toString() {
        return "ChatModel{" +
                "userInfo=" + userInfo +
                ", users=" + users +
                ", comments=" + comments +
                '}';
    }
}
