package com.example.qrcodes;


import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AccountInfoTeacher extends AppCompatActivity {
    TextView email, name, sectionList, midtermView, finalsView;
    FirebaseAuth mAuth;
    FirebaseFirestore fstore;
    Button changepass, addSection, modifyDatesBtn;
    String userId, midtermStartDate, midtermEndDate, finalsStartDate, finalsEndDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_info_teacher);

        email = findViewById(R.id.emailaddress1);
        name = findViewById(R.id.name1);
        changepass = findViewById(R.id.changepassword);
//        sectionList = findViewById(R.id.sectionList2);
        addSection = findViewById(R.id.addSection);

        midtermView = findViewById(R.id.midtermList);
        finalsView = findViewById(R.id.finalsList);
        modifyDatesBtn = findViewById(R.id.modifyDatesBtn);


        name.setFocusable(false);
        email.setFocusable(false);

        mAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();


        //TODO: Uncomment this
        userId = mAuth.getCurrentUser().getUid();  //UNCOMMENT THIS
//        userId = "DERk950rMnURI9GbHjyK71v1bMy1"; //for testing only COMMENT THIS LINE OUT
        Log.d("IDprofessor", userId);

        DocumentReference documentReference = fstore.collection("TeachersUser").document(userId);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                //get dates values
                midtermStartDate = documentSnapshot.getString("midtermStartDate");
                midtermEndDate = documentSnapshot.getString("midtermEndDate");
                finalsStartDate = documentSnapshot.getString("finalsStartDate");
                finalsEndDate = documentSnapshot.getString("finalsEndDate");

                Log.d("testss", documentSnapshot.getString("emails"));
                Log.d("testss", documentSnapshot.getString("names"));
                email.setText(documentSnapshot.getString("emails"));
                name.setText(documentSnapshot.getString("names"));

                midtermView.setText("\u2022 \bMidterm: " + midtermStartDate + " to " + midtermEndDate);
                finalsView.setText("\u2022 \bFinals: " + finalsStartDate + " to " + finalsEndDate);
            }
        });
        changepass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountInfoTeacher.this, ChangePasswordActivity.class);
                startActivity(intent);
            }
        });


        //Section Part
        //retrieving section names
        final String[] sectionName = new String[1000]; //max of 30 sections per professor
        final String[] studentsCount = new String[1000]; //max of 30 sections per professor

        ListView sectionListView = findViewById(R.id.sectionList2);

        fstore.collection("sections")
                .whereEqualTo("professorID", userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int index = 0;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                sectionName[index] = document.getString("sectionName");
                                studentsCount[index] = document.getString("studentsCount");
                                if (sectionName[index] != null) {
                                    Log.d("testss" + index, "Section Name: " + sectionName[index] + "-" + studentsCount[index]);
                                } else {
                                    Log.d("testss", "Section Name is null for document: " + document.getId());
                                }
                                index++; //increment index
                            }
                        } else {
                            Log.d("testss", "Error getting documents: ", task.getException());
                        }


                        // Count the number of non-null section names
                        int nonNullCount = 0;
                        for (String name : sectionName) {
                            if (name != null) {
                                nonNullCount++;
                            }
                        }

// Create new arrays with the correct length
                        String[] newSectionName = new String[nonNullCount];
                        String[] newStudentsCount = new String[nonNullCount];

// Copy non-null values to the new arrays
                        int newIndex = 0;
                        for (int i = 0; i < sectionName.length; i++) {
                            if (sectionName[i] != null) {
                                newSectionName[newIndex] = sectionName[i];
                                newStudentsCount[newIndex] = studentsCount[i];
                                newIndex++;
                            }
                        }



// 2. Create a combinedArray displaying the sectionName and student count
                        String[] combinedArray = new String[newSectionName.length];
                        for (int i = 0; i < newSectionName.length; i++) {
                            combinedArray[i] = newSectionName[i] + " (" + newStudentsCount[i] + " students)";
                        }

                        for (String item : combinedArray) {
                            Log.d("CombinedArray", item);
                        }

                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(AccountInfoTeacher.this, android.R.layout.simple_list_item_1, combinedArray);
                        sectionListView.setAdapter(arrayAdapter);


                        sectionListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                            @Override
                            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                                // This method will be called when an item is long-pressed
                                Toast.makeText(AccountInfoTeacher.this, "Modify Subjects for "+newSectionName[position], Toast.LENGTH_SHORT).show();

                                // In the NextActivity
                                Intent intent = new Intent(AccountInfoTeacher.this, EditSubjectsActivity.class);
                                intent.putExtra("currentId", userId);
                                intent.putExtra("sectionNameSelected", newSectionName[position]);
                                intent.putExtra("studentsCountSelected", newStudentsCount[position]);
                                startActivity(intent);


                                startActivity(intent);
                                // Return true to indicate that the long click event has been handled
                                return true;
                            }
                        });

