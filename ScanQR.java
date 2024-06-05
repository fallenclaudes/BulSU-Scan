package com.example.qrcodes;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ScanQR extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private FusedLocationProviderClient fusedLocationClient;
    private String currentLocation;

    private EditText name, id;
    private String selectedSubject, status;
    private String section;
    private FirebaseFirestore firestore;
    FirebaseFirestore fstore;
    private CollectionReference scannedDataCollection;
    private FirebaseUser currentUser;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qr);

        name = findViewById(R.id.Namescan);
        id = findViewById(R.id.idnummberscan);



        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Button btn_scan = findViewById(R.id.scan_btn);
        btn_scan.setOnClickListener(view -> requestLocationPermission());

        firestore = FirebaseFirestore.getInstance();
        fstore = FirebaseFirestore.getInstance();
        scannedDataCollection = firestore.collection("scannedData");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userId = currentUser.getUid();

        name.setFocusable(false);
        id.setFocusable(false);

        DocumentReference documentReference = fstore.collection("User").document(userId);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    String userName = documentSnapshot.getString("name");
                    String userNumber = documentSnapshot.getString("number");
                    name.setText(userName);
                    id.setText(userNumber);
                } else {
                    Toast.makeText(ScanQR.this, "User data not found", Toast.LENGTH_SHORT).show();
                }



            }
        });

    }


    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            scanQRCode();
        }
    }

    private void scanQRCode() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Scan");
        integrator.setBeepEnabled(true);
        integrator.setCameraId(0);
        integrator.setBarcodeImageEnabled(true);
        integrator.initiateScan();
    }

    private void addStudentData(String professorId, String scannedData, String section) {
        String sName = name.getText().toString();
        String sId = id.getText().toString();
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String userId = currentUser.getUid();
        try {
            JSONObject qrPayload = new JSONObject(scannedData);
            this.section = qrPayload.getString("section"); // Extract the section value from the QR payload
            selectedSubject = qrPayload.getString("subject");
            status = qrPayload.getString("status");
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Invalid QR code format", Toast.LENGTH_SHORT).show();
            return; // Return early if the QR code format is invalid
        }


        Map<String, Object> scannedDataMap = new HashMap<>();
        scannedDataMap.put("name", sName);
        scannedDataMap.put("id", sId);
        scannedDataMap.put("subject", selectedSubject);
        scannedDataMap.put("section", this.section);
        scannedDataMap.put("date", currentDate);
        scannedDataMap.put("userId", userId);
        scannedDataMap.put("professorId", professorId);
        scannedDataMap.put("status", status);

        Log.d("ScannedDataMap", "Name: " + sName);
        Log.d("ScannedDataMap", "ID: " + sId);
        Log.d("ScannedDataMap", "Subject: " + selectedSubject);
        Log.d("ScannedDataMap", "Section: " + this.section);
        Log.d("ScannedDataMap", "Date: " + currentDate);
        Log.d("ScannedDataMap", "UserID: " + userId);
        Log.d("ScannedDataMap", "ProfessorID: " + professorId);
        Log.d("ScannedDataMap", "Status: " + status);

        FirebaseFirestore db;
        db = FirebaseFirestore.getInstance();

        db.collection("students")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean bool = false;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d("DocumentCheck", document.getId() + " => " + document.getData());

                            if (document.getString("userId").equals(userId) &&
                                    document.getString("professorId").equals(professorId) &&
                                        document.getString("section").equals(this.section) &&
                                    document.getString("subject").equals(selectedSubject)){

                                bool = true;
                                Log.d("DocumentCheck", "Document exists for userId: " + userId);
                                scannedDataCollection.add(scannedDataMap)
                                        .addOnSuccessListener(documentReference -> {
                                            Toast.makeText(ScanQR.this, "Attendance Marked Successfully", Toast.LENGTH_SHORT).show();


                                            //delete duplicate files
//                                            FirebaseFirestore db = FirebaseFirestore.getInstance();

// Define the collection and fields to check for duplicates
                                            String collectionPath = "scannedData";
                                            String[] fieldsToCheck = {"date", "id", "name", "professorId", "section", "subject", "userId", "status"};

// Use a Map to keep track of seen values
                                            Map<String, Boolean> seenValues = new HashMap<>();

                                            db.collection(collectionPath)
                                                    .get()
                                                    .addOnCompleteListener(task1 -> {
                                                        if (task1.isSuccessful()) {
                                                            for (QueryDocumentSnapshot document1 : task1.getResult()) {
                                                                // Concatenate values of specified fields
                                                                StringBuilder keyBuilder = new StringBuilder();
                                                                for (String field : fieldsToCheck) {
                                                                    Object value = document1.get(field);
                                                                    keyBuilder.append(value != null ? value.toString() : "").append("-");
                                                                }
                                                                String key = keyBuilder.toString();

                                                                // Check if the key is already seen
                                                                if (seenValues.containsKey(key)) {
                                                                    // Duplicate found, delete the document
                                                                    String documentId = document1.getId();
                                                                    deleteDocument(collectionPath, documentId);
                                                                    Toast.makeText(this, "A duplicate scan document was found and deleted.", Toast.LENGTH_SHORT).show();
                                                                } else {
                                                                    // Mark the key as seen
                                                                    seenValues.put(key, true);
                                                                }
                                                            }
                                                        } else {
                                                            // Handle errors
                                                            Exception e = task1.getException();
                                                            if (e != null) {
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                    });
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(ScanQR.this, "Failed to mark attendance", Toast.LENGTH_SHORT).show();
                                        });
                            } else {
//                                Toast.makeText(this, "You are not a student in this subject.", Toast.LENGTH_SHORT).show();
                            }
                        }

                        if (bool==false){
                            Toast.makeText(this, "You are not a student in this subject.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("FirestoreQuery", "Error getting documents: " + task.getException());
                    }
                });






        String documentId = scannedData;
    }

    private void deleteDocument(String collectionPath, String documentId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(collectionPath).document(documentId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Document successfully deleted
                    System.out.println("Document deleted: " + documentId);
                    Toast.makeText(this, "A duplicate record was deleted", Toast.LENGTH_SHORT).show();
                });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            String scannedData = result.getContents();
            if (scannedData != null) {

                try {
                    JSONObject qrPayload = new JSONObject(scannedData);
                    String professorId = qrPayload.getString("professorId");
                    String section = qrPayload.getString("section");
                    selectedSubject = qrPayload.getString("subject");


                    getLocationAndAddData(professorId, scannedData);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Invalid QR code format", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void getLocationAndAddData(String professorId, String scannedData) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
        } else {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    //TODO: Delete this before forwarding. Switch to BULSU Campus Coordinate
                    //my coordinate here in CamSur
                   // double desiredLatitude = 13.673887247503236;
                  //  double desiredLongitude =  123.5196883730142;

                    //Coordinate in jm campus
                  //   double desiredLatitude= 14.942612713156436;
                   //   double desiredLongitude= 120.89596084360976;
                    //    double maxDistance = 1500;

                    //Coordinate in Bulsu campus
                   double desiredLatitude  = 14.954426752284874;
                    double desiredLongitude = 120.91100901908806;
                    double maxDistance = 1500;

                    float[] results = new float[1];
                    Location.distanceBetween(latitude, longitude, desiredLatitude, desiredLongitude, results);
                    float distance = results[0];

                    if (distance <= maxDistance) {
//                    if (true) {
                        currentLocation = latitude + "," + longitude;
                        addStudentData(professorId, scannedData, section);
                    } else {
                        Toast.makeText(ScanQR.this, "You are not in BulSU - Bustos Campus.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ScanQR.this, "Location not available", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scanQRCode();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
