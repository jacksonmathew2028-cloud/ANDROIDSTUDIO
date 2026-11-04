package com.example.myneapplication;

public class Student {
    private final String id;
    private final String name;
    private boolean isPresent;
    private final int avatarResId;

    public Student(String id, String name, boolean isPresent, int avatarResId) {
        this.id = id;
        this.name = name;
        this.isPresent = isPresent;
        this.avatarResId = avatarResId;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isPresent() {
        return isPresent;
    }

    public void setPresent(boolean present) {
        isPresent = present;
    }

    public int getAvatarResId() {
        return avatarResId;
    }
}
