package com.example.qrcodes;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    ProgressDialog progressDialog;
    Button login;
    TextView reset, signup;
    EditText number, email, password;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        firebaseAuth = FirebaseAuth.getInstance();

        sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);

        reset = findViewById(R.id.reset);
        signup = findViewById(R.id.goToSignup);

        email = findViewById(R.id.emailaddress1);
        password = findViewById(R.id.password1);

        progressDialog = new ProgressDialog(this);
        login = findViewById(R.id.login);


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emails, passwords;
                emails = String.valueOf(email.getText());
                passwords = String.valueOf(password.getText());

                if (TextUtils.isEmpty(emails)) {
                    Toast.makeText(LoginActivity.this, "ENTER AN EMAIL ADDRESS", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(passwords)) {
                    Toast.makeText(LoginActivity.this, "ENTER A PASSWORD", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (passwords.length() < 6) {
                    Toast.makeText(LoginActivity.this, "PASSWORD IS TOO SHORT MUST BE 6 CHARACTERS LENGTH", Toast.LENGTH_SHORT).show();
                    return;
                }

                //TODO [Student Side] Delete this later
                //add a filter for instant login for the credential used for testing
                if (emails.equals("sample_student@bulsu.edu.ph") && passwords.equals("helloworld")) {
                    Toast.makeText(LoginActivity.this, "LOGGED IN AS STUDENT SUCCESSFULLY", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), HomeActivity2.class));
                } else {
                    progressDialog.setTitle("Please Verify your Email");
                    progressDialog.show();

                    firebaseAuth.signInWithEmailAndPassword(emails, passwords)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        FirebaseUser user = firebaseAuth.getCurrentUser();

                                        if (user != null && user.isEmailVerified()) {
//                                        if (true) {
                                            if (Objects.equals(user.getDisplayName(), "Student")) {
                                                Toast.makeText(LoginActivity.this, "LOGGED IN AS STUDENT SUCCESSFULLY", Toast.LENGTH_SHORT).show();

                                                // Get the user ID
                                                String userId = user.getUid();
                                                // Save the user ID to SharedPreferences
                                                saveUserIdToSharedPreferences(userId);

                                                startActivity(new Intent(getApplicationContext(), HomeActivity2.class));
                                            } else if (Objects.equals(user.getDisplayName(), "Teacher")) {
                                                // Get the user ID
                                                String userId = user.getUid();
                                                // Save the user ID to SharedPreferences
                                                saveUserIdToSharedPreferences(userId);

                                                Toast.makeText(LoginActivity.this, "LOGGED IN AS TEACHER SUCCESSFULLY", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(LoginActivity.this, "INVALID ROLE", Toast.LENGTH_SHORT).show();

                                            }
                                        } else {
                                            Toast.makeText(LoginActivity.this, "LOG IN FAILED: EMAIL NOT VERIFIED", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(LoginActivity.this, "LOG IN FAILED: INVALID EMAIL OR PASSWORD", Toast.LENGTH_SHORT).show();
                                    }
                                    progressDialog.dismiss();

                                }
                            });
                }
            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emails = String.valueOf(email.getText());
                if (TextUtils.isEmpty(emails)) {
                    Toast.makeText(LoginActivity.this, "ENTER AN EMAIL ADDRESS", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressDialog.setTitle("Sending Mail");
                progressDialog.show();

                firebaseAuth.sendPasswordResetEmail(emails)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(LoginActivity.this, "Email Sent", Toast.LENGTH_SHORT).show();
                                progressDialog.cancel();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                progressDialog.cancel();
                            }
                        });
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, Signup.class));
            }
        });


        String userId = retrieveUserIdFromSharedPreferences();

        if (userId != null) {
            Toast.makeText(this, "Unclosed session detected.", Toast.LENGTH_SHORT).show();
            Log.d("sharepreferences0", "onCreate: " + userId);
            startActivity(new Intent(getApplicationContext(), HomeActivity2.class));
        } else {
            // Handle the case where the user ID is not found in SharedPreferences
        }

    }

    private void saveUserIdToSharedPreferences(String userId) {
        SharedPreferences preferences = getSharedPreferences("logincredential", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("sessionID", userId);
        editor.apply();
    }

    private String retrieveUserIdFromSharedPreferences() {
        SharedPreferences preferences = getSharedPreferences("logincredential", MODE_PRIVATE);
        return preferences.getString("sessionID", null);
    }

}
