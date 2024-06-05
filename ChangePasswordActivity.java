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

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText oldPasswordEditText;
    private EditText newPasswordEditText;
    private EditText confirmNewPasswordEditText;
    private Button changePasswordButton;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        firebaseAuth = FirebaseAuth.getInstance();
        oldPasswordEditText = findViewById(R.id.oldPasswordEditText);
        newPasswordEditText = findViewById(R.id.newPasswordEditText);
        confirmNewPasswordEditText = findViewById(R.id.confirmNewPasswordEditText);
        changePasswordButton = findViewById(R.id.changePasswordButton);

        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String oldPassword = oldPasswordEditText.getText().toString().trim();
                String newPassword = newPasswordEditText.getText().toString().trim();
                String confirmNewPassword = confirmNewPasswordEditText.getText().toString().trim();

                if (!oldPassword.isEmpty() && !newPassword.isEmpty() && !confirmNewPassword.isEmpty()) {
                    if (newPassword.equals(confirmNewPassword)) {
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
                                                    Toast.makeText(ChangePasswordActivity.this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                                                    finish();
                                                } else {
                                                    Toast.makeText(ChangePasswordActivity.this, "Failed to change password", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    } else {
                                        Toast.makeText(ChangePasswordActivity.this, "Authentication failed. Please check your old password.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(ChangePasswordActivity.this, "User not signed in", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ChangePasswordActivity.this, "New Password and Confirm Password do not match", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ChangePasswordActivity.this, "Please enter old, new, and confirm passwords", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
