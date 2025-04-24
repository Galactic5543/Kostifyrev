package com.kostify.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.kostify.app.R;

public class PilihMetodeOtpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pilih_metode_otp);

        Button btnOtpEmail = findViewById(R.id.btnOtpEmail);
        Button btnOtpTelepon = findViewById(R.id.btnOtpTelepon);

        // OTP via Email
        btnOtpEmail.setOnClickListener(v -> {
            Intent intent = new Intent(this, OTP.class);
            startActivity(intent);
        });

        // OTP via Telepon (Masih perlu Blaze Plan)
        btnOtpTelepon.setOnClickListener(v -> {
            Intent intent = new Intent(this, OTP.class);
            startActivity(intent);
        });
    }
}
