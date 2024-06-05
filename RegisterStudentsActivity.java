package com.example.qrcodes;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

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

public class RegisterStudentsActivity extends AppCompatActivity {

    private String selectedSection;
    private String selectedSubject, studentsCount;
    //    private String sectionStudentsCount;
    private String currentId;

    TextView subtitle, title;
    ListView attendeesListView;
    List nameeee;
    FirebaseFirestore db;
//    String startDate, endDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanned_attendees);

        db = FirebaseFirestore.getInstance();
        subtitle = findViewById(R.id.subtitle4);
        title = findViewById(R.id.title);
        title.setText("LIST OF STUDENTS");


        attendeesListView = findViewById(R.id.attendeesListView);

        // Retrieve the values passed via Intent
        Intent intent = getIntent();
        if (intent != null) {
            selectedSection = intent.getStringExtra("selectedSection");
            selectedSubject = intent.getStringExtra("selectedSubject");
            currentId = intent.getStringExtra("currentId");
        }

        Log.d("SelectedSection", "Selected Section: " + selectedSection);
        Log.d("SelectedSubject", "Selected Subject: " + selectedSubject);
        Log.d("CurrentId", "Current Id: " + currentId);


        //getting names of the students that attended
        //getting the list of meetings/attendance
        final String[] attendeesList = new String[1000];
        final String[] attendeesIDList = new String[1000];

        db.collection("students")
                .whereEqualTo("professorId", currentId)
                .whereEqualTo("section", selectedSection)
                .whereEqualTo("subject", selectedSubject) //these 2 are commented before. and without them, there is no code to filter na dapat student from
                .get()                          // x section and y subject should be chosen to appear in the listview
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int index = 0;

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

                        List<String> nonNullList2 = new ArrayList<>();
                        for (String item : attendeesIDList) {
                            if (item != null) {
                                nonNullList2.add(item);
                            }
                        }
                        String[] trimmedAttendanceIDList = nonNullList2.toArray(new String[0]);


                        // Loop through the array and log its contents to check
                        int i = 0;
                        Arrays.sort(trimmedAttendanceList);
                        Arrays.sort(trimmedAttendanceList2);
                        for (String name : trimmedAttendanceList2) {
                            Log.d("before edit", name + '-'); // Use your desired tag (e.g., "YourTag") and log level (e.g., Log.d)
                            trimmedAttendanceList2[i] = (i + 1) + ". " + trimmedAttendanceList[i];
                            Log.d("after edit", name); // Use your desired tag (e.g., "YourTag") and log level (e.g., Log.d)
                            i++;
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(RegisterStudentsActivity.this, android.R.layout.simple_list_item_1, trimmedAttendanceList2);
                        attendeesListView.setAdapter(adapter);

//                        trimmedAttendanceList2.length();
                        Toast.makeText(RegisterStudentsActivity.this, "Total Students: " + String.valueOf(trimmedAttendanceList2.length), Toast.LENGTH_SHORT).show();

                        subtitle.setText(selectedSection + " \u2192 " + selectedSubject + " \u2192 " +
                                " (Students: " + trimmedAttendanceList2.length + ")");
                        nameeee = Arrays.asList(trimmedAttendanceList);
                        studentsCount = String.valueOf(trimmedAttendanceList2.length);
                        updateStudentsCount(studentsCount);

                        attendeesListView.setOnItemClickListener((adapterView, view, position, id) -> {
//                            String selectedPosition = adapter.getItem(position);
                            Log.d("testss", trimmedAttendanceList[position]);
                            Log.d("testss", trimmedAttendanceIDList[position]);

                            AlertDialog.Builder builder = new AlertDialog.Builder(RegisterStudentsActivity.this);
                            builder.setTitle("Choose an action");

                            // Add "Delete" button
                            builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
//                                    String userIdToDelete = trimmedAttendanceIDList[position];
                                    String userIdToDelete = trimmedAttendanceList[position];
                                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                                    DocumentReference documentReference = db.collection("scannedData").document(userIdToDelete);

                                    db.collection("students")
                                            .whereEqualTo("professorId", currentId)
                                            .whereEqualTo("section", selectedSection)
                                            .whereEqualTo("subject", selectedSubject)
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
                            builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(RegisterStudentsActivity.this, "close", Toast.LENGTH_SHORT).show();
                                }
                            });

