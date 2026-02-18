package com.example.mygvp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

// Import login activities
import com.example.mygvp.admin.AdminLoginActivity;
import com.example.mygvp.faculty.FacultyLoginActivity;
import com.example.mygvp.student.StudentLoginActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnAdmin, btnFaculty, btnStudent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load main screen layout
        setContentView(R.layout.activity_main);

        // Bind UI elements
        btnAdmin = findViewById(R.id.btnAdmin);
        btnFaculty = findViewById(R.id.btnFaculty);
        btnStudent = findViewById(R.id.btnStudent);

        // Admin Login
        btnAdmin.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AdminLoginActivity.class);
            startActivity(intent);
        });

        // Faculty Login
        btnFaculty.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FacultyLoginActivity.class);
            startActivity(intent);
        });

        // Student Login
        btnStudent.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, StudentLoginActivity.class);
            startActivity(intent);
        });
    }
}
