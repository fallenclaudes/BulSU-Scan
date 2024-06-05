package com.example.qrcodes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SectionListTeacher extends AppCompatActivity {
    TextView email, name, sectionList, midtermView, finalsView;
    FirebaseAuth mAuth;
    FirebaseFirestore fstore;
    Button changepass, addSection, modifyDatesBtn;
    String userId, midtermStartDate, midtermEndDate, finalsStartDate, finalsEndDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_section_list_teacher);
//
//
//        name.setFocusable(false);
//        email.setFocusable(false);

        addSection = findViewById(R.id.addSection);
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
                    public void onComplete(@androidx.annotation.NonNull Task<QuerySnapshot> task) {
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
//                            combinedArray[i] = newSectionName[i] + " (" + newStudentsCount[i] + " students)";
                            combinedArray[i] = newSectionName[i];
                        }

                        for (String item : combinedArray) {
                            Log.d("CombinedArray", item);
                        }

                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(SectionListTeacher.this, android.R.layout.simple_list_item_1, combinedArray);
                        sectionListView.setAdapter(arrayAdapter);

                        //adding sections
                        final String[] userInput = new String[1]; //this will contain the user input for the new section name
                        addSection.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Toast.makeText(SectionListTeacher.this, "add section pressed", Toast.LENGTH_SHORT).show();

                                // Create an AlertDialog
                                AlertDialog.Builder builder = new AlertDialog.Builder(SectionListTeacher.this);
                                builder.setTitle("Enter Text");

                                // Inflate a custom layout for the dialog
                                LayoutInflater inflater = getLayoutInflater();
                                @SuppressLint("ResourceType") View dialogView = inflater.inflate(R.drawable.dialog_layout, null);
                                builder.setView(dialogView);

                                // Find the EditText elements in the custom layout
                                final EditText editTextSectionName = dialogView.findViewById(R.id.editTextSectionName);
//                                final EditText editTextStudentsCount = dialogView.findViewById(R.id.editTextStudentsCount);


                                // Update the positive button click listener
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Get the user's input for SectionName and StudentsCount
                                        String sectionName = editTextSectionName.getText().toString();
//                                        String studentsCount = editTextStudentsCount.getText().toString();
                                        String studentsCount = "0";

                                        Log.d("pre push", "onClick: " + sectionName + " " + studentsCount);

                                        // Validate input
                                        if (sectionName.length() <= 1 || studentsCount.length() == 0) {
                                            Toast.makeText(SectionListTeacher.this, "Bad Section Name or Students Count", Toast.LENGTH_SHORT).show();
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

                                            Toast.makeText(SectionListTeacher.this, "Section " + sectionName + " with Students Count " + studentsCount + " successfully added", Toast.LENGTH_SHORT).show();
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


                        sectionListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                            @Override
                            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                                // This method will be called when an item is long-pressed
                                Toast.makeText(SectionListTeacher.this, "Modify Subjects for " + newSectionName[position], Toast.LENGTH_SHORT).show();

                                // In the NextActivity
                                Intent intent = new Intent(SectionListTeacher.this, EditSubjectsActivity.class);
                                intent.putExtra("currentId", userId);
                                intent.putExtra("sectionNameSelected", newSectionName[position]);
                                intent.putExtra("studentsCountSelected", newStudentsCount[position]);
                                startActivity(intent);


                                startActivity(intent);
                                // Return true to indicate that the long click event has been handled
                                return true;
                            }
                        });

                        sectionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                // Show a dialog to edit the section details
                                AlertDialog.Builder builder = new AlertDialog.Builder(SectionListTeacher.this);
                                builder.setTitle("Edit Section Details");

                                // Set up the layout for the dialog
                                LayoutInflater inflater = getLayoutInflater();
                                @SuppressLint("ResourceType") View dialogView = inflater.inflate(R.drawable.edit_section_dialog, null);
                                builder.setView(dialogView);

                                // Find views in the dialog layout
                                EditText sectionNameEditText = dialogView.findViewById(R.id.editTextSectionName);
//                                EditText studentsCountEditText = dialogView.findViewById(R.id.editTextStudentsCount);

                                // Set initial values in the dialog
                                sectionNameEditText.setText(newSectionName[position]);
