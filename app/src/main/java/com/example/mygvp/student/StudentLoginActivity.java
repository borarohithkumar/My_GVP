package com.example.mygvp.student;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mygvp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StudentLoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private DatabaseReference studentsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_student_login);

        // Views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        // Firebase reference
        studentsRef = FirebaseDatabase
                .getInstance()
                .getReference("students");

        btnLogin.setOnClickListener(v -> loginStudent());
    }

    private void loginStudent() {

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this,
                    "Please enter email and password",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // READ STUDENTS' DATA ONCE
        studentsRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                boolean isFound = false;

                for (DataSnapshot studentSnap : snapshot.getChildren()) {

                    String dbEmail = studentSnap.child("email").getValue(String.class);
                    String dbPassword = studentSnap.child("password").getValue(String.class);

                    if (dbEmail == null || dbPassword == null) continue;

                    if (email.equals(dbEmail) && password.equals(dbPassword)) {

                        String rollNo = studentSnap.getKey(); // roll number

                        // grab the student's name from the database (fallback to "Student" if not found)
                        String studentName = studentSnap.child("name").getValue(String.class);
                        if (studentName == null || studentName.isEmpty()) {
                            studentName = "Student";
                        }

                        android.content.SharedPreferences prefs = getSharedPreferences("MyGVP_UserPrefs", MODE_PRIVATE);
                        android.content.SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("LOGGED_IN_ROLL_NO", rollNo);
                        editor.putString("LOGGED_IN_NAME", studentName);
                        editor.apply();

                        Toast.makeText(StudentLoginActivity.this,
                                "Login successful",
                                Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(
                                StudentLoginActivity.this,
                                StudentDashboardActivity.class
                        );
                        intent.putExtra("rollNo", rollNo);

                        // prevent going back to login
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                        startActivity(intent);
                        isFound = true;
                        break;
                    }
                }

                if (!isFound) {
                    Toast.makeText(StudentLoginActivity.this,
                            "Invalid email or password",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StudentLoginActivity.this,
                        "Firebase error: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
