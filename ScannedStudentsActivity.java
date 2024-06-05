package com.example.qrcodes;

import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ScannedStudentsActivity extends AppCompatActivity {
    private String selectedSection;

    String startDate, endDate;
    private String selectedSubject;
    private String selectedDate, meetingsCount, sectionStudentsCount;
    private String currentId;
    String formattedDate;
    TextView subtitle;
    ListView studentsListView;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanned_students);

        db = FirebaseFirestore.getInstance();
        subtitle = findViewById(R.id.subtitle6);
        studentsListView = findViewById(R.id.studentsListView);

        // Retrieve the values passed via Intent
        Intent intent = getIntent();
        if (intent != null) {
            startDate = intent.getStringExtra("startDate");
            endDate = intent.getStringExtra("endDate");
            selectedSection = intent.getStringExtra("selectedSection");
            selectedSubject = intent.getStringExtra("selectedSubject");
            sectionStudentsCount = intent.getStringExtra("sectionStudentsCount");
            selectedDate = intent.getStringExtra("selectedDate");
            currentId = intent.getStringExtra("currentId");
            meetingsCount = intent.getStringExtra("meetingsCount");
        }
        subtitle.setText(selectedSection + " \u2192 " + selectedSubject + " (" + meetingsCount + " meetings)");

        Log.d("StudentsList", "Start Date: " + startDate);
        Log.d("StudentsList", "End Date: " + endDate);
        Log.d("StudentsList", "Selected Section: " + selectedSection);
//        Log.d("sectionStudentsCount", "sectionStudentsCount: " + sectionStudentsCount);
        Log.d("StudentsList", "Selected Subject: " + selectedSubject);
        Log.d("StudentsList", "Selected Date: " + selectedDate);
        Log.d("StudentsList", "Current Id: " + currentId);
        Log.d("StudentsList", "Meetings Count: " + meetingsCount);


        // Define a data structure to store students and their occurrence counts
        Map<String, Integer> studentOccurrenceMap = new HashMap<>();
        Map<String, String> studentIdMap = new HashMap<>();  // New map to store student IDs

        db.collection("scannedData")
                .whereEqualTo("professorId", currentId)
                .whereEqualTo("section", selectedSection)
                .whereEqualTo("subject", selectedSubject)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Get the student name and ID
                            String studentName = document.getString("name");
                            String studentId = document.getString("userId");  // Assuming 'userId' is the field for student IDs

                            // Update the occurrence count in the map
                            if (studentName != null) {
                                if (studentOccurrenceMap.containsKey(studentName)) {
                                    // If the student name is already in the map, increment the occurrence count
                                    int count = studentOccurrenceMap.get(studentName);
                                    studentOccurrenceMap.put(studentName, count + 1);
                                } else {
                                    // If the student name is not in the map, add it with an initial count of 1
                                    studentOccurrenceMap.put(studentName, 1);
                                }

                                // Store the student ID in the studentIdMap
                                if (studentId != null) {
                                    studentIdMap.put(studentName, studentId);
                                }
                            }
                        }


                        // Combining the hell out of everything
                        List<String> studentList = new ArrayList<>();
                        studentList.sort(Comparator.reverseOrder());
                        for (Map.Entry<String, Integer> entry : studentOccurrenceMap.entrySet()) {
                            String studentName = entry.getKey();
                            int occurrenceCount = entry.getValue();
                            int absentCount = Integer.parseInt(meetingsCount.toString()) - occurrenceCount;
                            String studentId = studentIdMap.get(studentName); // Get the ID from the studentIdMap
                            String formattedStudent = studentName + " (Present: " + occurrenceCount + " | Absent: " + absentCount + ")";
                            studentList.add(formattedStudent);
                            Log.d("StudentList", formattedStudent);
                        }

                        Collections.sort(studentList);


                        ArrayAdapter<String> adapter = new ArrayAdapter<>(ScannedStudentsActivity.this, android.R.layout.simple_list_item_1, studentList);
                        studentsListView.setAdapter(adapter);

                        // Set an item click listener for the ListView
                        studentsListView.setOnItemClickListener((parent, view, position, id) -> {
                            String selectedStudent = studentList.get(position);

                            // Extract the name part before the opening parenthesis
                            int indexOfParenthesis = selectedStudent.indexOf("(");
                            if (indexOfParenthesis != -1) {
                                String name = selectedStudent.substring(0, indexOfParenthesis).trim();
                                String studentId = studentIdMap.get(name);
                                // 'name' will now contain only the name part
//                                Toast.makeText(ScannedStudentsActivity.this, "Selected Student: " + selectedStudent, Toast.LENGTH_SHORT).show();
                                Toast.makeText(ScannedStudentsActivity.this, name, Toast.LENGTH_SHORT).show();

                                Intent intent2 = new Intent(ScannedStudentsActivity.this, ScannedStudentRecord.class);
                                intent2.putExtra("startDate", startDate);
                                intent2.putExtra("endDate", endDate);
                                intent2.putExtra("selectedSection", selectedSection);
                                intent2.putExtra("selectedSubject", selectedSubject);
                                intent2.putExtra("selectedDate", selectedDate);
                                intent2.putExtra("selectedDateFormatted", formattedDate);
                                intent2.putExtra("selectedUser", name);
                                intent2.putExtra("selectedUserID", studentId);
                                intent2.putExtra("currentId", currentId);
                                startActivity(intent2);
                            }
                            // Display the student's name in a Toast
                        });
                    } else {
                        Log.d("FirestoreQuery", "Error getting documents: " + task.getException());
                    }
                });
    }


}

