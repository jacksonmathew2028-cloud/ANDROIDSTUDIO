package com.example.myneapplication;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class AttendanceRecord {
    private final String id;
    private final String studentName;
    private final String studentId;
    private final String subjectName;
    private final long timestamp;
    private final String codeUsed;

    public AttendanceRecord(String studentName, String studentId, String subjectName, String codeUsed) {
        this.id = UUID.randomUUID().toString();
        this.studentName = studentName;
        this.studentId = studentId;
        this.subjectName = subjectName;
        this.timestamp = System.currentTimeMillis();
        this.codeUsed = codeUsed;
    }

    public AttendanceRecord(String id, String studentName, String studentId, String subjectName, long timestamp, String codeUsed) {
        this.id = id;
        this.studentName = studentName;
        this.studentId = studentId;
        this.subjectName = subjectName;
        this.timestamp = timestamp;
        this.codeUsed = codeUsed;
    }

    public String getId() {
        return id;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getCodeUsed() {
        return codeUsed;
    }

    public String getFormattedDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy • hh:mm:ss a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public JSONObject toJson() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("id", id);
            obj.put("studentName", studentName);
            obj.put("studentId", studentId);
            obj.put("subjectName", subjectName);
            obj.put("timestamp", timestamp);
            obj.put("codeUsed", codeUsed);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }

    public static AttendanceRecord fromJson(JSONObject obj) {
        if (obj == null) return null;
        String id = obj.optString("id", UUID.randomUUID().toString());
        String name = obj.optString("studentName", "");
        String studentId = obj.optString("studentId", "");
        String subject = obj.optString("subjectName", "");
        long timestamp = obj.optLong("timestamp", System.currentTimeMillis());
        String code = obj.optString("codeUsed", "");
        return new AttendanceRecord(id, name, studentId, subject, timestamp, code);
    }
}
