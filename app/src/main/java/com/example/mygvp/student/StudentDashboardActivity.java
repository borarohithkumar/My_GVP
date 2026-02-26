package com.example.mygvp.student;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.mygvp.LostAndFoundActivity;
import com.example.mygvp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StudentDashboardActivity extends AppCompatActivity {

    private TextView tvName;
    private ImageView imgProfile, imgSettings; // Added settings for Change Password/Logout

    private CardView cardAttendance, cardFee, cardAchievement,
            cardResults, cardLostFound, cardSports;

    private DatabaseReference studentRef;
    private String rollNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        // UI references
        tvName = findViewById(R.id.tvName);
        imgProfile = findViewById(R.id.imgProfile);
        imgSettings = findViewById(R.id.imgSettings); // Make sure to add this ID in XML

        cardAttendance = findViewById(R.id.cardAttendance);
        cardFee = findViewById(R.id.cardFee);
        cardAchievement = findViewById(R.id.cardAchievement);
        cardResults = findViewById(R.id.cardResults);
        cardLostFound = findViewById(R.id.cardLostFound);
        cardSports = findViewById(R.id.cardSports);

        // Get roll number from Intent
        rollNo = getIntent().getStringExtra("rollNo");

        if (rollNo == null || rollNo.isEmpty()) {
            finish(); // Close if no roll number is found
            return;
        }

        // Firebase reference to specific student
        studentRef = FirebaseDatabase.getInstance()
                .getReference("students")
                .child(rollNo);

        loadStudentProfile();
        setupDashboardClicks();
    }

    private void loadStudentProfile() {
        studentRef.addValueEventListener(new ValueEventListener() { // Using addValueEventListener for real-time updates
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                String name = snapshot.child("name").getValue(String.class);
                String imageUrl = snapshot.child("imageUrl").getValue(String.class);

                tvName.setText(name != null ? "Welcome, " + name : "Welcome, Student");

                // Loading Profile Image with Glide
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Glide.with(StudentDashboardActivity.this)
                            .load(imageUrl)
                            .apply(RequestOptions.circleCropTransform()) // Makes image circular
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .error(R.drawable.ic_profile_placeholder)
                            .into(imgProfile);
                } else {
                    imgProfile.setImageResource(R.drawable.ic_profile_placeholder);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StudentDashboardActivity.this, "Database Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupDashboardClicks() {
        // Change Password / Settings Click
        imgSettings.setOnClickListener(v -> {
            // This is where we will implement the Change Password logic later
            Toast.makeText(this, "Settings / Change Password Clicked", Toast.LENGTH_SHORT).show();
        });

        cardResults.setOnClickListener(v -> {
            Intent intent = new Intent(StudentDashboardActivity.this, StudentResultsActivity.class);
            intent.putExtra("rollNo", rollNo);
            startActivity(intent);
        });

        cardAchievement.setOnClickListener(v -> {
            // We will point this to your new UploadAchievementActivity soon
            Toast.makeText(this, "Opening Achievements...", Toast.LENGTH_SHORT).show();
        });

        cardLostFound.setOnClickListener(v -> {
            startActivity(new Intent(StudentDashboardActivity.this, LostAndFoundActivity.class));
        });

        cardAttendance.setOnClickListener(v -> {
            Toast.makeText(this, "Opening Attendance...", Toast.LENGTH_SHORT).show();
        });

        cardFee.setOnClickListener(v -> {
            Toast.makeText(this, "Opening Fee Payment...", Toast.LENGTH_SHORT).show();
        });

        cardSports.setOnClickListener(v -> {
            Toast.makeText(this, "Opening Sports...", Toast.LENGTH_SHORT).show();
        });
    }
}
