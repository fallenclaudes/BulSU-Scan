package com.example.qrcodes;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Signup extends AppCompatActivity {

    ProgressDialog progressDialog;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;
    Button signup;

    EditText name, number, email, password, confirmpass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        number = findViewById(R.id.number);
        email = findViewById(R.id.emailaddress);
        password = findViewById(R.id.password);
        name = findViewById(R.id.fullname);
        confirmpass = findViewById(R.id.password2);
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        signup = findViewById(R.id.signup);

        progressDialog = new ProgressDialog(this);

        signup.setOnClickListener(view -> {
            String emails, passwords, names, numbers;
            String confirmPassword = confirmpass.getText().toString();
            emails = String.valueOf(email.getText());
            passwords = String.valueOf(password.getText());
            names = String.valueOf(name.getText());
            numbers = String.valueOf(number.getText());

            if (TextUtils.isEmpty(names)) {
                Toast.makeText(Signup.this, "ENTER YOUR FULL NAME", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(emails)) {
                Toast.makeText(Signup.this, "ENTER AN EMAIL ADDRESS", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(passwords)) {
                Toast.makeText(Signup.this, "ENTER A PASSWORD", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(numbers)) {
                Toast.makeText(Signup.this, "ENTER A NUMBER", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(confirmPassword)) {
                Toast.makeText(Signup.this, "ENTER THE SAME PASSWORD", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!TextUtils.equals(passwords, confirmPassword)) {
                Toast.makeText(Signup.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.length() < 6) {
                Toast.makeText(Signup.this, "PASSWORD IS TOO SHORT MUST BE 6 CHARACTERS LENGTH", Toast.LENGTH_SHORT).show();
                return;
            }

            //Checking if the email ends in bulsu.edu.ph
            if (!emails.endsWith("bulsu.edu.ph")) {
                Toast.makeText(Signup.this, "Email should end with 'bulsu.edu.ph'", Toast.LENGTH_SHORT).show();
            }
            else {
                progressDialog.show();
                firebaseAuth.createUserWithEmailAndPassword(emails, passwords)
                        .addOnCompleteListener(Signup.this, task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = firebaseAuth.getCurrentUser();
                                if (user != null) {

                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                            .setDisplayName("Student")
                                            .build();

                                    user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                                                            Toast.makeText(Signup.this, "REGISTRATION SUCCESSFUL PLEASE VERIFY YOUR EMAIL ID", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            Toast.makeText(Signup.this, "EMAIL VERIFICATION FAILED", Toast.LENGTH_SHORT).show();
                                                        }

                                                    }
                                                });
                                            } else {
                                                Toast.makeText(Signup.this, "Role Error", Toast.LENGTH_SHORT).show();
                                            }

                                        }
                                    });
                                }
                            } else {
                                Toast.makeText(Signup.this, "EMAIL ID IS ALREADY USED", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnSuccessListener(authResult -> {
                            progressDialog.cancel();


//                            firestore.collection("User")
//                                    .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
//                                    .set(new UserModel(names, emails, numbers));

                            //Save user data to the firestore
                            String uid = FirebaseAuth.getInstance().getUid();
                            if (uid != null) {
                                Map<String, Object> userData = new HashMap<>();
                                userData.put("name", names);
                                userData.put("number", numbers);
                                userData.put("email", emails);

                                firestore.collection("User")
                                        .document(uid)
                                        .set(userData)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d("Firestore", "DocumentSnapshot successfully written!");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w("Firestore", "Error writing document", e);
                                            }
                                        });
                            }




                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(Signup.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            progressDialog.cancel();
                        });
            }
        });
    }
}