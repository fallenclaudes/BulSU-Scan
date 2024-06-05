package com.example.qrcodes;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeActivity2 extends AppCompatActivity {

    private Button scanqr;
    private Button logoutButton, accountinformation, attendanceRecordBtn;
    private FirebaseAuth firebaseAuth;
    FirebaseUser currentUser;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home2);

        firebaseAuth = FirebaseAuth.getInstance();
        scanqr = findViewById(R.id.idbuttonscanner);
        accountinformation = findViewById(R.id.accountinfo);
        logoutButton = findViewById(R.id.logoutButton1);
        attendanceRecordBtn = findViewById(R.id.attendanceRecordBtn);


        //TODO: Uncomment this before forwarding
        //getting the list of sections
//        String currentId = currentUser.getUid();
//        String currentId = "Rw1whNZB5tfBbDHvVqxW0rMNC8i2";


        //check for session
        String currentId = "";
        SharedPreferences preferences = getSharedPreferences("logincredential", MODE_PRIVATE);
        String ussr = preferences.getString("sessionID", null);
        if (ussr != null) {
            currentId = ussr;
            Log.d("sharepreferences", "onCreate: " + ussr);
        } else {
            currentUser = FirebaseAuth.getInstance().getCurrentUser(); //TODO: Uncomment this later on
           currentId = currentUser.getUid();
            Log.d("sharepreferences", "onCreate: Something went wrong");
        }

//        get the user name
//        String currentId = "Rw1whNZB5tfBbDHvVqxW0rMNC8i2";

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("User").document(currentId);
// Fetch the document
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Get the 'name' field from the document
                        userName = document.getString("name");
                        Log.d("UserInfo", "Name: " + userName);
                    } else {
                        Log.d("UserInfo", "No such document");
                    }
                } else {
                    Log.d("UserInfo", "get failed with ", task.getException());
                }
            }
        });


        String finalCurrentId = currentId;
        attendanceRecordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity2.this, ScannedStudentsSubject.class);
                intent.putExtra("currentId", finalCurrentId);
                intent.putExtra("userName", userName);
                startActivity(intent);
            }
        });

        accountinformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity2.this, AccountInfoStudent.class);
                startActivity(intent);
            }
        });

        scanqr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(HomeActivity2.this, ScanQR.class);
                startActivity(i);
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences preferences = getSharedPreferences("logincredential", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.clear();
                editor.apply();

                firebaseAuth.signOut();
                Intent intent = new Intent(HomeActivity2.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
}
