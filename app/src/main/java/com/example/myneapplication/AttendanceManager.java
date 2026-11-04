package com.example.myneapplication;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class AttendanceManager {
    private static final String PREFS_NAME = "attendance_prefs";
    private static final String KEY_RECORDS = "key_attendance_records";
    public static final int CODE_DURATION_SECONDS = 30;

    private static AttendanceManager instance;
    private final SharedPreferences preferences;
    private final Random random = new Random();

    private String currentSubject = "Mobile Application Development";
    private String currentCode = "";
    private String previousCode = "";
    private long codeExpiryTimeMillis = 0;
    private long previousCodeExpiryTimeMillis = 0;
    private boolean sessionActive = true;

    private final List<AttendanceRecord> liveSessionRecords = new ArrayList<>();
    private final List<OnDataChangeListener> listeners = new ArrayList<>();

    public interface OnDataChangeListener {
        void onDataChanged();
    }

    public static class CheckInResult {
        private final boolean success;
        private final String message;
        private final AttendanceRecord record;

        public CheckInResult(boolean success, String message, AttendanceRecord record) {
            this.success = success;
            this.message = message;
            this.record = record;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public AttendanceRecord getRecord() {
            return record;
        }
    }

    private AttendanceManager(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        generateNewCode();
    }

    public static synchronized AttendanceManager getInstance(Context context) {
        if (instance == null) {
            instance = new AttendanceManager(context);
        }
        return instance;
    }

    public synchronized void generateNewCode() {
        if (!currentCode.isEmpty()) {
            previousCode = currentCode;
            // 5 second grace window after expiration
            previousCodeExpiryTimeMillis = System.currentTimeMillis() + 5000;
        }

        int codeNumber = 100000 + random.nextInt(900000);
        currentCode = String.valueOf(codeNumber);
        codeExpiryTimeMillis = System.currentTimeMillis() + (CODE_DURATION_SECONDS * 1000L);
        notifyListeners();
    }

    public String getCurrentSubject() {
        return currentSubject;
    }

    public void setCurrentSubject(String currentSubject) {
        if (currentSubject != null && !currentSubject.trim().isEmpty()) {
            this.currentSubject = currentSubject.trim();
            notifyListeners();
        }
    }

    public String getCurrentCode() {
        return currentCode;
    }

    public String getFormattedCode() {
        if (currentCode.length() == 6) {
            return currentCode.substring(0, 3) + " " + currentCode.substring(3);
        }
        return currentCode;
    }

    public int getRemainingSeconds() {
        long diff = codeExpiryTimeMillis - System.currentTimeMillis();
        if (diff <= 0) return 0;
        return (int) Math.ceil(diff / 1000.0);
    }

    public boolean isSessionActive() {
        return sessionActive;
    }

    public void setSessionActive(boolean sessionActive) {
        this.sessionActive = sessionActive;
        notifyListeners();
    }

    public synchronized CheckInResult markAttendance(String name, String studentId, String enteredCode) {
        if (!sessionActive) {
            return new CheckInResult(false, "Session is currently inactive. Ask teacher to start session.", null);
        }

        if (name == null || name.trim().isEmpty() || studentId == null || studentId.trim().isEmpty()) {
            return new CheckInResult(false, "Please provide your Name and Student ID.", null);
        }

        String cleanName = name.trim();
        String cleanId = studentId.trim().toUpperCase(Locale.ROOT);
        String cleanCode = enteredCode != null ? enteredCode.replaceAll("\\s+", "").trim() : "";

        if (cleanCode.length() != 6) {
            return new CheckInResult(false, "Please enter the 6-digit attendance code.", null);
        }

        long now = System.currentTimeMillis();
        boolean matchesCurrent = cleanCode.equals(currentCode) && now <= codeExpiryTimeMillis;
        boolean matchesPrevious = cleanCode.equals(previousCode) && now <= previousCodeExpiryTimeMillis;

        if (!matchesCurrent && !matchesPrevious) {
            return new CheckInResult(false, "Invalid or expired code! Please check the host's screen for the live code.", null);
        }

        // Check if student already marked attendance in this live session
        for (AttendanceRecord record : liveSessionRecords) {
            if (record.getStudentId().equalsIgnoreCase(cleanId)) {
                return new CheckInResult(false, "You have already marked attendance for this session (" + record.getFormattedTime() + ").", null);
            }
        }

        // Success - create record
        AttendanceRecord record = new AttendanceRecord(cleanName, cleanId, currentSubject, cleanCode);
        liveSessionRecords.add(0, record);
        saveRecordToHistory(record);
        notifyListeners();

        return new CheckInResult(true, "Attendance marked successfully!", record);
    }

    public synchronized List<AttendanceRecord> getLiveSessionRecords() {
        return new ArrayList<>(liveSessionRecords);
    }

    public synchronized List<AttendanceRecord> getAllRecords() {
        List<AttendanceRecord> list = new ArrayList<>();
        String jsonString = preferences.getString(KEY_RECORDS, "[]");
        try {
            JSONArray array = new JSONArray(jsonString);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                AttendanceRecord r = AttendanceRecord.fromJson(obj);
                if (r != null) {
                    list.add(r);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private synchronized void saveRecordToHistory(AttendanceRecord record) {
        try {
            String jsonString = preferences.getString(KEY_RECORDS, "[]");
            JSONArray array = new JSONArray(jsonString);
            array.put(record.toJson());
            preferences.edit().putString(KEY_RECORDS, array.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void clearHistory() {
        preferences.edit().remove(KEY_RECORDS).apply();
        liveSessionRecords.clear();
        notifyListeners();
    }

    public synchronized void resetLiveSession() {
        liveSessionRecords.clear();
        generateNewCode();
        notifyListeners();
    }

    public String generateExportText() {
        List<AttendanceRecord> records = getLiveSessionRecords();
        if (records.isEmpty()) {
            records = getAllRecords();
        }

        StringBuilder sb = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        sb.append("=== ATTENDANCE REPORT ===\n");
        sb.append("Subject: ").append(currentSubject).append("\n");
        sb.append("Exported at: ").append(sdf.format(new Date())).append("\n");
        sb.append("Total Present: ").append(records.size()).append("\n");
        sb.append("------------------------------------\n");
        int count = 1;
        for (AttendanceRecord r : records) {
            sb.append(count++).append(". ")
                    .append(r.getStudentName())
                    .append(" (ID: ").append(r.getStudentId()).append(")")
                    .append(" - ").append(r.getFormattedTime())
                    .append("\n");
        }
        sb.append("====================================\n");
        return sb.toString();
    }

    public void addListener(OnDataChangeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(OnDataChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (OnDataChangeListener listener : new ArrayList<>(listeners)) {
            listener.onDataChanged();
        }
    }
}
