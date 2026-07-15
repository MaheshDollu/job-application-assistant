package com.mahesh.jobassist.common;

public class TestDemo {
    private static final String API_KEY = "sk-live-abc123secretkey456";

    public String getUserName(User user) {
        return user.getName().toUpperCase();
    }
}
