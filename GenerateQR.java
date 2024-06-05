package com.example.qrcodes;

import static java.lang.String.format;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EventListener;
import java.util.List;
import java.util.Locale;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

// GenerateQR class
// ... (existing imports)

public class GenerateQR extends AppCompatActivity {
    String secccc = "";
    private TextView qrcodetv;
    private ImageView qrcode;
    private QRGEncoder qrgEncoder;
    private Bitmap bitmap;
    private TextView timerText;
    private CountDownTimer countDownTimer;
    private String selectedSubject, selectedSection;
    private File attendanceFile;
    FirebaseAuth mAuth;
    private SimpleDateFormat dateFormat;
    private Spinner section;
    private EditText calendarView;
    private TextView statusText, statusText2;
    private CollectionReference attendanceCollection;
    private int successfulScanCount = 0;
    String totalStudents;

    private FirebaseFirestore firestore;

    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_qr);

        mAuth = FirebaseAuth.getInstance();
        Spinner spinnerSubject = findViewById(R.id.spinnerSubject);
        qrcodetv = findViewById(R.id.idqrcodetext);
        statusText = findViewById(R.id.statusText);
        statusText2 = findViewById(R.id.statusText2);
        Spinner section = findViewById(R.id.spinnerSection);

        qrcode = findViewById(R.id.idQrcode);

        Button buttonqr = findViewById(R.id.buttonGqr);
        timerText = findViewById(R.id.timerText);


        calendarView = findViewById(R.id.calendarView4);
// Set the current date to the EditText
        setCurrentDate();
        calendarView.setFocusable(false);
        calendarView.setClickable(false);

        // Set OnClickListener for the EditText
//        calendarView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                showDatePickerDialog();
//            }
//        });


        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        selectedSubject = spinnerSubject.getSelectedItem().toString();

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        attendanceCollection = firestore.collection("attendance");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        //TODO: Uncomment this before forwarding
        //getting the list of sections
        String userId = currentUser.getUid();
