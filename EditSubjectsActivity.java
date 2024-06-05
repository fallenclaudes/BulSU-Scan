package com.example.qrcodes;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditSubjectsActivity extends AppCompatActivity {

    private TextView titleTextView;
    private TextView subtitleTextView;
    private ListView subjectsListView;
    private ConstraintLayout addStudentsLayout;
    private EditText subjectNameEditText;
    private Button addStudentBtn;
    String studentsCountSelected;
    String sectionNameSelected;
    String currentId;
    FirebaseFirestore db = FirebaseFirestore.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_subjects);

        // Retrieve values from the intent
        Intent intent = getIntent();
        currentId = intent.getStringExtra("currentId");
        sectionNameSelected = intent.getStringExtra("sectionNameSelected");
        studentsCountSelected = intent.getStringExtra("studentsCountSelected");

        // Log the values
        Log.d("NextActivity", "Current ID: " + currentId);
        Log.d("NextActivity", "Section Name Selected: " + sectionNameSelected);
        Log.d("NextActivity", "Students Count Selected: " + studentsCountSelected);

        titleTextView = findViewById(R.id.title10);
        subtitleTextView = findViewById(R.id.subtitle4);
        subjectsListView = findViewById(R.id.subjectsListView2);
        addStudentsLayout = findViewById(R.id.addStudentsLayout);
        subjectNameEditText = findViewById(R.id.spinnerWithFilter2);
        addStudentBtn = findViewById(R.id.addStudentBtn2);

        titleTextView.setText("LISTS OF SUBJECTS IN " + sectionNameSelected);

        List<String> subjectNames = new ArrayList<>();

        // Query to get subjects based on professorID and sectionName
        db.collection("subjects")
                .whereEqualTo("professorID", currentId)
                .whereEqualTo("sectionName", sectionNameSelected)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Assuming "subjectName" is the field you want to display
                            String subjectName = document.getString("subjectName");
                            subjectNames.add(subjectName);
                        }

                        // Use an adapter to populate the ListView
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                EditSubjectsActivity.this,
                                android.R.layout.simple_list_item_1,
                                subjectNames
                        );
                        subjectsListView.setAdapter(adapter);

                        // You can use subjectNames array here or outside this block
                    } else {
                        Log.d("FirestoreQuery", "Error getting documents: ", task.getException());
                    }

                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(EditSubjectsActivity.this, android.R.layout.simple_list_item_1, subjectNames);
                    subjectsListView.setAdapter(arrayAdapter);
                });

        subjectsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected subjectName
                String selectedSubject = (String) parent.getItemAtPosition(position);

                // Show a dialog for editing or deleting
                showEditDeleteDialog(selectedSubject, position);
            }
        });
        subjectsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = (String) parent.getItemAtPosition(position);
                Toast.makeText(getApplicationContext(), "Students in " + selectedItem, Toast.LENGTH_SHORT).show();

                // In the NextActivity
                Intent intent = new Intent(EditSubjectsActivity.this, RegisterStudentsActivity.class);
                intent.putExtra("currentId", currentId);
                intent.putExtra("selectedSection", sectionNameSelected);
                intent.putExtra("selectedSubject", selectedItem);
                startActivity(intent);


                startActivity(intent);

                return true;
            }
        });


        //Adding new sections
        addStudentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String subjectName = String.valueOf(subjectNameEditText.getText());

                if (!TextUtils.isEmpty(subjectName)) {
                    // subjectName is not empty, do something
                    Log.d("MyApp", "Subject name is not empty: " + subjectName);


                    // Create a Map to represent the data
                    Map<String, Object> subjectData = new HashMap<>();
                    subjectData.put("professorID", currentId);
                    subjectData.put("sectionName", sectionNameSelected);
                    subjectData.put("subjectName", subjectName);

                    // Add the data to Firestore
                    db.collection("subjects")
                            .add(subjectData)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    // Document added successfully
                                    Log.d("Firestore", "DocumentSnapshot added with ID: " + documentReference.getId());
                                    Toast.makeText(EditSubjectsActivity.this, subjectName + "successfully added.", Toast.LENGTH_SHORT).show();
                                    reloadActivity();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Error adding document
                                    Log.w("Firestore", "Error adding document", e);
                                    Toast.makeText(EditSubjectsActivity.this, "Something went wrong.", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    // subjectName is empty, handle accordingly
                    Log.d("MyApp", "Subject name is empty");
                    Toast.makeText(EditSubjectsActivity.this, "Enter a subject name first.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showEditDeleteDialog(String selectedSubject, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit or Delete Subject");

        // Add an EditText for editing the subjectName
        final EditText editText = new EditText(this);
        editText.setText(selectedSubject);
        builder.setView(editText);

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Get the edited subjectName
                String editedSubject = editText.getText().toString();

                updateSubject(selectedSubject, editedSubject, position);
            }
        });

        builder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Delete the subject in Firestore or wherever you store it
                deleteSubject(selectedSubject, position);
            }
        });

        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Cancel button, do nothing
            }
        });

        builder.show();
    }

    // Implement these methods according to your data storage logic
    private void updateSubject(String oldSubject, String newSubject, int position) {
        Log.d("UpdateSection", "Updatingggggg");
        Log.d("UpdateSection", oldSubject);
        Log.d("UpdateSection", newSubject);
        Log.d("UpdateSection", String.valueOf(position));
        Log.d("UpdateSection", "Section name: " + sectionNameSelected);
        Log.d("UpdateSection", "ID: " + sectionNameSelected);

        db.collection("subjects")
                .whereEqualTo("professorID", currentId)
                .whereEqualTo("sectionName", sectionNameSelected)
                .whereEqualTo("subjectName", oldSubject)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@androidx.annotation.NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Update the document in Firestore
                                DocumentReference documentRef = db.collection("subjects").document(document.getId());
                                Map<String, Object> updates = new HashMap<>();
                                updates.put("professorID", currentId);
                                updates.put("sectionName", sectionNameSelected);
                                updates.put("subjectName", newSubject);

                                documentRef.update(updates)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d("UpdateSection", "DocumentSnapshot successfully updated");
                                                Toast.makeText(EditSubjectsActivity.this, "Subject information successfully updated", Toast.LENGTH_SHORT).show();
                                                reloadActivity();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@androidx.annotation.NonNull Exception e) {
                                                Log.w("UpdateSection", "Error updating document", e);
                                                Toast.makeText(EditSubjectsActivity.this, "Something went wrong.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        } else {
                            Log.d("UpdateSection", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void deleteSubject(String subjectToDelete, int position) {

        db.collection("subjects")
                .whereEqualTo("professorID", currentId)
                .whereEqualTo("sectionName", sectionNameSelected)
                .whereEqualTo("subjectName", subjectToDelete)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@androidx.annotation.NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Delete the document from Firestore
                                db.collection("subjects").document(document.getId()).delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d("DeleteSection", "DocumentSnapshot successfully deleted");
                                                Toast.makeText(EditSubjectsActivity.this, "Subject successfully deleted", Toast.LENGTH_SHORT).show();
                                                reloadActivity();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@androidx.annotation.NonNull Exception e) {
                                                Log.w("DeleteSection", "Error deleting document", e);
                                                Toast.makeText(EditSubjectsActivity.this, "Something went wrong.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        } else {
                            Log.d("DeleteSection", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }


    private void reloadActivity() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }
}