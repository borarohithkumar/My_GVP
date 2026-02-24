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
import com.example.mygvp.LostAndFoundActivity;
import com.example.mygvp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StudentDashboardActivity extends AppCompatActivity {

    TextView tvName;
    ImageView imgProfile;

    CardView cardAttendance, cardFee, cardAchievement,
            cardResults, cardLostFound, cardSports;

    DatabaseReference studentRef;
    String rollNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        // UI references
        tvName = findViewById(R.id.tvName);
        imgProfile = findViewById(R.id.imgProfile);

        cardAttendance = findViewById(R.id.cardAttendance);
        cardFee = findViewById(R.id.cardFee);
        cardAchievement = findViewById(R.id.cardAchievement);
        cardResults = findViewById(R.id.cardResults);
        cardLostFound = findViewById(R.id.cardLostFound);
        cardSports = findViewById(R.id.cardSports);

        // Get roll number from login
        rollNo = getIntent().getStringExtra("rollNo");

        if (rollNo == null || rollNo.isEmpty()) {
            tvName.setText("Welcome, Student");
            imgProfile.setImageResource(R.drawable.ic_profile_placeholder);
            return;
        }

        // Firebase reference
        studentRef = FirebaseDatabase.getInstance()
                .getReference("students")
                .child(rollNo);

        loadStudentProfile();
        setupDashboardClicks();
    }

    // Load student name & profile image
    private void loadStudentProfile() {

        studentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (!snapshot.exists()) {
                    tvName.setText("Welcome, Student");
                    return;
                }

                String name = snapshot.child("name").getValue(String.class);
                String imageUrl = snapshot.child("imageUrl").getValue(String.class);

                if (name != null && !name.isEmpty()) {
                    tvName.setText("Welcome, " + name);
                } else {
                    tvName.setText("Welcome, Student");
                }

                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Glide.with(StudentDashboardActivity.this)
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .error(R.drawable.ic_profile_placeholder)
                            .into(imgProfile);
                } else {
                    imgProfile.setImageResource(R.drawable.ic_profile_placeholder);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        StudentDashboardActivity.this,
                        "Failed to load student data",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    // Navigate to the Dashboard
    private void setupDashboardClicks() {

        // Navigate to the student results
        cardResults.setOnClickListener(v -> {
            Intent intent = new Intent(
                    StudentDashboardActivity.this,
                    StudentResultsActivity.class
            );
            intent.putExtra("rollNo", rollNo);
            startActivity(intent);
        });

        // Placeholder modules
        cardAttendance.setOnClickListener(v ->
                Toast.makeText(this, "Attendance – Coming Soon", Toast.LENGTH_SHORT).show());

        cardFee.setOnClickListener(v ->
                Toast.makeText(this, "Fee Payment – Coming Soon", Toast.LENGTH_SHORT).show());

        cardAchievement.setOnClickListener(v ->
                Toast.makeText(this, "Achievements – Coming Soon", Toast.LENGTH_SHORT).show());

        // Navigate to the Lost and Found UI
        cardLostFound.setOnClickListener(v -> {
            Intent intent = new Intent(StudentDashboardActivity.this, LostAndFoundActivity.class);
            startActivity(intent);
        });

        cardSports.setOnClickListener(v ->
                Toast.makeText(this, "Sports – Coming Soon", Toast.LENGTH_SHORT).show());
    }
}
