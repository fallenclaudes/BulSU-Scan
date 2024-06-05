package com.example.qrcodes;

import android.content.Intent;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.common.returnsreceiver.qual.This;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ScannedStudentsSubject extends AppCompatActivity {

    private FirebaseFirestore db;
    private ListView studentSubjectsListView;
    private TextView subtitle;
    private String currentId;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanned_students_subject);

//        String selectedSection = "";
        db = FirebaseFirestore.getInstance();
        studentSubjectsListView = findViewById(R.id.studentSubjectsListView);
        subtitle = findViewById(R.id.subtitle7);

        // Retrieve the data from the Intent
        Intent intent = getIntent();
        if (intent != null) {
            currentId = intent.getStringExtra("currentId");
            userName = intent.getStringExtra("userName");
        }

        subtitle.setText(userName);


        final String[] subjectName = new String[100]; //max of 100 subjects per professor
        ArrayList<String> uniqueNamesList = new ArrayList<>();

//        String finalSelectedSection = selectedSection;
        db.collection("scannedData")
                .whereEqualTo("userId", currentId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int index = 0;
                            Set<String> seenDates = new HashSet<>(); // Create a HashSet to store seen dates

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String date = document.getString("date");

                                // Check if the date is not in the set (i.e., it's unique)
//                                if (!seenDates.contains(date)) {
                                subjectName[index] = document.getString("subject");
                                if (subjectName[index] != null) {
                                    Log.d("testss" + index, "Subject Name: " + subjectName[index]);
                                }
                                // Add the date to the set to mark it as seen
//                                    seenDates.add(date);

//                                }
//                                else {
                                // The document date is outside the range
//                                    Log.d("DateCheck - Subject", "Date is outside the range");
//                                }
                                index++; // Increment index
                            }

                        } else {
                            Log.d("testss", "Error getting documents: ", task.getException());
                        }

                        // Loop through the array and log its contents
                        for (String name : subjectName) {
                            if (name != null) {
                                Log.d("contents of the 1d array", name); // Use your desired tag (e.g., "YourTag") and log level (e.g., Log.d)
                            }
                        }

                        //transform to 2 array to record unique names and their count
                        int length = subjectName.length;
                        ArrayList<String> uniqueNamesList = new ArrayList<>();
                        ArrayList<Integer> countsList = new ArrayList<>();

                        // Loop through the array to count unique names
                        for (int i = 0; i < length; i++) {
                            String name = subjectName[i];
                            // Check if the name is not null
                            if (name != null) {
                                int count = 1; // Initialize count to 1 for the current name

                                // Check if the name is already in the uniqueNamesList
                                if (uniqueNamesList.contains(name)) {
                                    // If it's already in the list, find its index and update the count
                                    int index = uniqueNamesList.indexOf(name);
                                    count = countsList.get(index) + 1;
                                    countsList.set(index, count);
                                } else {
                                    // If it's not in the list, add it to both lists
                                    uniqueNamesList.add(name);
                                    countsList.add(count);
                                }
                            }
                        }

                        // Convert the ArrayLists to arrays
                        String[] uniqueNamesArray = uniqueNamesList.toArray(new String[0]);
//                        Integer[] countsArray = countsList.toArray(new Integer[0]);
                        final List<String> meetingsList = new ArrayList<>();
                        final List<String> countsArray = new ArrayList<>();

                        // Print the unique names and their counts to the logcattts
                        for (int i = 0; i < uniqueNamesArray.length; i++) {
                            int finalI = i;
                            int finalI1 = i;

                            Log.d("CHecking for", uniqueNamesArray[i]);
                            db.collection("scannedData")
                                    .whereEqualTo("subject", uniqueNamesArray[i])
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                    String date = document.getString("date");
                                                    meetingsList.add(date);
                                                }
                                            }


                                            Log.d("Before", "meetingsList (before): " + meetingsList);
                                            HashSet<String> uniqueMeetingsSet = new HashSet<>(meetingsList);
                                            meetingsList.clear();
                                            meetingsList.addAll(uniqueMeetingsSet);
                                            Log.d("After", "meetingsList (after): " + meetingsList);

                                            int hey = meetingsList.size();
                                            countsArray.add(String.valueOf(hey));
                                            meetingsList.clear();


