package com.example.qrcodes;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScannedAttendeesActivity extends AppCompatActivity {

    private String selectedSection;
    private String selectedSubject;
    private String selectedDate, sectionStudentsCount;
    private String currentId;
    String formattedDate;
    TextView subtitle;
    ListView attendeesListView;
    FirebaseFirestore db;
    String startDate, endDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanned_attendees);

        db = FirebaseFirestore.getInstance();
        subtitle = findViewById(R.id.subtitle4);
        attendeesListView = findViewById(R.id.attendeesListView);

        // Retrieve the values passed via Intent
        Intent intent = getIntent();
        if (intent != null) {
            startDate = intent.getStringExtra("startDate");
            endDate = intent.getStringExtra("endDate");
            sectionStudentsCount = intent.getStringExtra("sectionStudentsCount");
            selectedSection = intent.getStringExtra("selectedSection");
            selectedSubject = intent.getStringExtra("selectedSubject");
            selectedDate = intent.getStringExtra("selectedDate");
            currentId = intent.getStringExtra("currentId");

            Log.d("LogTags", "Start Date: " + startDate);
            Log.d("LogTags", "End Date: " + endDate);
        }


        Log.d("StartDate", "Start Date: " + startDate);
        Log.d("EndDate", "End Date: " + endDate);
        Log.d("SelectedSection", "Selected Section: " + selectedSection);
        Log.d("SelectedSubject", "Selected Subject: " + selectedSubject);
        Log.d("SelectedDate", "Selected Date: " + selectedDate);
        Log.d("CurrentId", "Current Id: " + currentId);


        String inputDateStr = selectedDate; // Input date string
        String outputDateFormat = "yyyy-MM-dd"; // Desired output format

        try {
            // Parse the input date string
            SimpleDateFormat inputFormat = new SimpleDateFormat("MMMM dd, yyyy");
            Date date = inputFormat.parse(inputDateStr);

            // Format the date in the desired output format
            SimpleDateFormat outputFormat = new SimpleDateFormat(outputDateFormat);
            formattedDate = outputFormat.format(date);

            System.out.println(formattedDate); // Output: 2023-09-25
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Log.d("checkDate!", "Unfromatted Date: " + selectedDate);
        Log.d("checkDate!", "Formatted Date: " + formattedDate);


        //getting names of the students that attended
        //getting the list of meetings/attendance
        final String[] attendeesList = new String[1000];
        final String[] attendeesIDList = new String[1000];

        db.collection("scannedData")
                .whereEqualTo("professorId", currentId)
                .whereEqualTo("date", formattedDate)
                .whereEqualTo("section", selectedSection)
                .whereEqualTo("subject", selectedSubject) //these 2 are commented before. and without them, there is no code to filter na dapat student from
                .get()                          // x section and y subject should be chosen to appear in the listview
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int index = 0;
                            Log.d("LoggingsAttendeed", formattedDate);

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                attendeesList[index] = document.getString("name");
                                attendeesIDList[index] = document.getString("userId");

                                Log.d("LoggingsAttendeed" + index, "Attendance Member: "
                                        + attendeesList[index] + " - " + attendeesIDList[index]);

                                index++; // Increment index
                            }
                        }


                        //Trim the attendanceList Array to remove unnecessary indexes
                        // Create a list to store non-null elements
                        List<String> nonNullList = new ArrayList<>();
                        for (String item : attendeesList) {
                            if (item != null) {
                                nonNullList.add(item);
                            }
                        }
                        String[] trimmedAttendanceList = nonNullList.toArray(new String[0]);
                        String[] trimmedAttendanceList2 = nonNullList.toArray(new String[0]);
                        //making them alphabetically ordered
                        Arrays.sort(trimmedAttendanceList);
                        Arrays.sort(trimmedAttendanceList2);

                        List<String> nonNullList2 = new ArrayList<>();
                        for (String item : attendeesIDList) {
                            if (item != null) {
                                nonNullList2.add(item);
                            }
                        }
                        String[] trimmedAttendanceIDList = nonNullList2.toArray(new String[0]);


                        // Loop through the array and log its contents to check
                        int i = 0;
                        for (String name : trimmedAttendanceList2) {
                            Log.d("before edit", name + '-' + trimmedAttendanceList[i]); // Use your desired tag (e.g., "YourTag") and log level (e.g., Log.d)

                            int finalI = i;
                            db.collection("scannedData")
                                    .whereEqualTo("professorId", currentId)
                                    .whereEqualTo("date", formattedDate)
                                    .whereEqualTo("section", selectedSection)
                                    .whereEqualTo("subject", selectedSubject)
                                    .whereEqualTo("name", trimmedAttendanceList[i])
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                    // Retrieve the "status" field
                                                    String status = document.getString("status");

                                                    if (status != null) {
                                                        // Log or use the status value
                                                        Log.d("Status", "Status for " + trimmedAttendanceList[finalI] + ": " + status);

                                                        trimmedAttendanceList2[finalI] = (finalI + 1) + ". " + trimmedAttendanceList[finalI] + " ("+status+")";
                                                        // Perform any further actions with the status value
                                                    } else {
                                                        // Handle the case where 'status' is null
                                                        Log.d("Status", "Status is null for " + trimmedAttendanceList[finalI]);
                                                    }
                                                }


                                                ArrayAdapter<String> adapter = new ArrayAdapter<>(ScannedAttendeesActivity.this, android.R.layout.simple_list_item_1, trimmedAttendanceList2);
                                                attendeesListView.setAdapter(adapter);
                                            } else {
                                                Log.e("FirestoreQuery", "Error getting documents: ", task.getException());
                                            }
                                        }

                                    });

                            i++;
                        }



