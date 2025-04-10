package com.kostify.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class Login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

    }

    public void registrasi(View view) {
        Intent intent = new Intent(Login.this, Registrasi.class);
        startActivity(intent);
    }

    public void notifikasi(View view) {
        Intent intent = new Intent(Login.this, Navigation.class);
        startActivity(intent);
    }

}