//                        sectionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//                            @Override
//                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                                // Show a dialog to edit the section details
//                                AlertDialog.Builder builder = new AlertDialog.Builder(AccountInfoTeacher.this);
//                                builder.setTitle("Edit Section Details");
//
//                                // Set up the layout for the dialog
//                                LayoutInflater inflater = getLayoutInflater();
//                                @SuppressLint("ResourceType") View dialogView = inflater.inflate(R.drawable.edit_section_dialog, null);
//                                builder.setView(dialogView);
//
//                                // Find views in the dialog layout
//                                EditText sectionNameEditText = dialogView.findViewById(R.id.editTextSectionName);
//                                EditText studentsCountEditText = dialogView.findViewById(R.id.editTextStudentsCount);
//
//                                // Set initial values in the dialog
//                                sectionNameEditText.setText(newSectionName[position]);
//                                studentsCountEditText.setText(newStudentsCount[position]);
//
//                                // Set up buttons
//                                builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        // Save the edited values
//                                        String updatedSectionName = sectionNameEditText.getText().toString();
//                                        String updatedStudentsCount = studentsCountEditText.getText().toString();
//
//
//                                        fstore.collection("sections")
//                                                .whereEqualTo("professorID", userId)
//                                                .whereEqualTo("sectionName", newSectionName[position])
//                                                .whereEqualTo("studentsCount", newStudentsCount[position])
//                                                .get()
//                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                                                    @Override
//                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                                                        if (task.isSuccessful()) {
//                                                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                                                // Update the document in Firestore
//                                                                DocumentReference documentRef = fstore.collection("sections").document(document.getId());
//                                                                Map<String, Object> updates = new HashMap<>();
//                                                                updates.put("sectionName", updatedSectionName);
//                                                                updates.put("studentsCount", updatedStudentsCount);
//
//                                                                documentRef.update(updates)
//                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                                            @Override
//                                                                            public void onSuccess(Void aVoid) {
//                                                                                Log.d("UpdateSection", "DocumentSnapshot successfully updated");
//                                                                                Toast.makeText(AccountInfoTeacher.this, "Section information successfully updated", Toast.LENGTH_SHORT).show();
//                                                                                reloadActivity();
//                                                                            }
//                                                                        })
//                                                                        .addOnFailureListener(new OnFailureListener() {
//                                                                            @Override
//                                                                            public void onFailure(@NonNull Exception e) {
//                                                                                Log.w("UpdateSection", "Error updating document", e);
//                                                                                Toast.makeText(AccountInfoTeacher.this, "Something went wrong.", Toast.LENGTH_SHORT).show();
//                                                                            }
//                                                                        });
//                                                            }
//                                                        } else {
//                                                            Log.d("UpdateSection", "Error getting documents: ", task.getException());
//                                                        }
//                                                    }
//                                                });
//
//                                        // Notify the adapter about the data change
//                                        arrayAdapter.notifyDataSetChanged();
//                                    }
//                                });
//
//                                builder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        // Delete the section
//
//                                        fstore.collection("sections")
//                                                .whereEqualTo("professorID", userId)
//                                                .whereEqualTo("sectionName", newSectionName[position])
//                                                .whereEqualTo("studentsCount", newStudentsCount[position])
//                                                .get()
//                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                                                    @Override
//                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                                                        if (task.isSuccessful()) {
//                                                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                                                // Delete the document from Firestore
//                                                                fstore.collection("sections").document(document.getId()).delete()
//                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                                            @Override
//                                                                            public void onSuccess(Void aVoid) {
//                                                                                Log.d("DeleteSection", "DocumentSnapshot successfully deleted");
//                                                                                Toast.makeText(AccountInfoTeacher.this, "Section successfully deleted", Toast.LENGTH_SHORT).show();
//                                                                                reloadActivity();
//                                                                            }
//                                                                        })
//                                                                        .addOnFailureListener(new OnFailureListener() {
//                                                                            @Override
//                                                                            public void onFailure(@NonNull Exception e) {
//                                                                                Log.w("DeleteSection", "Error deleting document", e);
//                                                                                Toast.makeText(AccountInfoTeacher.this, "Something went wrong.", Toast.LENGTH_SHORT).show();
//                                                                            }
//                                                                        });
//                                                            }
//                                                        } else {
//                                                            Log.d("DeleteSection", "Error getting documents: ", task.getException());
//                                                        }
//                                                    }
//                                                });
//
//                                        arrayAdapter.notifyDataSetChanged();
//                                        }
//                                });
//
//                                builder.setNeutralButton("Close", new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        // Close the dialog
//                                        dialog.dismiss();
//                                    }
//                                });
//
//                                // Show the dialog
//                                builder.show();
//                            }
//                        });
                    }
                });


        //adding sections
        final String[] userInput = new String[1]; //this will contain the user input for the new section name
        addSection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(AccountInfoTeacher.this, "add section pressed", Toast.LENGTH_SHORT).show();

                // Create an AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(AccountInfoTeacher.this);
                builder.setTitle("Enter Text");

                // Inflate a custom layout for the dialog
                LayoutInflater inflater = getLayoutInflater();
                @SuppressLint("ResourceType") View dialogView = inflater.inflate(R.drawable.dialog_layout, null);
                builder.setView(dialogView);

                // Find the EditText elements in the custom layout
                final EditText editTextSectionName = dialogView.findViewById(R.id.editTextSectionName);
