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
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScannedStudentsAttendance extends AppCompatActivity {

    private String userName, selectedSubject, meetingsCount, currentId;
    private TextView subtitle;
    private FirebaseFirestore db;
    ListView studentsAttendanceListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanned_students_attendance);

        // Get the intent that started this activity
        Intent intent = getIntent();

        // Retrieve the values from the intent extras
        selectedSubject = intent.getStringExtra("selectedSubject");
        meetingsCount = intent.getStringExtra("meetingsCount");
        currentId = intent.getStringExtra("currentId");
        userName = intent.getStringExtra("userName");

        // Log the values
        Log.d("IntentData", "Selected Subject: " + selectedSubject);
        Log.d("IntentData", "Meetings Count: " + meetingsCount);
        Log.d("IntentData", "Current ID: " + currentId);
        Log.d("IntentData", "User Name: " + userName);

        subtitle = findViewById(R.id.subtitle8);
        subtitle.setText(userName + " \u2192 " + selectedSubject);

        db = FirebaseFirestore.getInstance();
        studentsAttendanceListView = findViewById(R.id.studentsAttendanceListView);

        // Create arrays for meetings, attendance, and counts
// Create arrays for meetings and attendance
        final List<String> meetingsList = new ArrayList<>();
        final List<String> attendanceList = new ArrayList<>();
        final List<String> statusList = new ArrayList<>();
        final List<String> combinationList = new ArrayList<>();

// Retrieve data from Firestore
        db.collection("scannedData")
                .whereEqualTo("subject", selectedSubject)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Retrieve the date from the Firestore document
                                String date = document.getString("date");
                                String userId = document.getString("userId");
                                String status = document.getString("status");

                                // Check if the userId matches the currentId
                                if (userId != null && userId.equals(currentId)) {
                                    // Add the date to the attendanceList
                                    attendanceList.add(date);
                                    Log.d("date adding", "PRESENT: " + date + "-"+status);
//                                    combinationList.add(formatDate(date) + " (" + status+")");
                                    combinationList.add(formatDate(date) + " (" + status+")");
                                }
//                                if (status != null) {
//                                    statusList.add(status);
//                                } else {
//                                    statusList.add("Absent");
//                                }

                                // Add the date to the meetingsList
                                meetingsList.add(date);

                                // Log the date
                                Log.d("MeetingDate", "Date: " + date);
                            }

                            logList("before combisss", combinationList);

                        } else {
                            Log.d("Firestore", "Error getting documents: " + task.getException());
                        }


                        Log.d("Before", "meetingsList (before): " + meetingsList);
                        HashSet<String> uniqueMeetingsSet = new HashSet<>(meetingsList);
                        meetingsList.clear();
                        meetingsList.addAll(uniqueMeetingsSet);
                        Log.d("After", "meetingsList (after): " + meetingsList);


                        Log.d("Formatting", "meetingsList (before format): " + meetingsList);
                        for (int i = 0; i < meetingsList.size(); i++) {
                            meetingsList.set(i, formatDate(meetingsList.get(i)));
                        }
                        Log.d("Formatting", "meetingsList (after format): " + meetingsList);
//                        Log.d("Formatting", "attendanceList (before format): " + attendanceList);
//                        for (int i = 0; i < attendanceList.size(); i++) {
//                            attendanceList.set(i, formatDate(attendanceList.get(i)));
//                        }
//                        Log.d("Formatting", "attendanceList (after format): " + attendanceList);
//
                        logList("before combi", combinationList);


                        List<String> mergedList = mergeLists(combinationList, meetingsList);
                        // Print the mergedList
                        for (String item : mergedList) {
                            System.out.println(item);
                        }


                        //combining the 2 lists to see where dates are the user present and not
                        List<String> combinedList = new ArrayList<>();
                        for (int i = 0; i < meetingsList.size(); i++) {
                            String meeting = meetingsList.get(i);


//                            if (attendanceList.contains(meeting)) {
//                                combinedList.add(meeting + " (Present)");
//                            } else {
//                                combinedList.add(meeting);
//                            }
//                            combinedList.add(meeting + " (" + statusList.get(i) + ")");
                        }

                        //        String[] trimmedAttendanceList = nonNullList.toArray(new String[0]);

                        // Load the trimmedAttendanceList to the ListView