//                        trimmedAttendanceList2.length();
                        Toast.makeText(ScannedAttendeesActivity.this, "Attendance: " + String.valueOf(trimmedAttendanceList2.length), Toast.LENGTH_SHORT).show();

                        int absentCount = Integer.parseInt(sectionStudentsCount) - Integer.parseInt(String.valueOf(trimmedAttendanceList2.length));

                        subtitle.setText(selectedSection + " \u2192 " + selectedSubject + " \u2192 " + selectedDate +
                                " (Present: " + trimmedAttendanceList2.length +
                                " | Absent: " + absentCount+")");

                        attendeesListView.setOnItemClickListener((adapterView, view, position, id) -> {
//                            String selectedPosition = adapter.getItem(position);
                            Log.d("testss", trimmedAttendanceList[position]);
                            Log.d("testss", trimmedAttendanceIDList[position]);

                            AlertDialog.Builder builder = new AlertDialog.Builder(ScannedAttendeesActivity.this);
                            builder.setTitle("Choose an action");

                            // Add "Delete" button
                            builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
//                                    String userIdToDelete = trimmedAttendanceIDList[position];
                                    String userIdToDelete = trimmedAttendanceList[position];
                                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                                    DocumentReference documentReference = db.collection("scannedData").document(userIdToDelete);

                                    db.collection("scannedData")
                                            .whereEqualTo("professorId", currentId)
                                            .whereEqualTo("section", selectedSection)
                                            .whereEqualTo("subject", selectedSubject)
                                            .whereEqualTo("date", formattedDate)
                                            .whereEqualTo("name", userIdToDelete)
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                                            // Access the document ID
                                                            String documentId = document.getId();
                                                            Log.d("sadfsaf", "onComplete: " + documentId);

                                                            deleteDocument(documentId);
                                                        }
                                                    } else {
                                                        Log.d("TAG", "Error getting documents: ", task.getException());
                                                    }
                                                }
                                            });


                                }
                            });

                            // Add "Show Student Record" button
                            builder.setNegativeButton("Show Student Record", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    if (selectedSubject != null) {
                                        Intent intent = new Intent(ScannedAttendeesActivity.this, ScannedStudentRecord.class);
                                        intent.putExtra("startDate", startDate);
                                        intent.putExtra("endDate", endDate);
                                        intent.putExtra("selectedSection", selectedSection);
                                        intent.putExtra("selectedSubject", selectedSubject);
                                        intent.putExtra("selectedDate", selectedDate);
                                        intent.putExtra("selectedDateFormatted", formattedDate);
                                        intent.putExtra("selectedUser", trimmedAttendanceList[position]);
                                        intent.putExtra("selectedUserID", trimmedAttendanceIDList[position]);
                                        intent.putExtra("currentId", currentId);
                                        startActivity(intent);
                                    }
                                }
                            });

                            // Create and show the AlertDialog
                            builder.create().show();
                        });
                    }
                });


        //NOW THE ADD BUTTON IN THE DEEPEST PART OF THE VIEW
        AutoCompleteTextView autoCompleteTextView = findViewById(R.id.spinnerWithFilter);

        // Sample data for the dropdown
        String[] items = {"Item 1", "Item 2", "Item 3", "Item 4", "Item 5"};

        // Getting the names of the students to be added to the autoCompleteTextView
        int maxDocuments = 1000;
        String[][][] userDataArray = new String[maxDocuments][3][3]; // Assuming 3 fields: name, number, documentId


        List<String> enrolledList = new ArrayList<>();

        db.collection("students")
                .whereEqualTo("professorId", currentId)
                .whereEqualTo("section", selectedSection)
                .whereEqualTo("subject", selectedSubject)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Access the 'name' field from each document
                            String name = document.getString("name");

                            // Add the 'name' to the list
                            if (name != null) {
                                enrolledList.add(name);
                            }
                        }

                        // Now 'namesList' contains all the names that meet the conditions
                        for (String name : enrolledList) {
                            System.out.println("enrolledList: " + name);
                        }



                        db.collection("User")
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            int index = 0;
                                            List<String> namesList = new ArrayList<>();

                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                // Get values
                                                String name = document.getString("name");
                                                String email = document.getString("number");

                                                if(enrolledList.contains(name)){
                                                    // Get document ID
                                                    String documentId = document.getId();

                                                    // Store data in the array
                                                    userDataArray[index][0][0] = name;
                                                    userDataArray[index][1][0] = email;
                                                    userDataArray[index][2][0] = documentId;

                                                    // Use the retrieved data as needed
                                                    namesList.add(name);
                                                    Log.d("FirestoreStudentsData", "Document ID: " + documentId + ", Name: " + name + ", Email: " + email);

                                                    // Increment index for the next iteration
                                                    index++;
                                                }

                                            }

                                            // Create ArrayAdapter
                                            ArrayAdapter<String> adapter = new ArrayAdapter<>(ScannedAttendeesActivity.this,
                                                    android.R.layout.simple_dropdown_item_1line, namesList);
                                            // Set the adapter to the AutoCompleteTextView
                                            autoCompleteTextView.setAdapter(adapter);


                                            //getting the selected names, id, and email
                                            final String[] selectedName = new String[1];
                                            final String[] selectedDocumentId = new String[1];
                                            final String[] selectedEmail = new String[1];
                                            autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                @Override
                                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                    // Get the selected name from the adapter
                                                    selectedName[0] = (String) parent.getItemAtPosition(position);

                                                    // Find the corresponding document ID and number from the userDataArray
                                                    for (int i = 0; i < userDataArray.length; i++) {
                                                        if (userDataArray[i][0][0].equals(selectedName[0])) {
                                                            selectedDocumentId[0] = userDataArray[i][2][0];
                                                            selectedEmail[0] = userDataArray[i][1][0];
                                                            break;  // Exit the loop once a match is found
                                                        }
                                                    }

                                                    // Now you have the selected values in the variables
                                                    Log.d("SelectedData", "Name: " + selectedName[0]);
                                                    Log.d("SelectedData", "Document ID: " + selectedDocumentId[0]);
                                                    Log.d("SelectedData", "Number: " + selectedEmail[0]);
                                                }
                                            });

                                            Button addStudentsButton = findViewById(R.id.addStudentBtn);
                                            addStudentsButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {

                                                    //create the textview for getting the reason

                                                    AlertDialog.Builder builder = new AlertDialog.Builder(ScannedAttendeesActivity.this);
                                                    builder.setTitle("Add Excuse");
                                                    EditText input = new EditText(ScannedAttendeesActivity.this);
                                                    input.setText("Excuse - ");
                                                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                                                    builder.setView(input);

                                                    // Set up the buttons
                                                    builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            // Get the entered text when the "Save" button is clicked
                                                            String excuseText = input.getText().toString().trim();


                                                            if (!TextUtils.isEmpty(selectedName[0]) && !TextUtils.isEmpty(selectedDocumentId[0]) && !TextUtils.isEmpty(selectedEmail[0])) {
                                                                // Variables are not empty, you I can now push them to the scannedDAta Collection
                                                                String name = selectedName[0];
                                                                String documentId = selectedDocumentId[0];
                                                                String email = selectedEmail[0];
                                                                String subject = selectedSubject;
                                                                String section = selectedSection;
                                                                String professorId = currentId;
                                                                String date = formattedDate;

                                                                // Create a Map to store the data
                                                                Map<String, Object> scannedData = new HashMap<>();
                                                                scannedData.put("date", date);
                                                                scannedData.put("id", email);
                                                                scannedData.put("name", name);
                                                                scannedData.put("professorId", professorId);
                                                                scannedData.put("section", section);
                                                                scannedData.put("status", excuseText);
                                                                scannedData.put("subject", subject);
                                                                scannedData.put("userId", documentId);

                                                                db.collection("scannedData")
                                                                        .add(scannedData)
                                                                        .addOnSuccessListener(documentReference -> {
                                                                            // Document added successfully
                                                                            String addedDocumentId = documentReference.getId();
                                                                            Log.d("Firestore", "Document added with ID: " + addedDocumentId);
                                                                        })
                                                                        .addOnFailureListener(e -> {
                                                                            // Handle the error
                                                                            Log.e("Firestore", "Error adding document", e);
                                                                        });


                                                                Log.d("SelectedDataBtn2", "DATA PUSH SUCCESSFUL!");
                                                                Log.d("SelectedDataBtn2", "Name: " + selectedName[0]);
                                                                Log.d("SelectedDataBtn2", "Document ID: " + selectedDocumentId[0]);
                                                                Log.d("SelectedDataBtn2", "Number: " + selectedEmail[0]);
                                                                Log.d("SelectedDataBtn2", "Subject: " + selectedSubject);
                                                                Log.d("SelectedDataBtn2", "Section: " + selectedSection);
                                                                Log.d("SelectedDataBtn2", "Professor ID: " + currentId);
                                                                Log.d("SelectedDataBtn2", "Date: " + formattedDate);

                                                                reloadActivity();
                                                            } else {
                                                                // At least one of the variables is empty
                                                                Log.d("SelectedDataBtn", "One or more variables are empty");
                                                                Toast.makeText(ScannedAttendeesActivity.this, "Select a student to add.", Toast.LENGTH_SHORT).show();
                                                            }

                                                        }
                                                    });

                                                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            dialog.cancel(); // Cancel the dialog when "Cancel" is clicked
                                                        }
                                                    });
                                                    builder.show();


                                                }
                                            });

                                        } else {
                                            Log.d("FirestoreData", "Error getting documents: ", task.getException());
                                        }
                                    }
                                });
                    } else {
                        // Handle errors
                        Exception exception = task.getException();
                        if (exception != null) {
                            exception.printStackTrace();
                        }
                    }
                });




        //delete duplicate files
        FirebaseFirestore db = FirebaseFirestore.getInstance();

