package com.example.qrcodes;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
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

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class Signup2 extends AppCompatActivity {

    ProgressDialog progressDialog;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;
    Button signup;

    EditText name,  email, password,confirmPassword;
    private EditText midtermStartDateEditText;
    private EditText midtermEndDateEditText;
    private EditText finalsStartDateEditText;
    private EditText finalsEndDateEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup2);

        confirmPassword = findViewById(R.id.password5);
        email = findViewById(R.id.emailaddress);
        password = findViewById(R.id.password);
        name = findViewById(R.id.fullname);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        signup = findViewById(R.id.signup);


        //for calendars
        // Initialize EditText fields
        midtermStartDateEditText = findViewById(R.id.midtermStartDateEditText2);
        midtermEndDateEditText = findViewById(R.id.midtermEndDateEditText2);
        finalsStartDateEditText = findViewById(R.id.finalsStartDatePicker2);
        finalsEndDateEditText = findViewById(R.id.finalsEndDatePicker2);

        // Set OnClickListener for date picker EditText fields
        midtermStartDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(midtermStartDateEditText);
            }
        });

        midtermEndDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(midtermEndDateEditText);
            }
        });

        finalsStartDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(finalsStartDateEditText);
            }
        });

        finalsEndDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(finalsEndDateEditText);}
        });



        progressDialog = new ProgressDialog(this);

        signup.setOnClickListener(view -> {
            String emails, passwords, names,confirmPasswords;
            emails = String.valueOf(email.getText());
            passwords = String.valueOf(password.getText());
            confirmPasswords = String.valueOf(confirmPassword.getText());
            names = String.valueOf(name.getText());


            if (TextUtils.isEmpty(names)) {
                Toast.makeText(Signup2.this, "ENTER YOUR FULL NAME", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(emails)) {
                Toast.makeText(Signup2.this, "ENTER AN EMAIL ADDRESS", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(passwords)) {
                Toast.makeText(Signup2.this, "ENTER A PASSWORD", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(confirmPasswords)) {
                Toast.makeText(Signup2.this, "ENTER A PASSWORD", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!passwords.equals(confirmPasswords)) {
                Toast.makeText(Signup2.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.length() < 6) {
                Toast.makeText(Signup2.this, "PASSWORD IS TOO SHORT MUST BE 6 CHARACTERS LENGTH", Toast.LENGTH_SHORT).show();
                return;
            }

//            Checking if dates are empty

// Check if EditText fields are empty before proceeding
            if (TextUtils.isEmpty(midtermStartDateEditText.getText().toString())) {
                // Handle the case where midtermStartDateEditText is empty
                Toast.makeText(this, "Midterm Start Date cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(midtermEndDateEditText.getText().toString())) {
                // Handle the case where midtermEndDateEditText is empty
                Toast.makeText(this, "Midterm End Date cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(finalsStartDateEditText.getText().toString())) {
                // Handle the case where finalsStartDateEditText is empty
                Toast.makeText(this, "Finals Start Date cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(finalsEndDateEditText.getText().toString())) {
                // Handle the case where finalsEndDateEditText is empty
                Toast.makeText(this, "Finals End Date cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }


            // Checking if end dates are not earlier than start dates
            if (isEndDateBeforeStartDate(midtermStartDateEditText.getText().toString(), midtermEndDateEditText.getText().toString())) {
                Toast.makeText(Signup2.this, "Midterm End Date should not be earlier than Midterm Start Date", Toast.LENGTH_SHORT).show();
                return;
            }

            // Checking if end dates are not earlier than start dates
            if (isEndDateBeforeStartDate(midtermEndDateEditText.getText().toString(), finalsStartDateEditText.getText().toString())) {
                Toast.makeText(Signup2.this, "Finals Start Date should not be earlier than Midterm End Date", Toast.LENGTH_SHORT).show();
                return;
            }


            if (isEndDateBeforeStartDate(finalsStartDateEditText.getText().toString(), finalsEndDateEditText.getText().toString())) {
                Toast.makeText(Signup2.this, "Finals End Date should not be earlier than Finals Start Date", Toast.LENGTH_SHORT).show();
                return;
            }

            if (midtermStartDateEditText.getText().toString().equals(midtermEndDateEditText.getText().toString())){
                Toast.makeText(this, "Midterm dates cannot be equal", Toast.LENGTH_SHORT).show();
            }
            if (finalsStartDateEditText.getText().toString().equals(finalsEndDateEditText.getText().toString())){
                Toast.makeText(this, "Finals dates cannot be equal", Toast.LENGTH_SHORT).show();
            }
            if(midtermEndDateEditText.getText().toString().equals(finalsStartDateEditText.getText().toString())){
                Toast.makeText(this, "Midterm end date and finals start date can't be simultaneous", Toast.LENGTH_SHORT).show();
                return;
            }





            //Checking if the email ends in bulsu.edu.ph
            if (!emails.endsWith("bulsu.edu.ph")) {
                Toast.makeText(Signup2.this, "Email should end with 'bulsu.edu.ph'", Toast.LENGTH_SHORT).show();
            }
            else {
                progressDialog.show();

                firebaseAuth.createUserWithEmailAndPassword(emails, passwords)
                        .addOnCompleteListener(Signup2.this, task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = firebaseAuth.getCurrentUser();
                                if (user != null) {

                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                            .setDisplayName("Teacher")
                                            .build();
                                    user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            startActivity(new Intent(getApplicationContext(), LoginActivity2.class));
                                                            Toast.makeText(Signup2.this, "REGISTRATION SUCCESSFUL PLEASE VERIFY YOUR EMAIL ID", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            Toast.makeText(Signup2.this, "EMAIL VERIFICATION FAILED", Toast.LENGTH_SHORT).show();
                                                        }

                                                    }
                                                });
                                            } else {
                                                Toast.makeText(Signup2.this, "Role Error", Toast.LENGTH_SHORT).show();
                                            }

                                        }
                                    });
                                }
                            } else {
                                Toast.makeText(Signup2.this, "EMAIL ID IS ALREADY USED", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnSuccessListener(authResult -> {
                            progressDialog.cancel();

                            //These dates has the correct format of YYYY-mm-DD
                            String midtermStartDate = midtermStartDateEditText.getText().toString();
                            String midtermEndDate = midtermEndDateEditText.getText().toString();
                            String finalsStartDate = finalsStartDateEditText.getText().toString();
                            String finalsEndDate = finalsEndDateEditText.getText().toString();

                            Log.d("DateValues", "Midterm Start Date: " + midtermStartDate);
                            Log.d("DateValues", "Midterm End Date: " + midtermEndDate);
                            Log.d("DateValues", "Finals Start Date: " + finalsStartDate);
                            Log.d("DateValues", "Finals End Date: " + finalsEndDate);

//                            // Convert the original date strings to the desired format "yyyy-MM-dd"
//                            String convertedMidtermStartDate = convertDateFormat(midtermStartDate);
//                            String convertedMidtermEndDate = convertDateFormat(midtermEndDate);
//                            String convertedFinalsStartDate = convertDateFormat(finalsStartDate);
//                            String convertedFinalsEndDate = convertDateFormat(finalsEndDate);
//// Log the converted dates
//                            Log.d("Converted Dates", "Midterm Start Date: " + convertedMidtermStartDate);
//                            Log.d("Converted Dates", "Midterm End Date: " + convertedMidtermEndDate);
//                            Log.d("Converted Dates", "Finals Start Date: " + convertedFinalsStartDate);
//                            Log.d("Converted Dates", "Finals End Date: " + convertedFinalsEndDate);



                            firestore.collection("TeachersUser")
                                    .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                                    .set(new UserModelTeacher(emails, names, midtermStartDate, midtermEndDate, finalsStartDate, finalsEndDate))
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // Document successfully written
                                            Log.d("Firestore", "UserModel added with ID: " + FirebaseAuth.getInstance().getUid());
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Handle errors
                                            Log.e("Firestore", "Error adding document", e);
                                        }
                                    });
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(Signup2.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            progressDialog.cancel();
                        });
            }
        });
    }

    private String convertDateFormat(String originalDate) {
        try {
            // Define the original date format
            SimpleDateFormat originalFormat = new SimpleDateFormat("MM-dd-yyyy");

            // Parse the original date
            Date date = originalFormat.parse(originalDate);

            // Define the new date format
            SimpleDateFormat newFormat = new SimpleDateFormat("yyyy-MM-dd");

            // Format the date in the new format
            return newFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null; // Handle the exception or return a default value
        }
    }

    // Method to display DatePickerDialog
    private void showDatePickerDialog(final EditText editText) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // Set the selected date in the EditText
                        String selectedDate = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
                        editText.setText(selectedDate);
                    }
                }, year, month, day);

        datePickerDialog.show();

    }


    // Helper method to check if end date is before start date
    private boolean isEndDateBeforeStartDate(String startDate, String endDate) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            Date start = dateFormat.parse(startDate);
            Date end = dateFormat.parse(endDate);

            return end.before(start);
        } catch (ParseException e) {
            e.printStackTrace();
            return false; // Handle the exception or return a default value
        }
    }
}