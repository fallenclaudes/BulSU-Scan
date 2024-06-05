package com.example.qrcodes;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import android.Manifest;


public class ScannedAttendanceActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 123;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    String selectedSubject, selectedSection;
    String currentId, meetingsCount;
    ListView attendanceListView;
    TextView subtitle;
    String studentCount = "";
    Button studentsBtn, exportBtn;
    String startDate, endDate;
    String allCountMeetings;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanned_attendance);

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        studentsBtn = findViewById(R.id.studentsBtn);
        exportBtn = findViewById(R.id.exportBtn);

        // Retrieve the data from the Intent
        Intent intent = getIntent();
        if (intent != null) {
            startDate = intent.getStringExtra("startDate");
            endDate = intent.getStringExtra("endDate");
            selectedSection = intent.getStringExtra("selectedSection");
            selectedSubject = intent.getStringExtra("selectedSubject");
            currentId = intent.getStringExtra("currentId");
            meetingsCount = intent.getStringExtra("meetingsCount");
            ;
        }
        Log.d("TAG", "onCreate: " + selectedSubject);

        subtitle = findViewById(R.id.subtitle3);
        subtitle.setText(selectedSection + " \u2192 " + selectedSubject);

        db = FirebaseFirestore.getInstance();
        attendanceListView = findViewById(R.id.attendanceListView);