//                final EditText editTextStudentsCount = dialogView.findViewById(R.id.editTextStudentsCount);


                // Update the positive button click listener
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Get the user's input for SectionName and StudentsCount
                        String sectionName = editTextSectionName.getText().toString();
//                        String studentsCount = editTextStudentsCount.getText().toString();
                        String studentsCount = "0";

                        Log.d("pre push", "onClick: " + sectionName + " " + studentsCount);

                        // Validate input
                        if (sectionName.length() <= 1 || studentsCount.length() == 0) {
                            Toast.makeText(AccountInfoTeacher.this, "Bad Section Name or Students Count", Toast.LENGTH_SHORT).show();
                        } else {
                            fstore.collection("sections")
                                    .add(createSectionData(userId, sectionName, studentsCount))
                                    .addOnSuccessListener(documentReference -> {
                                        // Document ID is autogenerated and can be retrieved using documentReference.getId()
                                        String documentId = documentReference.getId();
                                        Log.d("Firestore", "Document added with ID: " + documentId);
                                        Log.d("Firestore", "Professor ID: " + userId);
                                        Log.d("Firestore", "Section Name: " + sectionName);
                                        Log.d("Firestore", "Students Count: " + studentsCount);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.w("Firestore", "Error adding document", e);
                                    });

                            Toast.makeText(AccountInfoTeacher.this, "Section " + sectionName + " with Students Count " + studentsCount + " successfully added", Toast.LENGTH_SHORT).show();
                            reloadActivity();
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Handle cancel button click if needed
                        dialog.dismiss();
                    }
                });

                // Show the dialog
                builder.create().show();
            }
        });


        //modify dates
        modifyDatesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showModifyDatesDialog();
            }
        });
    }


    private void showModifyDatesDialog() {
        // Create an AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(AccountInfoTeacher.this);
        builder.setTitle("Modify Dates");

        // Inflate a custom layout for the dialog
        LayoutInflater inflater = getLayoutInflater();
        @SuppressLint("ResourceType") View dialogView = inflater.inflate(R.drawable.modify_dates_dialog, null);
        builder.setView(dialogView);

        // Find the EditTexts in the custom layout
        EditText midtermStartDateEditText = dialogView.findViewById(R.id.midtermStartDateEditText);
        EditText midtermEndDateEditText = dialogView.findViewById(R.id.midtermEndDateEditText);
        EditText finalsStartDateEditText = dialogView.findViewById(R.id.finalsStartDateEditText);
        EditText finalsEndDateEditText = dialogView.findViewById(R.id.finalsEndDateEditText);

        // Set existing dates to EditTexts
        midtermStartDateEditText.setText(midtermStartDate);
        midtermEndDateEditText.setText(midtermEndDate);
        finalsStartDateEditText.setText(finalsStartDate);
        finalsEndDateEditText.setText(finalsEndDate);


        fstore = FirebaseFirestore.getInstance();
        //TODO: Uncomment this
        userId = mAuth.getCurrentUser().getUid();
//        userId = "DERk950rMnURI9GbHjyK71v1bMy1"; //for testing only
        Log.d("IDprofessor", userId);
        builder.setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Get the user's input and update the date variables
                midtermStartDate = midtermStartDateEditText.getText().toString();
                midtermEndDate = midtermEndDateEditText.getText().toString();
                finalsStartDate = finalsStartDateEditText.getText().toString();
                finalsEndDate = finalsEndDateEditText.getText().toString();

                if(midtermStartDate.equals(midtermEndDate)){
                    Toast.makeText(AccountInfoTeacher.this, "There should be an interval in start and end dates for midterm", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(finalsStartDate.equals(finalsEndDate)){
                    Toast.makeText(AccountInfoTeacher.this, "There should be an interval in start and end dates for finals", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(midtermEndDate.equals(finalsStartDate)){
                    Toast.makeText(AccountInfoTeacher.this, "Midterm end date and finals start date can't be simultaneous", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Checking if end dates are not earlier than start dates
                if (isEndDateBeforeStartDate(midtermEndDateEditText.getText().toString(), finalsStartDateEditText.getText().toString())) {
                    Toast.makeText(AccountInfoTeacher.this, "Finals Start Date should not be earlier than Midterm End Date", Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.d("StartDate", "Midterm: " + midtermStartDate);
                Log.d("EndDate", "Midterm: " + midtermEndDate);
                Log.d("StartDate", "Finals: " + finalsStartDate);
                Log.d("EndDate", "Finals: " + finalsEndDate);

                //check the pattern of the dates
// Define the regex pattern for "yyyy-MM-dd" format
                Pattern pattern = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");

// Match the input against the pattern
                Matcher matcher1 = pattern.matcher(midtermStartDate);
                Matcher matcher2 = pattern.matcher(midtermEndDate);
                Matcher matcher3 = pattern.matcher(finalsStartDate);
                Matcher matcher4 = pattern.matcher(finalsEndDate);

// Check if the input matches the pattern
                if (matcher1.matches() && matcher2.matches() && matcher3.matches() && matcher4.matches()) {
                } else {
                    // The input does not match the expected format
                    Toast.makeText(AccountInfoTeacher.this, "Invalid date format. Please use yyyy-MM-dd.", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Set OnClickListener for date picker EditText fields
                midtermStartDateEditText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDatePickerDialog(midtermStartDateEditText);
                    }
                });

                midtermEndDateEditText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDatePickerDialog(midtermEndDateEditText);
                    }
                });

                finalsStartDateEditText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDatePickerDialog(finalsStartDateEditText);
                    }
                });

                finalsEndDateEditText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDatePickerDialog(finalsEndDateEditText);}
                });


                // Checking if end dates are not earlier than start dates
                if (isEndDateBeforeStartDate(midtermStartDateEditText.getText().toString(), midtermEndDateEditText.getText().toString())) {
                    Toast.makeText(AccountInfoTeacher.this, "Midterm End Date should not be earlier than Midterm Start Date", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (isEndDateBeforeStartDate(finalsStartDateEditText.getText().toString(), finalsEndDateEditText.getText().toString())) {
                    Toast.makeText(AccountInfoTeacher.this, "Finals End Date should not be earlier than Finals Start Date", Toast.LENGTH_SHORT).show();
                    return;
                }


                if (isValidDateFormat(midtermStartDate) &&
                        isValidDateFormat(midtermEndDate) &&
                        isValidDateFormat(finalsStartDate) &&
                        isValidDateFormat(finalsEndDate)) {


                    // Create a Map to update the fields in Firestore
                    Map<String, Object> updateFields = new HashMap<>();
                    updateFields.put("iddd", userId);
                    updateFields.put("midtermStartDate", midtermStartDate);
                    updateFields.put("midtermEndDate", midtermEndDate);
                    updateFields.put("finalsStartDate", finalsStartDate);
                    updateFields.put("finalsEndDate", finalsEndDate);

// Update the document in the "TeacherUser" collection with the specified userId
                    fstore.collection("TeachersUser")
                            .document(userId)
                            .update(updateFields)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // Document updated successfully
                                    Log.d("Firestore", "Dates updated successfully");
                                    Toast.makeText(AccountInfoTeacher.this, "Dates modified successfully", Toast.LENGTH_SHORT).show();
                                    reloadActivity();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Handle the error
                                    Log.e("Firestore", "Error updating document", e);
                                }
                            });
                }else {
                    // Display an error message or take appropriate action
                    Toast.makeText(AccountInfoTeacher.this, "Invalid date format", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Handle cancel button click if needed
                dialog.dismiss();
            }
        });

        // Show the dialog
        builder.create().show();
    }


    private void showDatePickerDialog(final EditText editText) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // Set the selected date in the EditText
                        String selectedDate = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
                        editText.setText(selectedDate);
                    }
                }, year, month, day);

        datePickerDialog.show();

    }

    private boolean isValidDateFormat(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false);

        try {
            Date date = sdf.parse(dateStr);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private Map<String, Object> createSectionData(String userId, String sectionName, String studentsCount) {
        Map<String, Object> sectionData = new HashMap<>();
        sectionData.put("professorID", userId);
        sectionData.put("sectionName", sectionName);
        sectionData.put("studentsCount", studentsCount);
        return sectionData;
    }

    private void reloadActivity() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    private boolean isEndDateBeforeStartDate(String startDate, String endDate) {
        try {
            android.icu.text.SimpleDateFormat dateFormat = new android.icu.text.SimpleDateFormat("yyyy-MM-dd");

            Date start = dateFormat.parse(startDate);
            Date end = dateFormat.parse(endDate);

            return end.before(start);
        } catch (ParseException e) {
            e.printStackTrace();
            return false; // Handle the exception or return a default value
        }
    }
}