                            // Create and show the AlertDialog
                            builder.create().show();
                        });
                    }
                });


        //NOW THE ADD BUTTON IN THE DEEPEST PART OF THE VIEW
        AutoCompleteTextView autoCompleteTextView = findViewById(R.id.spinnerWithFilter);
        autoCompleteTextView.setVisibility(View.GONE);

        // Sample data for the dropdown
        String[] items = {"Item 1", "Item 2", "Item 3", "Item 4", "Item 5"};

        // Getting the names of the students to be added to the autoCompleteTextView
        int maxDocuments = 1000;
        String[][][] userDataArray = new String[maxDocuments][3][3]; // Assuming 3 fields: name, number, documentId

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

                            // Create ArrayAdapter
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(RegisterStudentsActivity.this,
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

                            ConstraintLayout addStudentsLayout = findViewById(R.id.addStudentsLayout);
                            addStudentsLayout.setBackgroundColor(Color.TRANSPARENT);

                            // Set bottom margin
                            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) attendeesListView.getLayoutParams();
                            layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin, layoutParams.rightMargin, 20);
                            attendeesListView.setLayoutParams(layoutParams);

                            Button addStudentsButton = findViewById(R.id.addStudentBtn);
                            addStudentsButton.setText("Add Students");
                            addStudentsButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    fetchFirestoreDataAndShowDialog(nameeee);
                                }
                            });
