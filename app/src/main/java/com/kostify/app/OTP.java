package com.kostify.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.TimeUnit;

public class OTP extends AppCompatActivity {

    private EditText[] otpFields;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private String userEmail;      // untuk Firestore
    private String phoneNumber;    // untuk Firebase Auth
    private String verificationId; // untuk Firebase Auth

    private Button btnVerifikasi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        otpFields = new EditText[]{
                findViewById(R.id.txtotp1),
                findViewById(R.id.txtotp2),
                findViewById(R.id.txtotp3),
                findViewById(R.id.txtotp4),
                findViewById(R.id.txtotp5),
                findViewById(R.id.txtotp6)
        };

        btnVerifikasi = findViewById(R.id.btnkonfirmasi);

        // Auto move cursor
        setupOTPFields();

        // Ambil data dari intent
        userEmail = getIntent().getStringExtra("EMAIL");
        phoneNumber = getIntent().getStringExtra("PHONE");

        if (phoneNumber != null) {
            kirimKodeVerifikasi(phoneNumber);
        }

        btnVerifikasi.setOnClickListener(v -> {
            String otpCode = getEnteredOTP();

            if (phoneNumber != null && verificationId != null) {
                // Verifikasi dengan Firebase (nomor HP)
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otpCode);
                verifikasiDenganFirebase(credential);
            } else if (userEmail != null) {
                // Verifikasi dengan Firestore (email)
                verifikasiDenganFirestore(otpCode);
            } else {
                Toast.makeText(this, "Data tidak lengkap", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Kirim OTP via Firebase ke nomor HP
    private void kirimKodeVerifikasi(String nomorHP) {
        String nomorBersih = nomorHP.replaceAll("[^\\d]", "");
        if (nomorBersih.startsWith("0")) {
            nomorBersih = nomorBersih.substring(1);
        }
        String nomorFinal = "+62" + nomorBersih;

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(nomorFinal)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(callbacks)
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    // Callback Firebase
    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                    verifikasiDenganFirebase(credential);
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    Toast.makeText(OTP.this, "Gagal mengirim OTP: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onCodeSent(@NonNull String verifId,
                                       @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    verificationId = verifId;
                    Toast.makeText(OTP.this, "Kode OTP dikirim", Toast.LENGTH_SHORT).show();
                }
            };

    private void verifikasiDenganFirebase(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Verifikasi berhasil!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, Navigation.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Verifikasi gagal: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void verifikasiDenganFirestore(String kodeOTP) {
        db.collection("otp_verification")
                .document(userEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot doc = task.getResult();
                        if (doc.exists()) {
                            String storedOTP = doc.getString("otp");
                            long createdAt = doc.getLong("created_at");

                            if ((System.currentTimeMillis() - createdAt) > 300000) {
                                Toast.makeText(this, "OTP sudah kadaluarsa", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            if (kodeOTP.equals(storedOTP)) {
                                db.collection("otp_verification")
                                        .document(userEmail)
                                        .update("status", "verified")
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(this, "Verifikasi berhasil!", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(this, Navigation.class));
                                            finish();
                                        });
                            } else {
                                Toast.makeText(this, "OTP salah", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private void setupOTPFields() {
        for (int i = 0; i < otpFields.length; i++) {
            final int index = i;
            otpFields[i].setOnKeyListener((v, keyCode, event) -> {
                if (otpFields[index].getText().toString().length() == 1 && index < otpFields.length - 1) {
                    otpFields[index + 1].requestFocus();
                }
                return false;
            });
        }
    }

    private String getEnteredOTP() {
        StringBuilder sb = new StringBuilder();
        for (EditText field : otpFields) {
            sb.append(field.getText().toString());
        }
        return sb.toString();
    }

    public void Navigasi(View view) {
        startActivity(new Intent(this, Navigation.class));
    }
}
