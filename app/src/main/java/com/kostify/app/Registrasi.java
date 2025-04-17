package com.kostify.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class Registrasi extends AppCompatActivity {

    private EditText inputEmail, inputPassword, inputPhone;
    private Button btnDaftar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registrasi);

        // Inisialisasi Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Hubungkan ke layout
        inputEmail = findViewById(R.id.txtemail);
        inputPassword = findViewById(R.id.txtpassword);
        inputPhone = findViewById(R.id.txtwhatsapp); // Ini pakai txtwhatsapp sesuai permintaan
        btnDaftar = findViewById(R.id.btndaftar);

        // Listener tombol daftar
        btnDaftar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();
                String phoneNumber = inputPhone.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty() || phoneNumber.isEmpty()) {
                    Toast.makeText(Registrasi.this, "Email, password, dan nomor WhatsApp wajib diisi", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Proses registrasi akun email + password di Firebase
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(Registrasi.this, "Registrasi berhasil", Toast.LENGTH_SHORT).show();

                                // Kirim nomor HP ke halaman OTP
                                Intent intent = new Intent(Registrasi.this, OTP.class);
                                intent.putExtra("phone_number", phoneNumber);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(Registrasi.this, "Gagal: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }
}
