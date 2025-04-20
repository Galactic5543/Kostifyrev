package com.kostify.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import android.view.View;

import androidx.activity.EdgeToEdge;

public class Registrasi extends AppCompatActivity {

    private EditText inputEmail, inputPassword, inputPhone;
    private Button btnDaftar;
    private FirebaseFirestore db;
    private String generatedOTP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrasi);

        db = FirebaseFirestore.getInstance();

        inputEmail = findViewById(R.id.txtemail);
        inputPassword = findViewById(R.id.txtpassword);
        inputPhone = findViewById(R.id.txtwhatsapp);
        btnDaftar = findViewById(R.id.btndaftar);

        btnDaftar.setOnClickListener(v -> {
            String email = inputEmail.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();
            String phone = inputPhone.getText().toString().trim();

            if(email.isEmpty() || password.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Isi semua field!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Generate OTP 6 digit
            generatedOTP = generateOTP();

            // Simpan ke Firestore
            Map<String, Object> otpData = new HashMap<>();
            otpData.put("otp", generatedOTP);
            otpData.put("created_at", System.currentTimeMillis());
            otpData.put("status", "unverified");

            db.collection("otp_verification")
                    .document(email)
                    .set(otpData)
                    .addOnSuccessListener(aVoid -> {
                        // Kirim OTP via email (Untuk production gunakan Firebase Functions)
                        sendOTPEmail(email, generatedOTP);

                        // Pindah ke halaman OTP
                        Intent intent = new Intent(Registrasi.this, OTP.class);
                        intent.putExtra("EMAIL", email);
                        startActivity(intent);
                    })
                    .addOnFailureListener(e -> Toast.makeText(
                            Registrasi.this,
                            "Gagal menyimpan OTP: " + e.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show());
        });
    }

    private String generateOTP() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    private void sendOTPEmail(String email, String otp) {
        // Implementasi pengiriman email disini
        // Contoh sederhana untuk testing:
        Toast.makeText(
                this,
                "OTP: " + otp + " dikirim ke " + email,
                Toast.LENGTH_LONG
        ).show();
    }




}