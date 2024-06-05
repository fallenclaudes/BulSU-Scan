package com.example.qrcodes;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;


public class AccountInfoStudent extends AppCompatActivity {
    TextView email, name,number;

    FirebaseAuth mAuth;
    FirebaseFirestore fstore;

    Button changepass;

    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_info_student);

        email = findViewById(R.id.emailaddress2);
        name = findViewById(R.id.name2);
        number = findViewById(R.id.number2);
        changepass = findViewById(R.id.changepassword);

        mAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();

        userId = mAuth.getCurrentUser().getUid();

        name.setFocusable(false);
        number.setFocusable(false);
        email.setFocusable(false);

        DocumentReference documentReference = fstore.collection("User").document(userId);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                email.setText(documentSnapshot.getString("email"));
                name.setText(documentSnapshot.getString("name"));
                number.setText(documentSnapshot.getString("number"));



            }
        });
        changepass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountInfoStudent.this,ChangePasswordActivity2.class);
                startActivity(intent);
            }
        });

    }
}