//                        ArrayAdapter<String> adapter = new ArrayAdapter<>(ScannedStudentsAttendance.this, android.R.layout.simple_list_item_1, combinedList);
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(ScannedStudentsAttendance.this, android.R.layout.simple_list_item_1, mergedList);
                        studentsAttendanceListView.setAdapter(adapter);
                    }
                });


    }




    private static List<String> mergeLists(List<String> combinationList, List<String> meetingsList) {


        logList("Combination List", combinationList);
        logList("Meetings List", meetingsList);

        List<String> mergedList = new ArrayList<>();

//        for (String combinationItem : combinationList) {
//            // Extract date and statement
//            String date = extractDate(combinationItem);
//            String statement = extractStatement(combinationItem);
//
//            // Print the extracted values
//            System.out.println("Extracted Date: " + date);
//            System.out.println("Statement: " + statement);
//            System.out.println();
//        }

        // Extract dates and sort them
        List<String> sortedDates = extractAndSortDates(combinationList, meetingsList);

        for (String date : sortedDates) {
            // Get the original combinationItem corresponding to the date
            String combinationItem = getCombinationItem(combinationList, date);

            // Check if the date is in meetingsList
            if (meetingsList.contains(date)) {
                // Add the original combinationItem from combinationList
                Log.d("TAG", "mergeLists: "+combinationItem);
                mergedList.add(combinationItem);

            } else {
                // If not in meetingsList, add the date from meetingsList
                mergedList.add(date);
            }
        }

        List<String> filteredList = filterList(mergedList);

        // Print the filteredList
        for (String item : filteredList) {
            System.out.println(item);
        }
//        return mergedList;
        return filteredList;
    }


    private static List<String> filterList(List<String> mergedList) {
        List<String> filteredList = new ArrayList<>();
//        Pattern pattern = Pattern.compile("([a-zA-Z]+ \\d{1,2}, \\d{4})");
//        Pattern pattern = Pattern.compile("November 15, 2023.*");

        Pattern pattern = Pattern.compile("([a-zA-Z]+ \\d{1,2}, \\d{4}).*");



        for (String item : mergedList) {
            Matcher matcher = pattern.matcher(item);
            if (matcher.matches()) {
                filteredList.add(item);
            }
        }

        return filteredList;
    }

    private static String extractStatement(String input) {
        // Use regex to extract the statement within parentheses
        Pattern pattern = Pattern.compile("\\(([^)]+)\\)");
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return "Something went Wrong";
        }
    }

    private static List<String> extractAndSortDates(List<String> combinationList, List<String> meetingsList) {
        List<String> allDates = new ArrayList<>();

        // Extract dates from combinationList
        for (String combinationItem : combinationList) {
            String date = extractDate(combinationItem);
            allDates.add(date);
        }

        // Add dates from meetingsList
        allDates.addAll(meetingsList);

        // Remove duplicates
        allDates = new ArrayList<>(new HashSet<>(allDates));

        // Sort the dates
        Collections.sort(allDates, (date1, date2) -> {
            try {
                Date d1 = new SimpleDateFormat("MMMM dd, yyyy").parse(date1);
                Date d2 = new SimpleDateFormat("MMMM dd, yyyy").parse(date2);
                return d1.compareTo(d2);
            } catch (ParseException e) {
                e.printStackTrace();
                return 0;
            }
        });

        return allDates;
    }

    private static String extractDate(String input) {
        // Use regex to extract the date part within parentheses
        Pattern pattern = Pattern.compile("\\(([^)]+)\\)");
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return input; // Return the input as is if no match is found
        }
    }

    private static String getCombinationItem(List<String> combinationList, String date) {
        for (String combinationItem : combinationList) {
            if (combinationItem.contains(date)) {
                Log.d("TAG", "getCombinationItem: "+combinationItem);
                return combinationItem;
            }
        }
        return date; // Return the date itself if not found
    }


    public String formatDate(String inputDate) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM dd, yyyy");
            Date date = inputFormat.parse(inputDate);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return inputDate; // return the original date if there's an error
        }
    }


    private static void logList(String listName, List<String> list) {
        System.out.println("Logging " + listName + ":");
        for (String item : list) {
            System.out.println(item);
        }
        System.out.println();
    }
}