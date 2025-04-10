package com.kostify.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Registrasi extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registrasi);

    }
    public void login(View view) {
        Intent intent = new Intent(Registrasi.this, Login.class);
        startActivity(intent);
    }

    public void otp(View view) {
        Intent intent = new Intent(Registrasi.this, OTP.class);
        startActivity(intent);
    }


}