//                                studentsCountEditText.setText(newStudentsCount[position]);
                                newStudentsCount[position] = "0";

                                // Set up buttons
                                builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Save the edited avalues
                                        String updatedSectionName = sectionNameEditText.getText().toString();
                                        String updatedStudentsCount = "0";

                                        Log.d("asdasdasdsa", "onClick: "+ updatedSectionName + " "+updatedStudentsCount);


                                        fstore.collection("sections")
                                                .whereEqualTo("professorID", userId)
                                                .whereEqualTo("sectionName", newSectionName[position])
//                                                .whereEqualTo("studentsCount", newStudentsCount[position])
                                                .get()
                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onComplete(@androidx.annotation.NonNull Task<QuerySnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                                // Update the document in Firestore
                                                                DocumentReference documentRef = fstore.collection("sections").document(document.getId());
                                                                Map<String, Object> updates = new HashMap<>();
                                                                updates.put("sectionName", updatedSectionName);
                                                                updates.put("studentsCount", updatedStudentsCount);

                                                                documentRef.update(updates)
                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {
                                                                                Log.d("UpdateSection", "DocumentSnapshot successfully updated");
                                                                                Toast.makeText(SectionListTeacher.this, "Section information successfully updated", Toast.LENGTH_SHORT).show();
                                                                                reloadActivity();
                                                                            }
                                                                        })
                                                                        .addOnFailureListener(new OnFailureListener() {
                                                                            @Override
                                                                            public void onFailure(@androidx.annotation.NonNull Exception e) {
                                                                                Log.w("UpdateSection", "Error updating document", e);
                                                                                Toast.makeText(SectionListTeacher.this, "Something went wrong.", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        });
                                                            }
                                                        } else {
                                                            Log.d("UpdateSection", "Error getting documents: ", task.getException());
                                                        }
                                                    }
                                                });

                                        // Notify the adapter about the data change
                                        arrayAdapter.notifyDataSetChanged();
                                    }
                                });

                                builder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Delete the section

                                        fstore.collection("sections")
                                                .whereEqualTo("professorID", userId)
                                                .whereEqualTo("sectionName", newSectionName[position])
                                                .whereEqualTo("studentsCount", newStudentsCount[position])
                                                .get()
                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onComplete(@androidx.annotation.NonNull Task<QuerySnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                                // Delete the document from Firestore
                                                                fstore.collection("sections").document(document.getId()).delete()
                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {
                                                                                Log.d("DeleteSection", "DocumentSnapshot successfully deleted");
                                                                                Toast.makeText(SectionListTeacher.this, "Section successfully deleted", Toast.LENGTH_SHORT).show();
                                                                                reloadActivity();
                                                                            }
                                                                        })
                                                                        .addOnFailureListener(new OnFailureListener() {
                                                                            @Override
                                                                            public void onFailure(@androidx.annotation.NonNull Exception e) {
                                                                                Log.w("DeleteSection", "Error deleting document", e);
                                                                                Toast.makeText(SectionListTeacher.this, "Something went wrong.", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        });
                                                            }
                                                        } else {
                                                            Log.d("DeleteSection", "Error getting documents: ", task.getException());
                                                        }
                                                    }
                                                });

                                        arrayAdapter.notifyDataSetChanged();
                                    }
                                });

                                builder.setNeutralButton("Close", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Close the dialog
                                        dialog.dismiss();
                                    }
                                });

                                // Show the dialog
                                builder.show();
                            }
                        });
                    }
                });



    }


    private void showModifyDatesDialog() {
        // Create an AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(SectionListTeacher.this);
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
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Get the user's input and update the date variables
                midtermStartDate = midtermStartDateEditText.getText().toString();
                midtermEndDate = midtermEndDateEditText.getText().toString();
                finalsStartDate = finalsStartDateEditText.getText().toString();
                finalsEndDate = finalsEndDateEditText.getText().toString();


                Log.d("StartDate", "Midterm: " + midtermStartDate);
                Log.d("EndDate", "Midterm: " + midtermEndDate);
                Log.d("StartDate", "Finals: " + finalsStartDate);
                Log.d("EndDate", "Finals: " + finalsEndDate);

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
                                    Toast.makeText(SectionListTeacher.this, "Dates modified successfully", Toast.LENGTH_SHORT).show();
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
                } else {
                    // Display an error message or take appropriate action
                    Toast.makeText(SectionListTeacher.this, "Invalid date format", Toast.LENGTH_SHORT).show();
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
}