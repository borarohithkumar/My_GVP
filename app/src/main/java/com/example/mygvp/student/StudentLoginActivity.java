package com.example.mygvp.student;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mygvp.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StudentLoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private DatabaseReference studentsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_login);

        // Bind Views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        // Firebase Reference
        studentsRef = FirebaseDatabase.getInstance().getReference("students");

        // Login Action
        btnLogin.setOnClickListener(v -> loginStudent());
    }

    private void loginStudent() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        studentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isFound = false;

                for (DataSnapshot studentSnap : snapshot.getChildren()) {
                    String dbEmail = studentSnap.child("email").getValue(String.class);
                    String dbPassword = studentSnap.child("password").getValue(String.class);

                    if (dbEmail != null && dbPassword != null &&
                            email.equals(dbEmail) && password.equals(dbPassword)) {

                        String rollNo = studentSnap.getKey();
                        String studentName = studentSnap.child("name").getValue(String.class);
                        if (studentName == null || studentName.isEmpty()) {
                            studentName = "Student";
                        }

                        // Save Session in SharedPreferences
                        SharedPreferences prefs = getSharedPreferences("MyGVP_UserPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("LOGGED_IN_ROLL_NO", rollNo);
                        editor.putString("LOGGED_IN_NAME", studentName);
                        editor.apply();

                        Toast.makeText(StudentLoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

                        // Navigate to Dashboard
                        Intent intent = new Intent(StudentLoginActivity.this, StudentDashboardActivity.class);
                        intent.putExtra("rollNo", rollNo);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                        isFound = true;
                        break;
                    }
                }

                if (!isFound) {
                    Toast.makeText(StudentLoginActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StudentLoginActivity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}