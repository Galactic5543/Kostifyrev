package com.kostify.app;

import android.os.Bundle;
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

import java.util.concurrent.TimeUnit;

public class otpjava extends AppCompatActivity {

    private EditText inputOTP;
    private Button btnVerifikasi;
    private String verificationId;
    private FirebaseAuth mAuth;
    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp); // Pastikan ini file layout yang benar

        mAuth = FirebaseAuth.getInstance();

        inputOTP = findViewById(R.id.txtotp1); // ID yang baru
        btnVerifikasi = findViewById(R.id.btnkonfirmasi); // ID yang baru

        phoneNumber = getIntent().getStringExtra("phone_number");

        if (phoneNumber != null) {
            kirimKodeVerifikasi(phoneNumber);
        }

        btnVerifikasi.setOnClickListener(v -> {
            String kode = inputOTP.getText().toString().trim();
            if (!kode.isEmpty() && verificationId != null) {
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, kode);
                verifikasiDenganFirebase(credential);
            }
        });
    }

    private void kirimKodeVerifikasi(String nomorHP) {
        // Hapus karakter selain angka dan hapus 0 di depan jika ada
        String nomorBersih = nomorHP.replaceAll("[^\\d]", ""); // hanya angka
        if (nomorBersih.startsWith("0")) {
            nomorBersih = nomorBersih.substring(1); // buang angka 0 pertama
        }

        String nomorFinal = "+62" + nomorBersih;

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(nomorFinal)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(callbacks)
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }


    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                    verifikasiDenganFirebase(credential);
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    Toast.makeText(otpjava.this, "Gagal mengirim OTP: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onCodeSent(@NonNull String verifId,
                                       @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    super.onCodeSent(verifId, token);
                    verificationId = verifId;
                    Toast.makeText(otpjava.this, "Kode OTP dikirim", Toast.LENGTH_SHORT).show();
                }
            };

    private void verifikasiDenganFirebase(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(otpjava.this, "Verifikasi berhasil!", Toast.LENGTH_SHORT).show();
                        // Intent ke halaman selanjutnya bisa ditambahkan di sini
                    } else {
                        Toast.makeText(otpjava.this, "Verifikasi gagal: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
