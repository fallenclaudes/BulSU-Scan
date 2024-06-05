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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ScannedSectionsActivity extends AppCompatActivity {

    private ListView sectionsListView;
    String startDate, endDate;
    // In the receiving activity (e.g., in onCreate method)
    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;
    private CollectionReference sectionsCollections;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanned_sections);

        firestore = FirebaseFirestore.getInstance();
        sectionsCollections = firestore.collection("sections");

        // In the receiving activity (e.g., in onCreate method)
        Intent intent = getIntent();
        // Retrieve values using keys
         startDate = intent.getStringExtra("startDate");
         endDate = intent.getStringExtra("endDate");
        Log.d("IntentData2", "Start Date: " + startDate);
        Log.d("IntentData2", "End Date: " + endDate);


        loadData();
    }


    private void loadData() {
        //TODO: Uncomment this after testing
//        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String professorId = currentUser.getUid();            //UNCOMMENT THIS
//        String professorId = "DERk950rMnURI9GbHjyK71v1bMy1";    //COMMENT THIS OUT

        //getting the name of the professor
        DocumentReference docRef = firestore.collection("TeachersUser").document(professorId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    String professorName = document.getString("names");

                    if (document.exists()) {
                        TextView subtitle = findViewById(R.id.subtitle);
                        subtitle.setText(professorName);
                        Log.d("testss", "DocumentSnapshot data: " + document.getData());
                    } else {
                        Log.d("testss", "No such document");
                    }
                } else {
                    Log.d("testss", "get failed with ", task.getException());
                }
            }
        });


        //getting data for the subject list view
        sectionsListView = findViewById(R.id.sectionsListView);
        final List<String> sectionNameList = new ArrayList<>();
        firestore.collection("sections")
                .whereEqualTo("professorID", professorId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int index = 0;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String name = document.getString("sectionName");
                                if (name != null) {
                                    sectionNameList.add(name);
                                    Log.d("testss" + index, "Section Name: " + name);
                                } else {
                                    Log.d("testss", "Section Name is null for document: " + document.getId());
                                }
                                index++; //increment index
                            }
                        } else {
                            Log.d("testss", "Error getting documents: ", task.getException());
                        }

                        //put the subject names to
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(ScannedSectionsActivity.this, android.R.layout.simple_list_item_1, sectionNameList);
                        sectionsListView.setAdapter(adapter);


                        sectionsListView.setOnItemClickListener((adapterView, view, position, id) -> {
                            String selectedSection = adapter.getItem(position);
                            Toast.makeText(ScannedSectionsActivity.this, selectedSection, Toast.LENGTH_SHORT).show();
                            if (selectedSection != null) {
                                Intent intent = new Intent(ScannedSectionsActivity.this, ScannedSubjectsActivity.class);
                                intent.putExtra("startDate", startDate);
                                intent.putExtra("endDate", endDate);
                                intent.putExtra("selectedSection", selectedSection);
                                intent.putExtra("currentId", professorId);
                                startActivity(intent);
                            }
                        });
                    }
                });


    }
}