//                                            System.out.println("Unique Name: " + uniqueNamesArray[finalI] + ", Count: " + countsArray.get(finalI1));


                                            CollectionReference scannedDataRef = db.collection("scannedData");
                                            List<String> combinedList = new ArrayList<>();
                                            int i = 0;
                                            for (String subject : uniqueNamesArray) {
                                                // Use a Map to store the counts for each subject
                                                Map<String, Object> countsMap = new HashMap<>();
                                                countsMap.put("subject", subject);

                                                // Query Firestore for each subject
                                                Query query = scannedDataRef
                                                        .whereEqualTo("userId", currentId)
                                                        .whereEqualTo("subject", subject);

                                                int finalI = i;
                                                query.get().addOnCompleteListener(taskz -> {
                                                    int documentCount = 0;

                                                    if (taskz.isSuccessful()) {

                                                        for (QueryDocumentSnapshot document : taskz.getResult()) {
                                                            // Get the value of the 'status' field
                                                            String status = document.getString("status");

                                                            if (status != null && (status.startsWith("P") || status.startsWith("L") ||
                                                                    status != null && (status.startsWith("p") || status.startsWith("l")))) {
                                                                // 'status' starts with "P" or "L"
                                                                documentCount++;
                                                                System.out.println("Status for document " + document.getId() + ": " + status);
                                                            }

                                                        }

//                                                        documentCount = taskz.getResult().size();
                                                        countsMap.put("count", documentCount);
                                                        // Log or use the documentCount as needed
                                                        Log.d("DocumentCount", "Subject: " + subject + ", Count: " + documentCount);

                                                        // Combine the information and update the UI here
                                                        // get Absent
//                                    int absentCount = countsArray[finalI]-countsMap.get("count");
                                                        String combinedString = uniqueNamesArray[finalI] + " (" + countsArray.get(finalI) + " meetings " +
                                                                "| Present: " + countsMap.get("count") +
                                                                ")";
                                                        combinedList.add(combinedString);

                                                        // Update the UI only when all queries are complete
                                                        if (combinedList.size() == uniqueNamesArray.length) {
                                                            String[] combinedArray = combinedList.toArray(new String[0]);
                                                            ArrayAdapter<String> adapter = new ArrayAdapter<>(ScannedStudentsSubject.this, android.R.layout.simple_list_item_1, combinedArray);
                                                            studentSubjectsListView.setAdapter(adapter);


                                                            //adding a onClickListener
                                                            studentSubjectsListView.setOnItemClickListener((adapterView, view, position, id) -> {
                                                                String selectedPosition = adapter.getItem(position);

//                                            Toast.makeText(ScannedStudentsSubject.this, combinedArray[position].toString(), Toast.LENGTH_SHORT).show();

                                                                // Initialize an ArrayList to store the extracted subject names and total meeting counts
                                                                ArrayList<String> subjectNamesList = new ArrayList<>();
                                                                ArrayList<Integer> totalMeetingCountsList = new ArrayList<>();

// Iterate through the combinedArray
                                                                for (String item : combinedArray) {
                                                                    // Find the opening parenthesis and closing parenthesis
                                                                    int openParenthesisIndex = item.indexOf("(");
                                                                    int closeParenthesisIndex = item.indexOf(")");

                                                                    if (openParenthesisIndex != -1 && closeParenthesisIndex != -1) {
                                                                        // Extract the substring between the opening and closing parentheses
                                                                        String meetingsInfo = item.substring(openParenthesisIndex + 1, closeParenthesisIndex).trim();

                                                                        // Split the meetingsInfo using spaces and get the first part as the total meeting count
                                                                        String[] parts = meetingsInfo.split(" ");
                                                                        if (parts.length > 0) {
                                                                            try {
                                                                                int totalMeetingCount = Integer.parseInt(parts[0]);
                                                                                // Add the subject name and total meeting count to their respective lists
                                                                                subjectNamesList.add(item.substring(0, openParenthesisIndex).trim());
                                                                                totalMeetingCountsList.add(totalMeetingCount);
                                                                            } catch (
                                                                                    NumberFormatException e) {
                                                                                // Handle invalid meeting count (not a valid integer)
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                                String[] subjectNamesArray = subjectNamesList.toArray(new String[0]);
                                                                Integer[] totalMeetingCountsArray = totalMeetingCountsList.toArray(new Integer[0]);

                                                                for (int inn = 0; inn < subjectNamesArray.length; inn++) {
                                                                    System.out.println("Subject: " + subjectNamesArray[inn]);
                                                                    System.out.println("Total Meeting Count: " + totalMeetingCountsArray[inn]);
                                                                }

                                                                Toast.makeText(ScannedStudentsSubject.this, subjectNamesArray[position].toString() + " (" + totalMeetingCountsArray[position].toString() + " meetings)", Toast.LENGTH_SHORT).show();


                                                                String selectedSubject = subjectNamesArray[position].toString();
                                                                String meetingsCount = totalMeetingCountsArray[position].toString();
//                                            Log.d("testss", uniqueNamesArray[position]);
                                                                if (selectedSubject != null) {
                                                                    Intent intent = new Intent(ScannedStudentsSubject.this, ScannedStudentsAttendance.class);
                                                                    intent.putExtra("selectedSubject", selectedSubject);
                                                                    intent.putExtra("meetingsCount", meetingsCount);
                                                                    intent.putExtra("currentId", currentId);
                                                                    intent.putExtra("userName", userName);
                                                                    startActivity(intent);
                                                                }
                                                            });

                                                        }
                                                    } else {
                                                        Log.e("FirestoreQuery", "Error getting documents: ", taskz.getException());
                                                    }
                                                });
                                                i++;
                                            }

                                        }


                                    });


                        }


                    }
                });


//        loadData(selectedSection, currentId);
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
