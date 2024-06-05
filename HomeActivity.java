package com.example.qrcodes;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeActivity extends AppCompatActivity {

    private Button generateqr;
    private Button sheets;
    private Button logoutButton;
    private Button sectionsssBtn;
    private Button accountinformation;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        firebaseAuth = FirebaseAuth.getInstance();
        accountinformation = findViewById(R.id.accountinfo);
        generateqr = findViewById(R.id.idbuttonGenerate);
        sheets = findViewById(R.id.sheets);
        logoutButton = findViewById(R.id.logoutButton);
        sectionsssBtn = findViewById(R.id.sectionsss);

        FirebaseUser currentUser;
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        //check for session
        String professorId = "";
        SharedPreferences preferences = getSharedPreferences("logincredentialteacher", MODE_PRIVATE);
        String ussr = preferences.getString("sessionID", null);
        if (ussr != null) {
            professorId = ussr;
            Log.d("sharepreferences", "onCreate: " + ussr);
        } else {
            professorId = currentUser.getUid();
//            professorId = "DERk950rMnURI9GbHjyK71v1bMy1";
            Log.d("sharepreferences", "onCreate: Something went wrong");
        }



        generateqr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, GenerateQR.class);
                startActivity(intent);
            }
        });
        sectionsssBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, SectionListTeacher.class);
                startActivity(intent);
            }
        });

        String finalProfessorId = professorId;
        sheets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Uncomment this later
//                String professorId = currentUser.getUid();
//                String professorId = "DERk950rMnURI9GbHjyK71v1bMy1";

                Log.d("proID", "proID: "+ finalProfessorId);
                FirebaseFirestore firestore = FirebaseFirestore.getInstance();

                //getting the name of the professor
                DocumentReference docRef = firestore.collection("TeachersUser").document(finalProfessorId);
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            String professorName = document.getString("names");
                            String midtermStartDate = document.getString("midtermStartDate");
                            String midtermEndDate = document.getString("midtermEndDate");
                            String finalsStartDate = document.getString("finalsStartDate");
                            String finalsEndDate = document.getString("finalsEndDate");

                            if (document.exists()) {
                                // Log the values
                                Log.d("FirestoreData", "Professor Name: " + professorName);
                                Log.d("FirestoreData", "Midterm Start Date: " + midtermStartDate);
                                Log.d("FirestoreData", "Midterm End Date: " + midtermEndDate);
                                Log.d("FirestoreData", "Finals Start Date: " + finalsStartDate);
                                Log.d("FirestoreData", "Finals End Date: " + finalsEndDate);

                            } else {
                                Log.d("testss", "No such document");
                                Toast.makeText(HomeActivity.this, "An error with retrieving the document occurred", Toast.LENGTH_SHORT).show();
                            }

                            //show the 2 buttons
                            // Create an AlertDialog
                            AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                            builder.setTitle("Choose a set");

                            // Add the buttons
                            builder.setPositiveButton("Midterm", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    SharedPreferences.Editor editor = getSharedPreferences("dateTypePreference", MODE_PRIVATE).edit();
                                    editor.putString("dateType", "MIDTERM");
                                    editor.apply();


                                    Intent intent = new Intent(HomeActivity.this, ScannedSectionsActivity.class);
                                    intent.putExtra("startDate", midtermStartDate);
                                    intent.putExtra("endDate", midtermEndDate);

                                    Log.d("IntentData", "Start Date: " + midtermStartDate);
                                    Log.d("IntentData", "End Date: " + midtermEndDate);

                                    startActivity(intent);
                                }
                            });

                            builder.setNegativeButton("Finals", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    SharedPreferences.Editor editor = getSharedPreferences("dateTypePreference", MODE_PRIVATE).edit();
                                    editor.putString("dateType", "FINALS");
                                    editor.apply();

                                    Intent intent = new Intent(HomeActivity.this, ScannedSectionsActivity.class);
                                    intent.putExtra("startDate", finalsStartDate);
                                    intent.putExtra("endDate", finalsEndDate);

                                    Log.d("IntentData", "Start Date: " + finalsStartDate);
                                    Log.d("IntentData", "End Date: " + finalsEndDate);

                                    startActivity(intent);
                                }
                            });

                            // Create and show the AlertDialog
                            builder.create().show();
                        } else {
                            Log.d("testss", "get failed with ", task.getException());
                        }
                    }
                });


//                Intent intent = new Intent(HomeActivity.this, AccountInfoTeacher.class);
//                startActivity(intent);
            }
        });
        accountinformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, AccountInfoTeacher.class);
                startActivity(intent);
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences preferences = getSharedPreferences("logincredentialteacher", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.clear();
                editor.apply();

                firebaseAuth.signOut();
                Intent intent = new Intent(HomeActivity.this, LoginActivity2.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
}
