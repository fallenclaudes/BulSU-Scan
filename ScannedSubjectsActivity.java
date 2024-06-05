package com.example.qrcodes;

import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ScannedSubjectsActivity extends AppCompatActivity {

    private ListView subjectsListView;
    String startDate, endDate;

    private ArrayAdapter<String> adapter;
    private ArrayList<String> studentsList;
    private CollectionReference scannedDataCollection;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    String currentId;
    TextView subtitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanned_subjects);

        String selectedSection = "";
        db = FirebaseFirestore.getInstance();
        subjectsListView = findViewById(R.id.subjectsListView);
        subtitle = findViewById(R.id.subtitle2);

        // Retrieve the data from the Intent
        Intent intent = getIntent();
        if (intent != null) {
            startDate = intent.getStringExtra("startDate");
            endDate = intent.getStringExtra("endDate");
            String inputDate1 = intent.getStringExtra("startDate");
            String inputDate2 = intent.getStringExtra("endDate");

            selectedSection = intent.getStringExtra("selectedSection");
            currentId = intent.getStringExtra("currentId");
            Toast.makeText(this, selectedSection, Toast.LENGTH_SHORT).show();
        }
        String selectedSectionz = selectedSection;
        Log.d("TAG", "onCreate: " + selectedSection);
        Log.d("TAG", "Starttt Date: " + startDate);
        Log.d("TAG", "Enddd Date: " + endDate);
        subtitle.setText(selectedSection);

        List<String> subjectNamesList = new ArrayList<>();
        List<String> meetingsCountArr = new ArrayList<>();

        String finalSelectedSection = selectedSection;

        // Define a map to store subject names and their respective meeting counts
        Map<String, Integer> subjectMeetingCountsMap = new HashMap<>();

        db.collection("subjects").whereEqualTo("professorID", currentId).whereEqualTo("sectionName", selectedSection).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String subjectName = document.getString("subjectName");
                        // Add the subjectName to the list
                        subjectNamesList.add(subjectName);

                        db.collection("scannedData").whereEqualTo("professorId", currentId).whereEqualTo("section", finalSelectedSection).whereEqualTo("subject", subjectName).get().addOnCompleteListener(task2 -> {
                            int meetingsCount = 0;
                            Set<String> uniqueDates = new HashSet<>();

                            if (task2.isSuccessful()) {
                                for (QueryDocumentSnapshot document2 : task2.getResult()) {
                                    String documentDateString = document2.getString("date");
                                    if (isDateWithinRange(documentDateString, startDate, endDate)) {
                                        if (!uniqueDates.contains(documentDateString)) {
                                            Log.d("Add meeting", "Meeting ID Added: " + document2.getId());
                                            meetingsCount++;
                                            uniqueDates.add(documentDateString); // Add the date to the set to mark it as encountered
                                        }
                                    }
                                }
                                Log.d("MeetingCount", "Number of meetings: " + meetingsCount);
                            } else {
                                Log.d("FirestoreQuery", "Error getting documents: ", task2.getException());
                            }
                            meetingsCountArr.add(String.valueOf(meetingsCount));

                            // Update the map with the meeting count for this subject
                            subjectMeetingCountsMap.put(subjectName, meetingsCount);

                            if (subjectMeetingCountsMap.size() == subjectNamesList.size()) {
                                // All queries are completed, construct combinedList and set the adapter
                                List<String> combinedList = new ArrayList<>();
                                for (String subject : subjectNamesList) {
                                    int meetingCount = subjectMeetingCountsMap.get(subject);
                                    String combined = subject + " (" + meetingCount + " meetings)";
                                    combinedList.add(combined);
                                }

                                adapter = new ArrayAdapter<>(ScannedSubjectsActivity.this, android.R.layout.simple_list_item_1, combinedList);
                                subjectsListView.setAdapter(adapter);

                                // Add the onClickListener code here as you had before
                                subjectsListView.setOnItemClickListener((adapterView, view, position, id) -> {
                                    String selectedPosition = adapter.getItem(position);
                                    String selectedSubject = subjectNamesList.get(position);
                                    String meetCount = meetingsCountArr.get(position);

                                    Log.d("SelectedPosition", "Position: " + selectedPosition);
                                    Log.d("SelectedSubject", "Subject: " + selectedSubject);
                                    Log.d("MeetingsCount", "Final Meetings Count: " + meetingsCountArr.size());
                                    Log.d("Current ID", "ID: " + currentId);
                                    Log.d("Start Date", "Start Date: " + startDate);
                                    Log.d("End Date", "End Date: " + endDate);

                                    if (selectedSubject != null) {
                                        Intent intent = new Intent(ScannedSubjectsActivity.this, ScannedAttendanceActivity.class);
                                        intent.putExtra("startDate", startDate);
                                        intent.putExtra("endDate", endDate);
                                        intent.putExtra("selectedSection", selectedSectionz);
                                        intent.putExtra("selectedSubject", selectedSubject);
                                        intent.putExtra("meetingsCount", meetCount);
                                        intent.putExtra("currentId", currentId);
                                        startActivity(intent);
                                    }
                                });
                            }
                        });
                    }
                } else {
                    Log.d("testss", "Error getting documents: ", task.getException());
                }
            }
        });
    }

    private boolean isDateWithinRange(String date, String startDate, String endDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date dateToCheck = sdf.parse(date);
            Date rangeStartDate = sdf.parse(startDate);
            Date rangeEndDate = sdf.parse(endDate);

            return dateToCheck.compareTo(rangeStartDate) >= 0 && dateToCheck.compareTo(rangeEndDate) <= 0;
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }





    private void loadData(String selectedSection, String currentId) {
        // Replace "YOUR_COLLECTION_NAME" with your actual collection name
        String collectionName = "scannedData";

// Define the Firestore query
        Query query = db.collection(collectionName).whereEqualTo("section", selectedSection);
//                .whereEqualTo("professorId", currentId);

// Perform the query
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    // Iterate through the documents that match the query
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        // Access the data in each document as needed
                        String documentId = document.getId(); // Get the document ID
                        String sectionValue = document.getString("section"); // Get the "section" field value
                        String professorIdValue = document.getString("professorId"); // Get the "professorId" field value

                        // Do something with the retrieved data
                        Log.d("Firestore", "Document ID: " + documentId);
                        Log.d("Firestore", "Section: " + sectionValue);
                        Log.d("Firestore", "Professor ID: " + professorIdValue);
                    }
                } else {
                    Log.e("Firestore", "Error getting documents: ", task.getException());
                }
            }
        });

    }

}