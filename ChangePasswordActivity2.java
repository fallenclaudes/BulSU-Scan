package com.example.qrcodes;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity2 extends AppCompatActivity {

    private EditText oldPasswordEditText;
    private EditText newPasswordEditText;
    private EditText confirmNewPasswordEditText;
    private Button changePasswordButton;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password2);

        firebaseAuth = FirebaseAuth.getInstance();
        oldPasswordEditText = findViewById(R.id.oldPasswordEditText);
        newPasswordEditText = findViewById(R.id.newPasswordEditText);
        confirmNewPasswordEditText = findViewById(R.id.confirmNewPasswordEditText); // Initialize password confirmation EditText
        changePasswordButton = findViewById(R.id.changePasswordButton);

        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String oldPassword = oldPasswordEditText.getText().toString().trim();
                String newPassword = newPasswordEditText.getText().toString().trim();
                String confirmPassword = confirmNewPasswordEditText.getText().toString().trim(); // Get confirmation password

                if (!oldPassword.isEmpty() && !newPassword.isEmpty()) {
                    if (newPassword.equals(confirmPassword)) { // Check if the new password matches the confirmation password
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            // Reauthenticate the user with their old password
                            String email = user.getEmail();
                            AuthCredential credential = EmailAuthProvider.getCredential(email, oldPassword);
                            user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        // Change the password
                                        user.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(ChangePasswordActivity2.this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                                                    finish();
                                                } else {
                                                    Toast.makeText(ChangePasswordActivity2.this, "Failed to change password", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    } else {
                                        Toast.makeText(ChangePasswordActivity2.this, "Authentication failed. Please check your old password.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            // User is not signed in
                            Toast.makeText(ChangePasswordActivity2.this, "User not signed in", Toast.LENGTH_SHORT).show();
                        }
                    } else {

                        Toast.makeText(ChangePasswordActivity2.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ChangePasswordActivity2.this, "Please enter old and new passwords", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