//                            addStudentsButton.setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View view) {
//                                    if (!TextUtils.isEmpty(selectedName[0]) && !TextUtils.isEmpty(selectedDocumentId[0]) && !TextUtils.isEmpty(selectedEmail[0])) {
//                                        // Variables are not empty, you I can now push them to the scannedDAta Collection
//                                        String name = selectedName[0];
//                                        String documentId = selectedDocumentId[0];
//                                        String email = selectedEmail[0];
//                                        String subject = selectedSubject;
//                                        String section = selectedSection;
//                                        String professorId = currentId;
//
//
//                                        // Create a Map to store the data
//                                        Map<String, Object> scannedData = new HashMap<>();
//                                        scannedData.put("number", email);
//                                        scannedData.put("name", name);
//                                        scannedData.put("professorId", professorId);
//                                        scannedData.put("section", section);
//                                        scannedData.put("subject", subject);
//                                        scannedData.put("userId", documentId);
//
//                                        db.collection("students")
//                                                .add(scannedData)
//                                                .addOnSuccessListener(documentReference -> {
//                                                    // Document added successfully
//                                                    String addedDocumentId = documentReference.getId();
//                                                    Log.d("Firestore", "Document added with ID: " + addedDocumentId);
//                                                })
//                                                .addOnFailureListener(e -> {
//                                                    // Handle the error
//                                                    Log.e("Firestore", "Error adding document", e);
//                                                });
//
//
//                                        Log.d("SelectedDataBtn2", "DATA PUSH SUCCESSFUL!");
//                                        Log.d("SelectedDataBtn2", "Name: " + selectedName[0]);
//                                        Log.d("SelectedDataBtn2", "Document ID: " + selectedDocumentId[0]);
//                                        Log.d("SelectedDataBtn2", "Number: " + selectedEmail[0]);
//                                        Log.d("SelectedDataBtn2", "Subject: " + selectedSubject);
//                                        Log.d("SelectedDataBtn2", "Section: " + selectedSection);
//                                        Log.d("SelectedDataBtn2", "Professor ID: " + currentId);
//
//                                        reloadActivity();
//                                    } else {
//                                        // At least one of the variables is empty
//                                        Log.d("SelectedDataBtn", "One or more variables are empty");
//                                        Toast.makeText(RegisterStudentsActivity.this, "Select a student to add.", Toast.LENGTH_SHORT).show();
//                                    }
//
//                                }
//                            });

                        } else {
                            Log.d("FirestoreData", "Error getting documents: ", task.getException());
                        }
                    }
                });


        //delete duplicate files
        deleteDuplicates();
    }


    private void fetchFirestoreDataAndShowDialog(List nameList) {
        FirebaseFirestore.getInstance().collection("User")
//                .whereEqualTo("professorId", currentId)
//                .whereEqualTo("section", selectedSection)
//                .whereEqualTo("subject", selectedSubject)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> firestoreData = new ArrayList<>();
                        Log.d("NamesList", "Contents: " + nameList.toString());

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Adjust this line based on the field you want to retrieve
                            String name = document.getString("name");
                            Log.d("checkkk", "fetchFirestoreDataAndShowDialog: " + name);
                            if (!nameList.contains(name)) {
                            Log.d("checkkk", "fetchFirestoreDataAndShowDialog: " + "none");
                                firestoreData.add(name);
                            }
                        }

                        showCheckboxDialog(firestoreData);
                    } else {
                        // Handle errors
                        Toast.makeText(RegisterStudentsActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showCheckboxDialog(List<String> firestoreData) {
        // Create a LayoutInflater to inflate the dialog layout
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.drawable.checkbox_dialog, null);

        // Find the LinearLayout where checkboxes will be added
        LinearLayout checkboxContainer = dialogView.findViewById(R.id.checkboxContainer);

        // Dynamically create checkboxes based on the data from Firestore
        for (String data : firestoreData) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(data);
            checkboxContainer.addView(checkBox);
        }

        // Build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setTitle("Select Students to Add")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Handle the checkboxes here
                        for (int i = 0; i < checkboxContainer.getChildCount(); i++) {
                            View childView = checkboxContainer.getChildAt(i);
                            if (childView instanceof CheckBox) {
                                CheckBox checkBox = (CheckBox) childView;
                                if (checkBox.isChecked()) {
                                    // Do something with the selected option
                                    String selectedOption = checkBox.getText().toString();

                                    //get student information
                                    FirebaseFirestore.getInstance().collection("User")
                                            .whereEqualTo("name", selectedOption) // Assuming "name" is the field you want to match
                                            .get()
                                            .addOnCompleteListener(task -> {
                                                if (task.isSuccessful()) {
                                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                                        // Retrieve values from Firestore document
                                                        String email = document.getString("email");
                                                        String name = document.getString("name");
                                                        String number = document.getString("number");

                                                        // Retrieve the document ID
                                                        String documentID = document.getId();

                                                        // Now you can use the retrieved values as needed
                                                        Log.d("UserData", "Email: " + email + ", Name: " + name + ", Number: " + number + ", DocumentID: " + documentID);


                                                        // Create a Map to store the data
                                                        Map<String, Object> scannedData = new HashMap<>();
                                                        scannedData.put("number", number);
                                                        scannedData.put("email", email);
                                                        scannedData.put("name", name);
                                                        scannedData.put("professorId", currentId);
                                                        scannedData.put("section", selectedSection);
                                                        scannedData.put("subject", selectedSubject);
                                                        scannedData.put("userId", documentID);

                                                        db.collection("students")
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
                                                        Log.d("SelectedDataBtn2", "Name: " + name);
                                                        Log.d("SelectedDataBtn2", "Document ID: " + document);
                                                        Log.d("SelectedDataBtn2", "Number: " + number);
                                                        Log.d("SelectedDataBtn2", "Subject: " + selectedSubject);
                                                        Log.d("SelectedDataBtn2", "Section: " + selectedSection);
                                                        Log.d("SelectedDataBtn2", "Professor ID: " + currentId);

                                                        reloadActivity();
                                                    }
                                                } else {
                                                    // Handle errors
                                                    Log.e("FirestoreQuery", "Error getting documents: ", task.getException());
                                                }
                                            });



//                                    Toast.makeText(RegisterStudentsActivity.this, "Selected Option: " + selectedOption, Toast.LENGTH_SHORT).show();
                                    //herrreeee
                                }
                            }
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Handle the cancel action
                        dialog.dismiss();
                    }
                });

        // Show the dialog
        builder.create().show();
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

        db.collection("students")
                .document(documentId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("TAG", "DocumentSnapshot successfully deleted: " + documentId);
                        Toast.makeText(RegisterStudentsActivity.this, "Record successfuly deleted.", Toast.LENGTH_SHORT).show();

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

    private void updateStudentsCount(String newStudentsCount) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

// Assuming currentId, selectedSection, and selectedSubject are already defined

// Construct the reference to the document you want to update
        db.collection("subjects")
                .whereEqualTo("professorID", currentId)
                .whereEqualTo("sectionName", selectedSection)
                .whereEqualTo("subjectName", selectedSubject)
                .limit(1)  // Limit to 1 document (assuming there should be only one matching document)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Get the document ID of the matching document
                            String documentId = document.getId();

                            db.collection("subjects").document(documentId)
                                    .update("studentsCount", newStudentsCount)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("FirestoreUpdate", "DocumentSnapshot successfully updated!");
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.w("FirestoreUpdate", "Error updating document", e);
                                    });
                        }
                    } else {
                        Log.d("FirestoreQuery", "Error getting documents: " + task.getException());
                    }
                });

    }


    private void deleteDuplicates() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

// Define the collection and fields to check for duplicates
        String collectionPath = "students";
        String[] fieldsToCheck = {"name", "professorId", "section", "subject", "userId"};

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

}
