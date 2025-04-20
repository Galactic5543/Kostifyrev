package com.kostify.app;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class OTP extends AppCompatActivity {

    private FirebaseFirestore db;
    private String userEmail;
    private EditText[] otpFields;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        db = FirebaseFirestore.getInstance();
        userEmail = getIntent().getStringExtra("EMAIL");
        setupOTPFields();
    }

    private void setupOTPFields() {
        otpFields = new EditText[]{
                findViewById(R.id.txtotp1),
                findViewById(R.id.txtotp2),
                findViewById(R.id.txtotp3),
                findViewById(R.id.txtotp4),
                findViewById(R.id.txtotp5),
                findViewById(R.id.txtotp6)
        };

        // Auto-focus dan input handling
        for(int i = 0; i < otpFields.length; i++) {
            final int currentIndex = i;
            otpFields[i].setOnKeyListener((v, keyCode, event) -> {
                if(otpFields[currentIndex].getText().toString().length() == 1) {
                    if(currentIndex < 5) {
                        otpFields[currentIndex + 1].requestFocus();
                    }
                }
                return false;
            });
        }
    }

    public void verifyOTP(View view) {
        String enteredOTP = getEnteredOTP();

        db.collection("otp_verification")
                .document(userEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        DocumentSnapshot doc = task.getResult();
                        if(doc.exists()) {
                            String storedOTP = doc.getString("otp");
                            long createdAt = doc.getLong("created_at");

                            // Cek expired (5 menit)
                            if((System.currentTimeMillis() - createdAt) > 300000) {
                                Toast.makeText(
                                        this,
                                        "OTP sudah kadaluarsa",
                                        Toast.LENGTH_SHORT
                                ).show();
                                return;
                            }

                            if(enteredOTP.equals(storedOTP)) {
                                // Update status verifikasi
                                db.collection("otp_verification")
                                        .document(userEmail)
                                        .update("status", "verified")
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(
                                                    this,
                                                    "Verifikasi berhasil!",
                                                    Toast.LENGTH_SHORT
                                            ).show();
                                            // Lanjut ke halaman utama
                                            startActivity(new Intent(this, Navigation.class));
                                            finish();
                                        });
                            } else {
                                Toast.makeText(
                                        this,
                                        "OTP salah",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                    }
                });
    }

    private String getEnteredOTP() {
        StringBuilder sb = new StringBuilder();
        for(EditText field : otpFields) {
            sb.append(field.getText().toString());
        }
        return sb.toString();
    }
    public void Navigasi(View view) {
        Intent intent = new Intent(OTP.this, Navigation.class);
        startActivity(intent);
    }
}