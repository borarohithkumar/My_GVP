package com.example.mygvp.student;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mygvp.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Properties;
import java.util.Random;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etCode, etNewPassword;
    private TextInputLayout layoutCode, layoutNewPass;
    private MaterialButton btnAction;
    private DatabaseReference dbRef;
    private String userKey, generatedCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        etEmail = findViewById(R.id.etResetEmail);
        etCode = findViewById(R.id.etResetCode);
        etNewPassword = findViewById(R.id.etNewPassword);
        layoutCode = findViewById(R.id.layoutCode);
        layoutNewPass = findViewById(R.id.layoutNewPass);
        btnAction = findViewById(R.id.btnResetAction);

        dbRef = FirebaseDatabase.getInstance().getReference("students");

        btnAction.setOnClickListener(v -> {
            if (btnAction.getText().toString().equals("Send Code")) {
                sendCode();
            } else {
                resetPassword();
            }
        });
    }

    private void sendCode() {
        String email = etEmail.getText().toString().trim();
        if (email.isEmpty()) {
            etEmail.setError("Email required");
            return;
        }

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean found = false;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (email.equals(ds.child("email").getValue(String.class))) {
                        userKey = ds.getKey();
                        generatedCode = String.valueOf(new Random().nextInt(899999) + 100000);

                        // 1. Save to Firebase
                        dbRef.child(userKey).child("resetCode").setValue(generatedCode);

                        // 2. Send Real Email
                        executeEmailTask(email, generatedCode);

                        // 3. Update UI
                        layoutCode.setVisibility(View.VISIBLE);
                        layoutNewPass.setVisibility(View.VISIBLE);
                        btnAction.setText("Reset Password");
                        etEmail.setEnabled(false);
                        found = true;
                        break;
                    }
                }
                if (!found)
                    Toast.makeText(ForgotPasswordActivity.this, "Email not registered!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void resetPassword() {
        String inputCode = etCode.getText().toString().trim();
        String newPass = etNewPassword.getText().toString().trim();

        if (inputCode.isEmpty() || newPass.isEmpty()) {
            Toast.makeText(this, "Please enter OTP and new password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (inputCode.equals(generatedCode)) {
            dbRef.child(userKey).child("password").setValue(newPass);
            dbRef.child(userKey).child("resetCode").removeValue();
            Toast.makeText(this, "Password reset successful!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Invalid OTP!", Toast.LENGTH_SHORT).show();
        }
    }

    private void executeEmailTask(String recipientEmail, String code) {
        new Thread(() -> {
            final String username = "your-college-email@gmail.com"; // YOUR EMAIL
            final String password = "your-app-password"; // YOUR GOOGLE APP PASSWORD

            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");

            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(username));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
                message.setSubject("GVP Portal: Password Reset Code");
                message.setText("Hello,\n\nYour verification code for the GVP Student Portal is: " + code + "\n\nIf you did not request this, please ignore this email.");

                Transport.send(message);

                runOnUiThread(() -> Toast.makeText(ForgotPasswordActivity.this, "OTP sent to your email", Toast.LENGTH_LONG).show());

            } catch (MessagingException e) {
                runOnUiThread(() -> Toast.makeText(ForgotPasswordActivity.this, "Mail Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}