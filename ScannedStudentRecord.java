package com.example.qrcodes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ScannedStudentRecord extends AppCompatActivity {
    private String selectedSection;
    private String startDate, endDate;
    private String selectedSubject;
    private String selectedDate;
    private String formattedDate;
    private String currentId;
    private String selectedUser;
    private String selectedUserID;
    private FirebaseFirestore db;
    private ListView studentRecordsListView;
    private TextView subtitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanned_student_record);

        db = FirebaseFirestore.getInstance();
        studentRecordsListView = findViewById(R.id.studentRecordsListView);
        subtitle = findViewById(R.id.subtitle5);

        // Retrieve the values passed via Intent
        Intent intent = getIntent();
        if (intent != null) {
            startDate = intent.getStringExtra("startDate");
            endDate = intent.getStringExtra("endDate");
            selectedSection = intent.getStringExtra("selectedSection");
            selectedSubject = intent.getStringExtra("selectedSubject");
            selectedDate = intent.getStringExtra("selectedDate");
            formattedDate = intent.getStringExtra("selectedDateFormatted");
            selectedUser = intent.getStringExtra("selectedUser");
            selectedUserID = intent.getStringExtra("selectedUserID");
            currentId = intent.getStringExtra("currentId");
        }

        // Print the values in the log
        Log.d("LogTag", "Start Date: " + startDate);
        Log.d("LogTag", "End Date: " + endDate);
        Log.d("Global Variables", "selectedSection: " + selectedSection);
        Log.d("Global Variables", "selectedSubject: " + selectedSubject);
        Log.d("Global Variables", "selectedDate: " + selectedDate);
        Log.d("Global Variables", "formattedDate: " + formattedDate);
        Log.d("Global Variables", "selectedUser: " + selectedUser);
        Log.d("Global Variables", "selectedUserID: " + selectedUserID);
        Log.d("Global Variables", "currentId: " + currentId);
        subtitle.setText(selectedSection + " \u2192 " + selectedSubject + " \u2192 " + selectedUser);

        // Get records for the student
        db.collection("scannedData")
                .whereEqualTo("professorId", currentId)
                .whereEqualTo("section", selectedSection)
                .whereEqualTo("subject", selectedSubject)
                .whereEqualTo("name", selectedUser)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<String> studentRecordList = new ArrayList<>();
                            Set<String> seenDates = new HashSet<>();

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String documentDateString = document.getString("date");
                                String status = document.getString("status");

                                try {
                                    Date documentDate = new SimpleDateFormat("yyyy-MM-dd").parse(documentDateString);
                                    Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse(intent.getStringExtra("startDate"));
                                    Date endDate = new SimpleDateFormat("yyyy-MM-dd").parse(intent.getStringExtra("endDate"));

                                    if (documentDate.equals(startDate) || documentDate.after(startDate) && documentDate.before(endDate) || documentDate.equals(endDate)) {
                                        if (!seenDates.contains(documentDateString)) {
                                            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM dd, yyyy");
                                            Date date2 = new SimpleDateFormat("yyyy-MM-dd").parse(documentDateString);
                                            String formattedDate = outputFormat.format(date2);

                                            String record = formattedDate + " (" + status + ")";
                                            studentRecordList.add(record);

                                            Log.d("Attendance Date", record);
                                            seenDates.add(documentDateString);
                                        }
                                    }
                                } catch (ParseException e) {
                                    Log.e("DateParseError", "Error parsing date", e);
                                }
                            }

                            ArrayAdapter<String> adapter = new ArrayAdapter<>(ScannedStudentRecord.this, android.R.layout.simple_list_item_1, studentRecordList);
                            studentRecordsListView.setAdapter(adapter);
                        } else {
                            Log.e("FirestoreQuery", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}
