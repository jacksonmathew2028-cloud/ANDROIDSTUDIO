package com.example.myneapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private AttendanceManager manager;
    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;

    // Header & Bottom Navigation
    private TextView tvHeaderTitle;
    private BottomNavigationView bottomNav;

    // Content Tabs
    private View layoutStudents;
    private View layoutLiveCode;
    private View layoutCheckIn;
    private View layoutRecords;

    // Tab 1: Students Roster
    private StudentAdapter studentAdapter;
    private RecyclerView rvStudents;
    private EditText etSearch;
    private MaterialButton btnMarkAllPresent;

    // Tab 2: Live Rotating Code Generator (Host)
    private SwitchMaterial switchSessionActive;
    private TextInputEditText etSubjectName;
    private TextView tvLivePill;
    private TextView tvLiveCode;
    private LinearProgressIndicator progressCodeTimer;
    private TextView tvCodeTimerCountdown;
    private MaterialButton btnRegenerateCode;
    private TextView tvLiveAttendanceCount;
    private MaterialButton btnExportLive;
    private MaterialButton btnResetLiveSession;
    private TextView tvLiveEmptyNotice;
    private RecyclerView rvLiveAttendees;
    private AttendanceAdapter liveAttendeesAdapter;

    // Tab 3: Student Smartphone Check-In Portal
    private TextView tvStudentActiveSubject;
    private TextInputEditText etInputStudentName;
    private TextInputEditText etInputStudentId;
    private TextInputEditText etInputCode;
    private MaterialButton btnSubmitAttendance;
    private MaterialCardView cardCheckInFeedback;
    private LinearLayout layoutFeedbackContainer;
    private TextView tvFeedbackTitle;
    private TextView tvFeedbackDetails;

    // Tab 4: Records History
    private TextInputEditText etSearchRecords;
    private TextView tvTotalRecordsCount;
    private MaterialButton btnExportAllRecords;
    private MaterialButton btnClearAllRecords;
    private TextView tvRecordsEmptyNotice;
    private RecyclerView rvAllRecords;
    private AttendanceAdapter allRecordsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        manager = AttendanceManager.getInstance(this);

        initViews();
        setupWindowInsets();
        setupNavigation();
        setupStudentsTab();
        setupLiveCodeTab();
        setupCheckInTab();
        setupRecordsTab();
        setupManagerListener();
        setupCodeTimer();

        // Default to Students tab
        bottomNav.setSelectedItemId(R.id.nav_students);
    }

    private void initViews() {
        tvHeaderTitle = findViewById(R.id.tv_header_title);
        bottomNav = findViewById(R.id.bottom_navigation);

        layoutStudents = findViewById(R.id.layout_students);
        layoutLiveCode = findViewById(R.id.layout_live_code);
        layoutCheckIn = findViewById(R.id.layout_check_in);
        layoutRecords = findViewById(R.id.layout_records);

        // Tab 1: Students
        rvStudents = findViewById(R.id.rv_students);
        etSearch = findViewById(R.id.et_search);
        btnMarkAllPresent = findViewById(R.id.btn_mark_all_present);

        // Tab 2: Live Code
        switchSessionActive = findViewById(R.id.switch_session_active);
        etSubjectName = findViewById(R.id.et_subject_name);
        tvLivePill = findViewById(R.id.tv_live_pill);
        tvLiveCode = findViewById(R.id.tv_live_code);
        progressCodeTimer = findViewById(R.id.progress_code_timer);
        tvCodeTimerCountdown = findViewById(R.id.tv_code_timer_countdown);
        btnRegenerateCode = findViewById(R.id.btn_regenerate_code);
        tvLiveAttendanceCount = findViewById(R.id.tv_live_attendance_count);
        btnExportLive = findViewById(R.id.btn_export_live);
        btnResetLiveSession = findViewById(R.id.btn_reset_live_session);
        tvLiveEmptyNotice = findViewById(R.id.tv_live_empty_notice);
        rvLiveAttendees = findViewById(R.id.rv_live_attendees);

        // Tab 3: Check-In
        tvStudentActiveSubject = findViewById(R.id.tv_student_active_subject);
        etInputStudentName = findViewById(R.id.et_input_student_name);
        etInputStudentId = findViewById(R.id.et_input_student_id);
        etInputCode = findViewById(R.id.et_input_code);
        btnSubmitAttendance = findViewById(R.id.btn_submit_attendance);
        cardCheckInFeedback = findViewById(R.id.card_checkin_feedback);
        layoutFeedbackContainer = findViewById(R.id.layout_feedback_container);
        tvFeedbackTitle = findViewById(R.id.tv_feedback_title);
        tvFeedbackDetails = findViewById(R.id.tv_feedback_details);

        // Tab 4: Records
        etSearchRecords = findViewById(R.id.et_search_records);
        tvTotalRecordsCount = findViewById(R.id.tv_total_records_count);
        btnExportAllRecords = findViewById(R.id.btn_export_all_records);
        btnClearAllRecords = findViewById(R.id.btn_clear_all_records);
        tvRecordsEmptyNotice = findViewById(R.id.tv_records_empty_notice);
        rvAllRecords = findViewById(R.id.rv_all_records);
    }

    private void setupWindowInsets() {
        View mainView = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            bottomNav.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });
    }

    private void setupNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_students) {
                showTab(0);
                return true;
            } else if (itemId == R.id.nav_live_code) {
                showTab(1);
                return true;
            } else if (itemId == R.id.nav_check_in) {
                showTab(2);
                return true;
            } else if (itemId == R.id.nav_records) {
                showTab(3);
                return true;
            }
            return false;
        });
    }

    private void showTab(int tabIndex) {
        layoutStudents.setVisibility(tabIndex == 0 ? View.VISIBLE : View.GONE);
        layoutLiveCode.setVisibility(tabIndex == 1 ? View.VISIBLE : View.GONE);
        layoutCheckIn.setVisibility(tabIndex == 2 ? View.VISIBLE : View.GONE);
        layoutRecords.setVisibility(tabIndex == 3 ? View.VISIBLE : View.GONE);

        if (tabIndex == 0) {
            tvHeaderTitle.setText(R.string.attendance_title);
        } else if (tabIndex == 1) {
            tvHeaderTitle.setText("Live Code Generator");
            updateLiveCodeUI();
        } else if (tabIndex == 2) {
            tvHeaderTitle.setText("Student Check-In");
            updateCheckInUI();
        } else if (tabIndex == 3) {
            tvHeaderTitle.setText("Attendance Records");
            updateRecordsUI();
        }
    }

    // ==================== TAB 1: STUDENTS ROSTER ====================
    private void setupStudentsTab() {
        List<Student> students = getSampleStudents();
        studentAdapter = new StudentAdapter(students);

        rvStudents.setLayoutManager(new LinearLayoutManager(this));
        rvStudents.setAdapter(studentAdapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (studentAdapter != null) {
                    studentAdapter.filter(s != null ? s.toString() : "");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnMarkAllPresent.setOnClickListener(v -> {
            if (studentAdapter != null) {
                studentAdapter.markAllPresent();
                Toast.makeText(this, "All students marked present!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<Student> getSampleStudents() {
        List<Student> list = new ArrayList<>();
        list.add(new Student("1", "JACKSON THOMAS", true, R.drawable.avatar_boy));
        list.add(new Student("2", "ANUMOL C.V", false, R.drawable.avatar_girl));
        list.add(new Student("3", "AMAL BINOY", false, R.drawable.avatar_boy_alt));
        list.add(new Student("4", "JOSHUA JOMON", true, R.drawable.avatar_boy));
        list.add(new Student("5", "ANNA MATHEW", false, R.drawable.avatar_girl));
        return list;
    }

    // ==================== TAB 2: LIVE CODE GENERATOR ====================
    private void setupLiveCodeTab() {
        etSubjectName.setText(manager.getCurrentSubject());
        etSubjectName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null && !s.toString().trim().isEmpty()) {
                    manager.setCurrentSubject(s.toString().trim());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        switchSessionActive.setChecked(manager.isSessionActive());
        switchSessionActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
            manager.setSessionActive(isChecked);
            updateLivePill(isChecked);
        });

        btnRegenerateCode.setOnClickListener(v -> {
            manager.generateNewCode();
            updateLiveCodeUI();
            Toast.makeText(this, "New attendance code generated!", Toast.LENGTH_SHORT).show();
        });

        btnExportLive.setOnClickListener(v -> shareAttendanceReport());

        btnResetLiveSession.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Reset Live Session?")
                    .setMessage("This will clear live attendees and generate a new code.")
                    .setPositiveButton("Reset", (dialog, which) -> {
                        manager.resetLiveSession();
                        Toast.makeText(this, "Live session reset.", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        liveAttendeesAdapter = new AttendanceAdapter();
        rvLiveAttendees.setLayoutManager(new LinearLayoutManager(this));
        rvLiveAttendees.setAdapter(liveAttendeesAdapter);

        updateLiveCodeUI();
    }

    private void updateLivePill(boolean isActive) {
        if (isActive) {
            tvLivePill.setText("● LIVE");
            tvLivePill.setBackgroundResource(R.drawable.bg_live_pill);
            tvLiveCode.setTextColor(ContextCompat.getColor(this, R.color.code_card_text));
        } else {
            tvLivePill.setText("PAUSED");
            tvLivePill.setBackgroundColor(Color.GRAY);
            tvLiveCode.setTextColor(Color.LTGRAY);
        }
    }

    private void updateLiveCodeUI() {
        tvLiveCode.setText(manager.getFormattedCode());
        updateLivePill(manager.isSessionActive());

        List<AttendanceRecord> liveRecords = manager.getLiveSessionRecords();
        int count = liveRecords.size();
        tvLiveAttendanceCount.setText("Live Checked-In Attendees (" + count + ")");

        if (count == 0) {
            tvLiveEmptyNotice.setVisibility(View.VISIBLE);
            rvLiveAttendees.setVisibility(View.GONE);
        } else {
            tvLiveEmptyNotice.setVisibility(View.GONE);
            rvLiveAttendees.setVisibility(View.VISIBLE);
            liveAttendeesAdapter.setRecords(liveRecords);
        }
    }

    // ==================== TAB 3: STUDENT CHECK-IN ====================
    private void setupCheckInTab() {
        btnSubmitAttendance.setOnClickListener(v -> handleStudentCheckIn());
        updateCheckInUI();
    }

    private void handleStudentCheckIn() {
        hideKeyboard();
        String name = etInputStudentName.getText() != null ? etInputStudentName.getText().toString() : "";
        String studentId = etInputStudentId.getText() != null ? etInputStudentId.getText().toString() : "";
        String code = etInputCode.getText() != null ? etInputCode.getText().toString() : "";

        AttendanceManager.CheckInResult result = manager.markAttendance(name, studentId, code);

        cardCheckInFeedback.setVisibility(View.VISIBLE);
        if (result.isSuccess()) {
            layoutFeedbackContainer.setBackgroundColor(ContextCompat.getColor(this, R.color.success_container));
            tvFeedbackTitle.setTextColor(ContextCompat.getColor(this, R.color.success));
            tvFeedbackTitle.setText("✓ " + result.getMessage());

            AttendanceRecord r = result.getRecord();
            String details = "Student: " + r.getStudentName() + " (" + r.getStudentId() + ")\n"
                    + "Subject: " + r.getSubjectName() + "\n"
                    + "Time: " + r.getFormattedDateTime();
            tvFeedbackDetails.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            tvFeedbackDetails.setText(details);

            etInputCode.setText("");
            Toast.makeText(this, "Attendance Recorded Successfully!", Toast.LENGTH_SHORT).show();
        } else {
            layoutFeedbackContainer.setBackgroundColor(ContextCompat.getColor(this, R.color.error_container));
            tvFeedbackTitle.setTextColor(ContextCompat.getColor(this, R.color.error));
            tvFeedbackTitle.setText("✕ Check-In Failed");

            tvFeedbackDetails.setTextColor(ContextCompat.getColor(this, R.color.error));
            tvFeedbackDetails.setText(result.getMessage());
        }
    }

    private void updateCheckInUI() {
        tvStudentActiveSubject.setText("Active Subject: " + manager.getCurrentSubject());
    }

    // ==================== TAB 4: RECORDS HISTORY ====================
    private void setupRecordsTab() {
        allRecordsAdapter = new AttendanceAdapter();
        rvAllRecords.setLayoutManager(new LinearLayoutManager(this));
        rvAllRecords.setAdapter(allRecordsAdapter);

        etSearchRecords.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (allRecordsAdapter != null) {
                    allRecordsAdapter.filter(s != null ? s.toString() : "");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnExportAllRecords.setOnClickListener(v -> shareAttendanceReport());

        btnClearAllRecords.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Clear All Records?")
                    .setMessage("This will permanently delete all saved attendance history. This action cannot be undone.")
                    .setPositiveButton("Clear All", (dialog, which) -> {
                        manager.clearHistory();
                        Toast.makeText(this, "Attendance history cleared.", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        updateRecordsUI();
    }

    private void updateRecordsUI() {
        List<AttendanceRecord> allRecords = manager.getAllRecords();
        int count = allRecords.size();
        tvTotalRecordsCount.setText("Total: " + count + " Checked In");

        if (count == 0) {
            tvRecordsEmptyNotice.setVisibility(View.VISIBLE);
            rvAllRecords.setVisibility(View.GONE);
        } else {
            tvRecordsEmptyNotice.setVisibility(View.GONE);
            rvAllRecords.setVisibility(View.VISIBLE);
            allRecordsAdapter.setRecords(allRecords);
        }
    }

    // ==================== COMMON TIMERS & EVENT LISTENERS ====================
    private void setupManagerListener() {
        manager.addListener(() -> runOnUiThread(() -> {
            updateLiveCodeUI();
            updateCheckInUI();
            updateRecordsUI();
        }));
    }

    private void setupCodeTimer() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (manager.isSessionActive()) {
                    int remaining = manager.getRemainingSeconds();
                    if (remaining <= 0) {
                        manager.generateNewCode();
                        remaining = AttendanceManager.CODE_DURATION_SECONDS;
                    }
                    progressCodeTimer.setProgress(remaining);
                    tvCodeTimerCountdown.setText("Code refreshes in " + remaining + "s");
                    tvLiveCode.setText(manager.getFormattedCode());
                } else {
                    tvCodeTimerCountdown.setText("Session is currently paused");
                }
                timerHandler.postDelayed(this, 1000);
            }
        };
    }

    private void shareAttendanceReport() {
        String report = manager.generateExportText();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Attendance Report - " + manager.getCurrentSubject());
        intent.putExtra(Intent.EXTRA_TEXT, report);
        startActivity(Intent.createChooser(intent, "Share Attendance Report"));
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        timerHandler.removeCallbacks(timerRunnable);
        timerHandler.post(timerRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        timerHandler.removeCallbacks(timerRunnable);
    }
}
