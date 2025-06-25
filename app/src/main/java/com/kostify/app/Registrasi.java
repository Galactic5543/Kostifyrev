package com.kostify.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Date;
import java.util.Random;
import android.view.View;


import androidx.activity.EdgeToEdge;

public class Registrasi extends AppCompatActivity {

    private EditText inputEmail, inputPassword, inputPhone;
    private Button btnOtpEmail;
    private FirebaseFirestore db;
    private String generatedOTP;
    private Button btnVerifikasiLink, btnPilihOtp;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrasi);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        inputEmail = findViewById(R.id.txtemail);
        inputPassword = findViewById(R.id.txtpassword);
        inputPhone = findViewById(R.id.txtwhatsapp);
        btnVerifikasiLink = findViewById(R.id.btnVerifikasiLink);
        btnPilihOtp = findViewById(R.id.btnPilihOtp);

        btnVerifikasiLink.setOnClickListener(v -> {
            String email = inputEmail.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();
            String phone = inputPhone.getText().toString().trim();
            String name = ((EditText) findViewById(R.id.txtnama)).getText().toString().trim();

            if (email.isEmpty() || password.isEmpty() || phone.isEmpty() || name.isEmpty()) {
                Toast.makeText(this, "Semua field wajib diisi!", Toast.LENGTH_SHORT).show();
                return;
            }

            // 1. Buat akun di Firebase Auth
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // 2. Simpan data ke Firestore
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                saveUserToFirestore(user.getUid(), name, email, phone);

                                // 3. Kirim email verifikasi
                                user.sendEmailVerification()
                                        .addOnCompleteListener(emailTask -> {
                                            if (emailTask.isSuccessful()) {
                                                Toast.makeText(
                                                        this,
                                                        "Link verifikasi dikirim ke email",
                                                        Toast.LENGTH_SHORT
                                                ).show();
                                                startActivity(new Intent(this, Login.class));
                                                finish();
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(
                                    this,
                                    "Registrasi gagal: " + task.getException(),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });
        });
    }

    // Di dalam method sendVerificationLink()
    private void sendVerificationLink(String email) {
        // Ambil data dari input
        String password = inputPassword.getText().toString().trim();
        String phone = inputPhone.getText().toString().trim();
        String name = ((EditText) findViewById(R.id.txtnama)).getText().toString().trim();

        // Validasi input
        if (email.isEmpty() || password.isEmpty() || phone.isEmpty() || name.isEmpty()) {
            Toast.makeText(this, "Semua field wajib diisi!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Registrasi user dengan email & password
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user != null) {
                            // Simpan data tambahan ke Firestore
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("nama", name);
                            userData.put("email", email);
                            userData.put("telepon", phone);
                            userData.put("tanggal_daftar", new Date());

                            FirebaseFirestore.getInstance()
                                    .collection("users")
                                    .document(user.getUid())
                                    .set(userData)
                                    .addOnSuccessListener(aVoid -> {
                                        // Kirim email verifikasi
                                        user.sendEmailVerification()
                                                .addOnCompleteListener(emailTask -> {
                                                    if (emailTask.isSuccessful()) {
                                                        Toast.makeText(
                                                                Registrasi.this,
                                                                "Registrasi berhasil! Cek email untuk verifikasi",
                                                                Toast.LENGTH_LONG
                                                        ).show();
                                                        startActivity(new Intent(Registrasi.this, Login.class));
                                                        finish();
                                                    } else {
                                                        Toast.makeText(
                                                                Registrasi.this,
                                                                "Gagal mengirim email verifikasi: " + emailTask.getException(),
                                                                Toast.LENGTH_SHORT
                                                        ).show();
                                                    }
                                                });
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(
                                                Registrasi.this,
                                                "Gagal menyimpan data: " + e.getMessage(),
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    });
                        }
                    } else {
                        // Handle error registrasi
                        Toast.makeText(
                                Registrasi.this,
                                "Registrasi gagal: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    public void login(View view) {
        Intent intent = new Intent(Registrasi.this, Login.class);
        startActivity(intent);
    }

    private void saveUserToFirestore(String userId, String name, String email, String phone) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("nama", name);
        userData.put("email", email);
        userData.put("telepon", phone);
        userData.put("tanggal_daftar", FieldValue.serverTimestamp()); // Tambahkan ini

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .set(userData)
                .addOnFailureListener(e -> {
                    Toast.makeText(
                            this,
                            "Gagal menyimpan data: " + e.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }


}