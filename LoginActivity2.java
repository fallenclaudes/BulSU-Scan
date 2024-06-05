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

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity2 extends AppCompatActivity {

    Button login;
    TextView reset, signup;
    EditText email, password;
    FirebaseAuth firebaseAuth;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);
        firebaseAuth = FirebaseAuth.getInstance();
        email = findViewById(R.id.emailaddress1);
        reset = findViewById(R.id.reset);
        login = findViewById(R.id.login1);
        signup = findViewById(R.id.goToSignup);
        password = findViewById(R.id.password1);

        progressDialog = new ProgressDialog(this);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emails, passwords;
                emails = String.valueOf(email.getText());
                passwords = String.valueOf(password.getText());

                Log.d("YourTag", "Emails: " + emails);
                Log.d("YourTag", "Passwords: " + passwords);

                if (TextUtils.isEmpty(emails)) {
                    Toast.makeText(LoginActivity2.this, "ENTER AN EMAIL ADDRESS", Toast.LENGTH_SHORT).show();
                }
                    if (TextUtils.isEmpty(passwords)) {
                        Toast.makeText(LoginActivity2.this, "ENTER A PASSWORD", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (passwords.length() < 6) {
                        Toast.makeText(LoginActivity2.this, "PASSWORD IS TOO SHORT MUST BE 6 CHARACTERS LENGTH", Toast.LENGTH_SHORT).show();
                        return;
                    }


                    //TODO [Teacher Side] Delete this later
                    //add a filter for instant login for the credential used for testing
                        progressDialog.setTitle("Please Verify your Email");
                        progressDialog.show();

                        firebaseAuth.signInWithEmailAndPassword(emails, passwords)
                                .addOnCompleteListener(LoginActivity2.this, task -> {
                                    if (task.isSuccessful()) {
                                        FirebaseUser user = firebaseAuth.getCurrentUser();
                                        if (user != null && user.isEmailVerified()) {
                                            if (user.getDisplayName().equals("Teacher")) {
                                                Toast.makeText(LoginActivity2.this, "LOGGED IN AS TEACHER SUCCESSFULLY", Toast.LENGTH_SHORT).show();

                                                // Get the user ID
                                                String userId = user.getUid();
                                                // Save the user ID to SharedPreferences
                                                saveUserIdToSharedPreferences(userId);

                                                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                                            } else if (user.getDisplayName().equals("Student")) {
                                                Toast.makeText(LoginActivity2.this, "YOU CANNOT USE THIS BECAUSE YOU ARE A STUDENT", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(LoginActivity2.this, "INVALID ROLE", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(LoginActivity2.this, "LOG IN FAILED: EMAIL NOT VERIFIED", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(LoginActivity2.this, "LOG IN FAILED: INVALID EMAIL OR PASSWORD", Toast.LENGTH_SHORT).show();
                                    }
                                    progressDialog.dismiss();
                                });
                }

        });


        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emails = String.valueOf(email.getText());
                if (TextUtils.isEmpty(emails)) {
                    Toast.makeText(LoginActivity2.this, "ENTER AN EMAIL ADDRESS", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressDialog.setTitle("Sending Mail");
                progressDialog.show();

                firebaseAuth.sendPasswordResetEmail(emails)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(LoginActivity2.this, "Email Sent", Toast.LENGTH_SHORT).show();
                            progressDialog.cancel();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(LoginActivity2.this, "Failed to send reset email: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            progressDialog.cancel();
                        });
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity2.this, Signup2.class));
            }
        });


        String userId = retrieveUserIdFromSharedPreferences();

        if (userId != null) {
            Toast.makeText(this, "Unclosed session detected.", Toast.LENGTH_SHORT).show();
            Log.d("sharepreferences0", "onCreate: " + userId);
            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
        } else {
            // Handle the case where the user ID is not found in SharedPreferences
        }
    }

    private void saveUserIdToSharedPreferences(String userId) {
        SharedPreferences preferences = getSharedPreferences("logincredentialteacher", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("sessionID", userId);
        editor.apply();
    }

    private String retrieveUserIdFromSharedPreferences() {
        SharedPreferences preferences = getSharedPreferences("logincredentialteacher", MODE_PRIVATE);
        return preferences.getString("sessionID", null);
    }
}