// Print the values in the
        Log.d("LogTag", "Meetings Count: " + meetingsCount);
        Log.d("LogTag", "Start Date: " + startDate);
        Log.d("LogTag", "End Date: " + endDate);
        Log.d("LogTag", "selectedSection: " + selectedSection);
        Log.d("LogTag", "selectedSubject: " + selectedSubject);
        Log.d("LogTag", "currentId: " + currentId);


        String[] subjectNamesArray;
        String[] meetingsCountArray;

        List<String> subjectNamesList = new ArrayList<>();
        List<String> meetingsCountArr = new ArrayList<>();

        String finalSelectedSection = selectedSection;


        //getting the list of meetings/attendance
        final String[] attendanceList = new String[1000];
        final String[] attendanceListCount = new String[1000];
        ArrayList<String> uniqueNamesList = new ArrayList<>();

        db.collection("scannedData").whereEqualTo("professorId", currentId).whereEqualTo("section", selectedSection).whereEqualTo("subject", selectedSubject).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    int index = 0;
                    Set<String> seenDates = new HashSet<>(); // Create a HashSet to store seen dates

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String date = document.getString("date");

                        //check first if inside the startDate and endDate
                        // Assuming startDate and endDate are in the format "yyyy-MM-dd"
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        String documentDateString = document.getString("date");

                        try {
                            Date documentDate = dateFormat.parse(documentDateString);
                            Date startDate = dateFormat.parse(intent.getStringExtra("startDate"));
                            Date endDate = dateFormat.parse(intent.getStringExtra("endDate"));

                            // Check if the document date is between the start and end dates
                            if (documentDate.equals(startDate) || documentDate.after(startDate) && documentDate.before(endDate) || documentDate.equals(endDate)) {
                                // The document date is within the range
                                Log.d("DateCheck", "Date is within the range");

                                //for counting the number of classes
                                //Changing the date format
                                SimpleDateFormat inputFormat0 = new SimpleDateFormat("yyyy-MM-dd");
                                Date date0 = null;
                                try {
                                    date0 = inputFormat0.parse(document.getString("date"));
                                } catch (ParseException e) {
                                    throw new RuntimeException(e);
                                }

                                // Format the date in the desired output format
                                SimpleDateFormat outputFormat0 = new SimpleDateFormat("MMMM dd, yyyy");
                                String formattedDate0 = outputFormat0.format(date0);

                                attendanceListCount[index] = formattedDate0;

                                // Check if the date is not in the set (i.e., it's unique)
                                if (!seenDates.contains(date)) {
                                    //Changing the date format
                                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
                                    Date date2 = null;
                                    try {
                                        date2 = inputFormat.parse(document.getString("date"));
                                    } catch (ParseException e) {
                                        throw new RuntimeException(e);
                                    }

                                    // Format the date in the desired output format
                                    SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM dd, yyyy");
                                    String formattedDate = outputFormat.format(date2);
                                    attendanceList[index] = formattedDate;

                                    if (attendanceList[index] != null) {
                                        Log.d("testss" + index, "Attendance Date: " + attendanceList[index]);
                                    }

                                    // Add the date to the set to mark it as seen
                                    seenDates.add(date);
                                }
                                index++; // Increment index

                            } else {
                                // The document date is outside the range
                                Log.d("DateCheck", "Date is outside the range");
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                    }
                } else {
                    Log.d("testss", "Error getting documents: ", task.getException());
                }
                //check the array contents
                for (String item : attendanceListCount) {
                    if (item != null) {
                        Log.d("TAGSS", "onComplete: " + item);
                    }
                }

                //transform to 2 array to record unique names and their count
                int length = attendanceListCount.length;
                ArrayList<String> uniqueNamesList = new ArrayList<>();
                ArrayList<Integer> countsList = new ArrayList<>();

                // Loop through the array to count unique names
                for (int i = 0; i < length; i++) {
                    String name = attendanceListCount[i];
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
                Integer[] countsArray = countsList.toArray(new Integer[0]);

                // Print the unique names and their counts to the logcattts
                for (int i = 0; i < uniqueNamesArray.length; i++) {
                    System.out.println("Unique Date: " + uniqueNamesArray[i] + ", Count: " + countsArray[i]);
                }


                //Trim the attendanceList Array to remove unnecessary indexes
                // Create a list to store non-null elements
                List<String> nonNullList = new ArrayList<>();
                for (String item : attendanceList) {
                    if (item != null) {
                        nonNullList.add(item);
                    }
                }
                String[] trimmedAttendanceList0 = nonNullList.toArray(new String[0]);


                //Sorting the dates:
                // Parse the date strings into Date objects
                List<Date> dates = new ArrayList<>();
                SimpleDateFormat inputFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH);
                for (String dateString : trimmedAttendanceList0) {
                    try {
                        Date date = inputFormat.parse(dateString);
                        dates.add(date);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                // Sort the Date objects from earliest to latest
                Collections.sort(dates);

                // Format the sorted dates back to the "MMMM dd, yyyy" format
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH);
                List<String> sorted = new ArrayList<>();
                for (Date date : dates) {
                    sorted.add(outputFormat.format(date));
                }

                // Print the sorted date strings
                for (String sortedDateString : sorted) {
                    System.out.println(sortedDateString);
                }
                String[] trimmedAttendanceList = sorted.toArray(new String[0]);


                // Loop through the array and log its contents to get the number of students in that section
                int i = 0;
                int finalI = 0;
                final String[] studentCounts = {"0"};
                db.collection("sections").whereEqualTo("professorId", currentId).whereEqualTo("sectionName", selectedSection).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                studentCounts[0] = document.getString("studentsCount");
                            }
                        } else {
                            Log.d("TAGSSSS", "Error getting documents: ", task.getException());
                        }

                        Log.d("TAGSSsSS", String.valueOf(studentCounts[0]), task.getException());//the value appears here

                        final int[] j = {0};
                        String value = studentCounts[0];
                        int intValue = Integer.parseInt(value);

                        final String[] intValue2 = new String[1];
                        db.collection("sections").whereEqualTo("professorID", currentId).whereEqualTo("sectionName", selectedSection).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        // Get the studentsCount field
                                        intValue2[0] = document.getString("studentsCount");
                                        if (intValue2[0] != null) {
                                            // Do something with the studentsCount, such as displaying it
                                            Log.d("StudentsCount", "Students Count: " + intValue2[0]);
                                            Log.d("StudentsCount", "Students Count2: " + intValue);
                                        } else {
                                            Log.d("StudentsCount", "studentsCount is null for document: " + document.getId());
                                        }
                                    }


                                    ArrayList<String> formattedDataList = new ArrayList<>();
                                    for (String name : trimmedAttendanceList) {
//                                      if (name != null) {
                                        Log.d("before attendance List", name);
                                        //get the number of students per section
                                        int intValue3 = Integer.parseInt(intValue2[0]);
                                        int remaining = Math.subtractExact(intValue3, countsArray[j[0]]);

                                        String formattedData = (j[0] + 1) + ". " + trimmedAttendanceList[j[0]];
                                        formattedDataList.add(formattedData);
                                        Log.d("after attendance List", name);
                                        j[0]++;
//                            }
                                    }

                                    allCountMeetings = String.valueOf(formattedDataList.size());

                                    //loading to the listview
                                    // Create an ArrayAdapter and set it to your ListView
                                    ArrayAdapter<String> adapter = new ArrayAdapter<>(ScannedAttendanceActivity.this, android.R.layout.simple_list_item_1, formattedDataList);
                                    attendanceListView.setAdapter(adapter);


                                    //adding a onClickListener
                                    attendanceListView.setOnItemClickListener((adapterView, view, position, id) -> {

                                        Log.d("attendaceDAte", trimmedAttendanceList[position]);

                                        db.collection("subjects")
                                                .whereEqualTo("professorID", currentId)
                                                .whereEqualTo("sectionName", selectedSection)
                                                .whereEqualTo("subjectName", selectedSubject)
                                                .get()
                                                .addOnCompleteListener(task2 -> {
                                                    if (task2.isSuccessful()) {
                                                        for (QueryDocumentSnapshot document : task2.getResult()) {
                                                            // Access the 'studentsCount' field from each document
                                                            String studentsCount44 = document.getString("studentsCount");

                                                            // Do something with the 'studentsCount'
                                                            System.out.println("Final Students Count: " + studentsCount44);
                                                            if (selectedSubject != null) {
                                                                Intent intent = new Intent(ScannedAttendanceActivity.this, ScannedAttendeesActivity.class);
                                                                intent.putExtra("startDate", startDate);
                                                                intent.putExtra("endDate", endDate);
                                                                intent.putExtra("selectedSection", selectedSection);
                                                                intent.putExtra("selectedSubject", selectedSubject);
                                                                intent.putExtra("sectionStudentsCount", studentsCount44);
                                                                intent.putExtra("selectedDate", trimmedAttendanceList[position]);
                                                                intent.putExtra("currentId", currentId);
                                                                startActivity(intent);
                                                            }
                                                        }
                                                    } else {
                                                        // Handle errors
                                                        Exception exception = task2.getException();
                                                        if (exception != null) {
                                                            exception.printStackTrace();
                                                        }
                                                    }
                                                });


                                    });

                                    studentsBtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent intent = new Intent(ScannedAttendanceActivity.this, ScannedStudentsActivity.class);
                                            intent.putExtra("startDate", startDate);
                                            intent.putExtra("endDate", endDate);
                                            intent.putExtra("selectedSection", selectedSection);
                                            intent.putExtra("selectedSubject", selectedSubject);
                                            intent.putExtra("meetingsCount", String.valueOf(formattedDataList.size()));
                                            intent.putExtra("currentId", currentId);
                                            startActivity(intent);
                                        }
                                    });
                                } else {
                                    Log.d("FirestoreQuery", "Error getting documents: ", task.getException());
                                }
                            }
                        });


                    }
                });





                //getting names of the students that attended
                //getting the list of meetings/attendance
                final String[] attendeesList = new String[1000];
                final String[] attendeesIDList = new String[1000];

                db.collection("scannedData").whereEqualTo("professorId", currentId).whereEqualTo("subject", selectedSubject).whereEqualTo("section", selectedSection).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        int presentCount = 0;
                        if (task.isSuccessful()) {
                            int index = 0;
                            Set<String> uniqueDatesSet = new HashSet<>();
                            Set<String> uniqueNamesSet = new HashSet<>();
                            Set<String> uniqueUserIdsSet = new HashSet<>();

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Assuming startDate and endDate are in the format "yyyy-MM-dd"
                                android.icu.text.SimpleDateFormat dateFormat = new android.icu.text.SimpleDateFormat("yyyy-MM-dd");
                                String documentDateString = document.getString("date");

                                try {
                                    Date documentDate = dateFormat.parse(documentDateString);
                                    Date startDate = dateFormat.parse(intent.getStringExtra("startDate"));
                                    Date endDate = dateFormat.parse(intent.getStringExtra("endDate"));

                                    // Check if the document date is between the start and end dates
                                    if (documentDate.equals(startDate) || documentDate.after(startDate) && documentDate.before(endDate) || documentDate.equals(endDate)) {
                                        // The document date is within the range
                                        Log.d("DateCheckAttendance", "Date is within the range");

                                        String name = document.getString("name");
                                        String userId = document.getString("date");
                                        String date = document.getString("date");

                                        // Check if the date is not in the set (i.e., it's unique)
                                        if (!uniqueDatesSet.contains(date)) {
                                            presentCount++;  // Increment presentCount only for unique dates
                                            uniqueDatesSet.add(date);  // Add the date to the set to mark it as seen
                                        }


                                        // Check if the name is not in the set (i.e., it's unique)
                                        if (!uniqueNamesSet.contains(name)) {
                                            attendeesList[index] = name;
                                            uniqueNamesSet.add(name);  // Add the name to the set to mark it as seen

                                            Log.d("testss" + index, "Attendance Member: " + name);
                                            index++; // Increment index
                                        }

                                        // Check if the userId is not in the set (i.e., it's unique)
                                        if (!uniqueUserIdsSet.contains(userId)) {
                                            attendeesIDList[index] = userId;
                                            uniqueUserIdsSet.add(userId);  // Add the userId to the set to mark it as seen
                                        }

                                        Log.d("testss" + index, "Attendance Member ID: " + userId);
                                    }
                                } catch (ParseException e) {
                                    throw new RuntimeException(e);
                                }
                            }//end of loop
                        } else {
                            Log.d("testss", "Error getting documents: ", task.getException());
                        }


                        //Trim the attendanceList Array to remove unnecessary indexes
                        // Create a list to store non-null elements
                        List<String> nonNullList = new ArrayList<>();
                        for (String item : attendeesList) {
                            if (item != null) {
                                nonNullList.add(item);
                            }
                        }
                        String[] trimmedAttendeesList = nonNullList.toArray(new String[0]);

                        List<String> nonNullList2 = new ArrayList<>();
                        for (String item : attendeesIDList) {
                            if (item != null) {
                                nonNullList2.add(item);
                            }
                        }
                        String[] trimmedAttendeesDateList = nonNullList2.toArray(new String[0]);


                        // Loop through the array and log its contents to check
                        int i = 0;
                        int absentCount = Integer.parseInt(meetingsCount) - presentCount;


                        exportBtn.setOnClickListener(new View.OnClickListener() {
                            int count = 0;

                            @Override
                            public void onClick(View view) {
                                for (Object item : trimmedAttendanceList) {
                                    Log.d("Attendance Dates", item.toString());
                                    count++;
                                }
                                for (Object item : trimmedAttendeesList) {
                                    Log.d("Attendance Students", item.toString());
                                }


                                // Assuming trimmedAttendanceList and trimmedAttendeesList have the same length
                                Log.d("TAGTAG", "onClick: " + trimmedAttendanceList.length + " " + trimmedAttendeesList.length);
                                String[][] attendance = new String[trimmedAttendeesList.length][trimmedAttendanceList.length];

                                for (int i = 0; i < trimmedAttendeesList.length; i++) {
                                    for (int j = 0; j < trimmedAttendanceList.length; j++) {
                                        Log.d("AttendanceValue-Before", "Student: " + trimmedAttendeesList[i] + ", Date: " + trimmedAttendanceList[j] + ", Value: " + attendance[i][j]);
                                    }
                                }

// Sort the array alphabetically
                                Arrays.sort(trimmedAttendeesList);

// Now, trimmedAttendanceList is sorted alphabetically
                                for (String name : trimmedAttendeesList) {
                                    System.out.println("soratation"+name);
                                }

                                for (int i = 0; i < trimmedAttendanceList.length; i++) {

                                    String dateToCheck0 = trimmedAttendanceList[i];
                                    String dateToCheck = DateConverter.convertDate(dateToCheck0);
                                    Log.d("TAGTAG", "onClick: " + dateToCheck);

                                    for (int j = 0; j < trimmedAttendeesList.length; j++) {
                                        String studentToCheck = trimmedAttendeesList[j];

                                        Log.d("Date to Check", dateToCheck);
                                        Log.d("Name to Check", studentToCheck);
                                        // Query Firestore to check if the student is present on the specific date
                                        int finalJ = j;
                                        int finalI1 = i;
                                        db.collection("scannedData").whereEqualTo("professorId", currentId).whereEqualTo("section", selectedSection).whereEqualTo("subject", selectedSubject).whereEqualTo("date", dateToCheck).whereEqualTo("name", studentToCheck).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    // Check if any documents match the query
                                                    Log.d("TAGTAG2", "onClick: " + dateToCheck);
                                                    boolean isPresent = !task.getResult().isEmpty();
                                                    // The student is present on the specific date
                                                    Log.d("PresenceCheck", studentToCheck + " is " + (isPresent ? "present" : "absent") + " on " + dateToCheck);
                                                    // Set the value in the attendance array
//                                                    attendance[finalJ][finalI1] = isPresent;

                                                    // ...

                                                    db.collection("scannedData")
                                                            .whereEqualTo("professorId", currentId)
                                                            .whereEqualTo("section", selectedSection)
                                                            .whereEqualTo("subject", selectedSubject)
                                                            .whereEqualTo("date", dateToCheck)
                                                            .whereEqualTo("name", studentToCheck)
                                                            .get()
                                                            .addOnCompleteListener(task2 -> {
                                                                if (task2.isSuccessful()) {
                                                                    for (QueryDocumentSnapshot document : task2.getResult()) {
                                                                        // Get the status field from the Firestore document
                                                                        String status = document.getString("status");
                                                                        if (status != null) {
                                                                            attendance[finalJ][finalI1] = status;
                                                                            Log.d("StatusCheck", "Status: " + status);
                                                                        }
                                                                    }
                                                                } else {
                                                                    Log.d("FirestoreQuery", "Error getting documents: " + task2.getException());
                                                                }


                                                                for (int i = 0; i < trimmedAttendeesList.length; i++) {
                                                                    for (int j = 0; j < trimmedAttendanceList.length; j++) {
                                                                        Log.d("AttendanceValue-After", "Student: " + trimmedAttendeesList[i] + ", Date: " + trimmedAttendanceList[j] + ", Value: " + attendance[i][j]);
                                                                    }
                                                                }


                                                                String dateRange = startDate + " to " + endDate;
                                                                String attendanceReportJson = exportAttendanceReport(attendance, trimmedAttendanceList,trimmedAttendeesList, selectedSection, selectedSubject, dateRange);
                                                                Log.d("AttendanceReport", attendanceReportJson);
                                                            });

// ...


                                                } else {
                                                    Log.d("PresenceCheck", "Error getting documents: ", task.getException());
                                                }


//                                                for (int i = 0; i < trimmedAttendeesList.length; i++) {
//                                                    for (int j = 0; j < trimmedAttendanceList.length; j++) {
//                                                        Log.d("AttendanceValue-After", "Student: " + trimmedAttendeesList[i] + ", Date: " + trimmedAttendanceList[j] + ", Value: " + attendance[i][j]);
//                                                    }
//                                                }
//
//                                                String dateRange = startDate + " to " + endDate;
//                                                String attendanceReportJson = exportAttendanceReport(attendance, trimmedAttendanceList,trimmedAttendeesList, selectedSection, selectedSubject, dateRange);
//                                                Log.d("AttendanceReport", attendanceReportJson);
//                                                                        Toast.makeText(ScannedAttendanceActivity.this, "Loading Data", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }
                            }
                        });
                    }
                });
            }
        });


    }


    public String exportAttendanceReport(String[][] attendance, String[] trimmedAttendanceList, String[] trimmedAttendeesList, String selectedSection, String selectedSubject, String dateRange) {


        List<String> attendyStudents = Arrays.asList(trimmedAttendeesList);

        if (trimmedAttendanceList != null) {
            Log.d("FINAL Trimmed Attendance List:", Arrays.toString(trimmedAttendanceList));
        } else {
            Log.d("FINAL Trimmed Attendance List:", "Array is null");
        }

        if (trimmedAttendeesList != null) {
            Log.d("FINAL Trimmed Attendees List:", Arrays.toString(trimmedAttendeesList));
        } else {
            Log.d("FINAL Trimmed Attendees List:", "Array is null");
        }


        JSONArray attendanceReportArray = new JSONArray();

//        Random random = new Random();
//        int randomNumber = random.nextInt();
        try {
            if (trimmedAttendanceList != null && trimmedAttendeesList != null) {
                int attendeesCount = trimmedAttendeesList.length;
                int attendanceListCount = trimmedAttendanceList.length;

                Log.d("Attendees Count:", String.valueOf(attendeesCount));
                for (String value : trimmedAttendeesList) {
                    Log.d("Attendee Value:", value);
                }
                Log.d("Attendance List Count:", String.valueOf(attendanceListCount));


                for (int i = 0; i < attendeesCount; i++) {
                    JSONObject attendanceDetails = new JSONObject();

                    attendanceDetails.put("No", i+1);
                    attendanceDetails.put(selectedSection + "-" + selectedSubject + "(" + dateRange + ")", trimmedAttendeesList[i]);

                    int presentCount = 0;
                    int absentCount = 0;
                    int lateCount = 0;
                    int excuseCount = 0;
                    int numbering = 0;
                    for (int j = 0; j < attendanceListCount; j++) {
                        if (i < attendeesCount && j < attendanceListCount) {
                            Log.d("loggings for i and j", "I: "+i+ " J: " + j + "Status: "+attendance[i][j]);
                            if(attendance[i][j]==null){
                                attendance[i][j]="Absent";
                            }
                            attendanceDetails.put(trimmedAttendanceList[j], attendance[i][j]);

                            //check the counts
                            String value = attendance[i][j];
                            if (value != null && !value.isEmpty()) {
                                // Check the condition for counting
                                if (value.startsWith("p") || value.startsWith("P")) {
                                    presentCount++;
                                } else if (value.startsWith("a") || value.startsWith("A")) {
                                    absentCount++;
                                } else if (value.startsWith("e") || value.startsWith("E")) {
                                    excuseCount++;
                                }else if (value.startsWith("l") || value.startsWith("L")) {
                                    lateCount++;
                                }
                            }
                        } else {
                            Log.d("Errors", "something went wrong");
                        }
                    }
//                    trimmedAttendeesList[i]
                    attendanceDetails.put("MeetingsNo", allCountMeetings);
                    attendanceDetails.put("Present", presentCount);
                    attendanceDetails.put("Absent", absentCount);
                    attendanceDetails.put("Late", lateCount);
                    attendanceDetails.put("Excuse", excuseCount);



                    attendanceReportArray.put(attendanceDetails);
                }
            }


            // Create empty rows
            JSONObject emptyRow = new JSONObject();

            for (int i = 0; i < 3; i++) {
//                JSONObject emptyRow = new JSONObject();
                emptyRow.put("-", "");
                for (String date : trimmedAttendanceList) {
                    emptyRow.put(date, "");
                }
                attendanceReportArray.put(emptyRow);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


        //json String to be put as a table in Excel
        String jsonString = attendanceReportArray.toString();


        try {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                // Create a workbook, sheet, and a row for the header
                HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
                HSSFSheet hssfSheet = hssfWorkbook.createSheet();
                HSSFRow headerRow = hssfSheet.createRow(0);

                // Get the headers from the first object in the JSON array
                JSONObject firstObject = attendanceReportArray.getJSONObject(0);
                Iterator<String> keys = firstObject.keys();

                // Create header cells
                int colIndex = 0;

// Create header cells
//                int colIndex = 0;

                HSSFRow headerRow1 = hssfSheet.createRow(0);
                HSSFCell headerCell1 = headerRow1.createCell(colIndex++);
                headerCell1.setCellValue("Summary of Attendance for");

                HSSFCell headerCell2 = headerRow1.createCell(colIndex++);
                SharedPreferences prefs = getSharedPreferences("dateTypePreference", MODE_PRIVATE);
                String dateType = prefs.getString("dateType", "DEFAULT_VALUE");

                headerCell2.setCellValue(dateType);

                HSSFRow headerRow2 = hssfSheet.createRow(1);
                colIndex = 0;

                HSSFCell headerCellCol1 = headerRow2.createCell(colIndex++);
                headerCellCol1.setCellValue("Year and Section:");

                HSSFCell headerCellCol2 = headerRow2.createCell(colIndex++);
                headerCellCol2.setCellValue(selectedSection);

                HSSFRow headerRow3 = hssfSheet.createRow(2);
                colIndex = 0;

                HSSFCell headerCellCol3 = headerRow3.createCell(colIndex++);
                headerCellCol3.setCellValue("Subject:");

                HSSFCell headerCellCol4 = headerRow3.createCell(colIndex++);
                headerCellCol4.setCellValue(selectedSubject);

                HSSFRow headerRow4 = hssfSheet.createRow(3);
                colIndex = 0;

                HSSFCell headerCellCol5 = headerRow4.createCell(colIndex++);
                headerCellCol5.setCellValue("Date Range: ");

                HSSFCell headerCellCol6 = headerRow4.createCell(colIndex++);
                headerCellCol6.setCellValue(dateRange);



                //for the table header
                HSSFRow tableHeaderRow = hssfSheet.createRow(5); // Assuming the header takes the first 4 rows, adjust if needed
                colIndex = 0;
                HSSFCell dataCelld = tableHeaderRow.createCell(colIndex++);
                dataCelld.setCellValue("No.");

                colIndex = 1;
                HSSFCell dataCell45 = tableHeaderRow.createCell(colIndex++);
                dataCell45.setCellValue("Student's Name");

                colIndex = 2;
                for (String value : trimmedAttendanceList) {
                    HSSFCell dataCell = tableHeaderRow.createCell(colIndex++);
                    dataCell.setCellValue(value);
                }
                HSSFCell dataCell = tableHeaderRow.createCell(colIndex);
                dataCell.setCellValue("No. of Meetings");
                HSSFCell dataCellz = tableHeaderRow.createCell(colIndex+1);
                dataCellz.setCellValue("No. of Present");
                HSSFCell dataCella = tableHeaderRow.createCell(colIndex+2);
                dataCella.setCellValue("No. of Absent");
                HSSFCell dataCellab = tableHeaderRow.createCell(colIndex+3);
                dataCellab.setCellValue("No. of Late");
                HSSFCell dataCellaba = tableHeaderRow.createCell(colIndex+4);
                dataCellaba.setCellValue("No. of Excuse");


                // Populate the sheet with data table
                for (int i = 0; i < attendanceReportArray.length(); i++) {
                    JSONObject jsonObject = attendanceReportArray.getJSONObject(i);


                    // Populate the cells with data
                    colIndex = 0;
                    HSSFRow dataRow = hssfSheet.createRow(i + 6);
                    keys = jsonObject.keys();

                    while (keys.hasNext()) {
                        String key = keys.next();
                        HSSFCell dataCell2 = dataRow.createCell(colIndex++);
                        dataCell2.setCellValue(jsonObject.getString(key));

                        Log.d("Lates", "Row: "+dataRow);
                        Log.d("Lates", "Col: "+colIndex);
                    }
                }


// Save the workbook to a file
                File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                String fileName = "Attendance record for " + selectedSubject + "-" + selectedSection + " (" + dateRange + ").xls";
                File filePath = new File(downloadsFolder, fileName);
                try {

                    if (!filePath.exists()) {
                        // Check if the app doesn't have WRITE_EXTERNAL_STORAGE permission
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            // Request the permission
                            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                        } else {
                            // You have the permission, proceed with file creation
//                            createFile();
                            filePath.createNewFile(); //this is giving java.io.IOException: Permission denied
                        }


                    }

                    FileOutputStream fileOutputStream = new FileOutputStream(filePath);
                    hssfWorkbook.write(fileOutputStream);

                    if (fileOutputStream != null) {
                        fileOutputStream.flush();
                        fileOutputStream.close();
                        Log.d("ExportExcel", "Export successful");
                        Toast.makeText(getApplicationContext(), "Exported Successfully", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e("ExportExcel", "Error exporting attendance report", e);
                    Toast.makeText(getApplicationContext(), "Error exporting attendance report", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }


            } else {
                // Handle the situation where external storage is not available
                Log.d("ExportExcel", "exportAttendanceReport: NOT AVAILABLE STORAGE");
                Toast.makeText(getApplicationContext(), "External storage not available", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        Log.d("ExportExcel", "exportAttendanceReport: OUTSIDE");
        return attendanceReportArray.toString();
    }


}