//        String userId = "DERk950rMnURI9GbHjyK71v1bMy1";

        final String[] sectionName = new String[30]; //max of 30 sections per professor
        firestore.collection("sections")
                .whereEqualTo("professorID", userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int index = 0;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                sectionName[index] = document.getString("sectionName");
                                if (sectionName[index] != null) {
                                    Log.d("testss" + index, "Section Name: " + sectionName[index]);
                                } else {
                                    Log.d("testss", "Section Name is null for document: " + document.getId());
                                }
                                index++; //increment index
                            }
                        } else {
                            Log.d("testss", "Error getting documents: ", task.getException());
                        }

                        for (String section : sectionName) {
                            if (section != null) {
                                Log.d("SectionName", section);
                            }
                        }

                        //append the names of the section to the sectionList TextView
                        // Initialize the Spinner
                        List<String> validSectionNames = new ArrayList<>();
                        for (String name : sectionName) {
                            if (name != null) {
                                validSectionNames.add(name);
                            }
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(GenerateQR.this, android.R.layout.simple_spinner_item, validSectionNames);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        section.setAdapter(adapter);
//                        selectedSection = section.getItemAtPosition().toString();
//
//                        Log.d("selectedSection", "onComplete: " + selectedSection);


                        section.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                                // Get the selected item from the adapter
                                secccc = (String) parentView.getItemAtPosition(position);

                                // Show a Toast with the selected item
//                Toast.makeText(getApplicationContext(), "Selected Section: " + secccc, Toast.LENGTH_SHORT).show();

                                final String[] subjectName = new String[30]; //max of 30 sections per professor
                                firestore.collection("subjects")
                                        .whereEqualTo("professorID", userId)
                                        .whereEqualTo("sectionName", secccc)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    int index = 0;
                                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                                        subjectName[index] = document.getString("subjectName");
                                                        if (subjectName[index] != null) {
                                                            Log.d("testss" + index, "Subject Name: " + subjectName[index]);
                                                        } else {
                                                            Log.d("testss", "Subject Name is null for document: " + document.getId());
                                                        }
                                                        index++; //increment index
                                                    }
                                                } else {
                                                    Log.d("testss", "Error getting documents: ", task.getException());
                                                }

                                                for (String subject : subjectName) {
                                                    if (subject != null) {
                                                        Log.d("subjectName", subject);
                                                    }
                                                }

                                                //append the names of the section to the sectionList TextView
                                                // Initialize the Spinner
                                                List<String> validSubjectNames = new ArrayList<>();
                                                for (String name : subjectName) {
                                                    if (name != null) {
                                                        validSubjectNames.add(name);
                                                    }
                                                }

                                                ArrayAdapter<String> adapter = new ArrayAdapter<>(GenerateQR.this, android.R.layout.simple_spinner_item, validSubjectNames);
                                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                                spinnerSubject.setAdapter(adapter);

                                                try {
                                                    selectedSubject = spinnerSubject.getSelectedItem().toString();
                                                } catch (Exception e) {
                                                    selectedSubject = "";
                                                    e.printStackTrace(); // Print the exception details to the console
//                                    Toast.makeText(GenerateQR.this, "Error getting selected subject", Toast.LENGTH_SHORT).show();
                                                }

                                            }
                                        });

                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parentView) {
                                // Do nothing here
                            }
                        });
                    }
                });


        buttonqr.setOnClickListener(view -> {
//            selectedSection = section.getSelectedItem().toString();
//            Log.d("selectedSection", "onComplete: " + selectedSection);


            if (selectedSubject.isEmpty()) {
                Toast.makeText(GenerateQR.this, "Please select a subject", Toast.LENGTH_SHORT).show();
            } else {
                WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
                Display display = manager.getDefaultDisplay();
                Point point = new Point();
                display.getSize(point);
                int width = point.x;
                int height = point.y;
                int dimen = Math.min(width, height);
                dimen = dimen * 3 / 4;

                Log.d("statusText2", "a" + "successfulScanCount");
                try {
//                    Toast.makeText(this, selectedSection, Toast.LENGTH_SHORT).show();
                    String selectedDate = calendarView.getText().toString();
                    String sectionText = secccc;

                    // Log the selected date and section
                    Log.d("CalendarActivity", "Selected Subject: " + selectedSubject);
                    Log.d("CalendarActivity", "Selected Section: " + sectionText);
                    Log.d("CalendarActivity", "Selected Date: " + selectedDate);


                    String qrText = generateQRText(selectedSubject, selectedDate, sectionText, "Present");
                    qrgEncoder = new QRGEncoder(qrText, null, QRGContents.Type.TEXT, dimen);
                    bitmap = qrgEncoder.getBitmap();
                    qrcodetv.setVisibility(View.GONE);
                    qrcode.setImageBitmap(bitmap);
                    startCountdownTimer(2, selectedSubject, selectedDate, sectionText, dimen);
                    recordAttendance(selectedSubject, selectedDate, sectionText);


                } catch (Exception e) {

                    e.printStackTrace();
                }

//                / Set up a Firestore listener to listen for changes in the 'scannedData' collection
                successfulScanCount = 0;
                FirebaseFirestore db;
                String selectedDate = calendarView.getText().toString();
                String sectionText = secccc;
                db = FirebaseFirestore.getInstance();
                db.collection("scannedData")
                        .whereEqualTo("professorId", userId)
                        .whereEqualTo("subject", selectedSubject)
                        .whereEqualTo("date", selectedDate)
                        .whereEqualTo("section", sectionText)
                        .addSnapshotListener((value, error) -> {
                            if (error != null) {
                                // Handle the error
                                return;
                            }

                            // Increment successfulScanCount by the number of new documents
                            if (value != null) {
                                for (DocumentChange dc : value.getDocumentChanges()) {
                                    if (dc.getType() == DocumentChange.Type.ADDED) {
                                        successfulScanCount++;
                                    }
                                }

                            }
                            Log.d("statusText2", "b" + successfulScanCount);
                            //get total number of students in the section
//                            totalStudents = getStudentsCount (userId, selectedSection, selectedSubject);

                            //getting the total num of students
                            db.collection("subjects")
                                    .whereEqualTo("professorID", userId)
                                    .whereEqualTo("sectionName", sectionText)
                                    .whereEqualTo("subjectName", selectedSubject)
                                    .get()
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            for (DocumentSnapshot document : task.getResult()) {
                                                String studentsCount = document.getString("studentsCount");
                                                if (studentsCount != null) {
                                                    Log.d("StudentsCount", "Students Count: " + studentsCount);
                                                    totalStudents = studentsCount;
                                                } else {
                                                    // Handle the case where 'studentsCount' is null
                                                    Log.d("StudentsCount", "Students Count is null");
                                                }
                                            }
                                        } else {
                                            // Handle the error
                                            Log.d("FirestoreQuery", "Error getting documents: " + task.getException());
                                        }



                                        statusText2.setText(String.valueOf(successfulScanCount + " out of " + totalStudents + " scanned."));

                                    });

                        });


            }
        });

        spinnerSubject.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSubject = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private String generateQRText(String subject, String date, String section, String status) {
        //TODO: Uncomment this before forwarding
        String professorId = currentUser.getUid();
//        String professorId = "DERk950rMnURI9GbHjyK71v1bMy1"; //for testing only

        JSONObject qrPayload = new JSONObject();
        try {
            qrPayload.put("subject", subject);
            qrPayload.put("date", date);
            qrPayload.put("section", section);
            qrPayload.put("professorId", professorId);
            qrPayload.put("status", status);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return qrPayload.toString();
    }


    private void startCountdownTimer(int minutes, String selectedSubject, String selectedDate, String sectionText, int dimen) {
        countDownTimer = new CountDownTimer(minutes * 60 * 1000, 1000) {
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / (60 * 1000);
                long seconds = (millisUntilFinished / 1000) % 60;
                @SuppressLint("DefaultLocale") String timeLeftFormatted = format("%02d:%02d", minutes, seconds);
                timerText.setText(timeLeftFormatted);
            }

            public void onFinish() {
//                qrcode.setImageDrawable(null);

                String qrText = generateQRText(selectedSubject, selectedDate, sectionText, "Late");
                qrgEncoder = new QRGEncoder(qrText, null, QRGContents.Type.TEXT, dimen);
                bitmap = qrgEncoder.getBitmap();
                qrcodetv.setVisibility(View.GONE);
                qrcode.setImageBitmap(bitmap);
                startCountdownTimer2(30, selectedSubject, selectedDate, sectionText, dimen);
                statusText.setText("LATE");
//                recordAttendance(selectedSubject, selectedDate, sectionText);
                Toast.makeText(GenerateQR.this, "Recording Late Attendance.", Toast.LENGTH_SHORT).show();
            }
        }.start();
    }

    private void startCountdownTimer2(int minutes, String selectedSubject, String selectedDate, String sectionText, int dimen) {
        countDownTimer = new CountDownTimer(minutes * 60 * 1000, 1000) {
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / (60 * 1000);
                long seconds = (millisUntilFinished / 1000) % 60;
                @SuppressLint("DefaultLocale") String timeLeftFormatted = format("%02d:%02d", minutes, seconds);
                timerText.setText(timeLeftFormatted);
            }

            public void onFinish() {
                qrcode.setImageDrawable(null);

                Toast.makeText(GenerateQR.this, "Scanning time has expired.", Toast.LENGTH_SHORT).show();
            }
        }.start();
    }

    private void recordAttendance(String subject, String date, String section) {
        String userId = currentUser.getUid();

        String qrText = generateQRText(subject, date, section, "Present");

        Attendance attendance = new Attendance(userId, subject, date, section);

        String documentId = userId;

        attendanceCollection.document(documentId)
                .set(attendance)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(GenerateQR.this, "Attendance recorded", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(GenerateQR.this, "Failed to record attendance", Toast.LENGTH_SHORT).show();
                });

        int dimen = getQRCodeDimension();
        qrgEncoder = new QRGEncoder(qrText, null, QRGContents.Type.TEXT, dimen);
    }

    private int getQRCodeDimension() {
        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int width = point.x;
        int height = point.y;
        int dimen = Math.min(width, height);
        return dimen * 3 / 4;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private static class Attendance {
        private String subject;
        private String userId;
        private String date;

        private String section;

        public Attendance(String userId, String subject, String date, String section) {
            this.userId = userId;
            this.subject = subject;
            this.date = date;
            this.section = section;
        }
    }

    private void setCurrentDate() {
        // Get the current date
        Date currentDate = Calendar.getInstance().getTime();

        // Format the date to "yyyy-MM-dd"
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formattedDate = dateFormat.format(currentDate);

        // Set the formatted date to the EditText
        calendarView.setText(formattedDate);
    }

    private void showDatePickerDialog() {
        // Get current date
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // Create a DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int selectedYear, int selectedMonth, int selectedDay) {
                        // Update the EditText with the selected date
                        calendarView.setText(selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay);

                        Toast.makeText(GenerateQR.this, calendarView.getText(), Toast.LENGTH_SHORT).show();
                    }
                },
                year,
                month,
                dayOfMonth);

        // Show the DatePickerDialog
        datePickerDialog.show();
    }
}