// Define the collection and fields to check for duplicates
        String collectionPath = "scannedData";
        String[] fieldsToCheck = {"date", "id", "name", "professorId", "section", "subject", "userId"};

// Use a Map to keep track of seen values
        Map<String, Boolean> seenValues = new HashMap<>();

        db.collection(collectionPath)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Concatenate values of specified fields
                            StringBuilder keyBuilder = new StringBuilder();
                            for (String field : fieldsToCheck) {
                                Object value = document.get(field);
                                keyBuilder.append(value != null ? value.toString() : "").append("-");
                            }
                            String key = keyBuilder.toString();

                            // Check if the key is already seen
                            if (seenValues.containsKey(key)) {
                                // Duplicate found, delete the document
                                String documentId = document.getId();
                                deleteDocument(collectionPath, documentId);
                            } else {
                                // Mark the key as seen
                                seenValues.put(key, true);
                            }
                        }
                    } else {
                        // Handle errors
                        Exception e = task.getException();
                        if (e != null) {
                            e.printStackTrace();
                        }
                    }
                });

    }

    // Function to delete a document
    private void deleteDocument(String collectionPath, String documentId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(collectionPath).document(documentId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Document successfully deleted
                    System.out.println("Document deleted: " + documentId);
                    Toast.makeText(this, "A duplicate record was deleted", Toast.LENGTH_SHORT).show();
                });

    }

    public void reloadActivity() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    private void deleteDocument(String documentId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("scannedData")
                .document(documentId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("TAG", "DocumentSnapshot successfully deleted: " + documentId);
                        Toast.makeText(ScannedAttendeesActivity.this, "Record successfuly deleted.", Toast.LENGTH_SHORT).show();

                        reloadActivity();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("TAG", "Error deleting document", e);
                        // Handle the error if deletion fails
                    }
                });
    }

}
