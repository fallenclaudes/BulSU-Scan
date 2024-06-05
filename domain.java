package com.example.qrcodes;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class domain extends AppCompatActivity  {

    private Button teacher ,student;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_domain);

        teacher=findViewById(R.id.teacher);

        teacher.setOnClickListener(view -> {
            Intent intent=new Intent(domain.this ,LoginActivity2.class);
            startActivity(intent);
        });
        student=findViewById(R.id.student);

        student.setOnClickListener(view -> {
            Intent intent=new Intent(domain.this ,LoginActivity.class);
            startActivity(intent);
        });
    }
}