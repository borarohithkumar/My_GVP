package com.example.mygvp.faculty;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mygvp.R;
import com.google.firebase.database.*;

public class FacultyDashboardActivity extends AppCompatActivity {

    TextView tvFacultyName;
    DatabaseReference facultyRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faculty_dashboard);

        tvFacultyName = findViewById(R.id.tvFacultyName);

        String facultyId = getIntent().getStringExtra("facultyId");

        if (facultyId == null) {
            tvFacultyName.setText("Welcome, Faculty");
            return;
        }

        facultyRef = FirebaseDatabase.getInstance()
                .getReference("faculty")
                .child(facultyId);

        facultyRef.child("name").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()) {
                            String name = snapshot.getValue(String.class);
                            tvFacultyName.setText("Welcome, " + name);
                        } else {
                            tvFacultyName.setText("Welcome, Faculty");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(
                                FacultyDashboardActivity.this,
                                "Failed to load faculty data",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }
}