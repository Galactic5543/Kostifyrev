package com.kostify.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Pastikan client ID sudah ada di strings.xml
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    public void registrasi(View view) {
        Intent intent = new Intent(Login.this, Registrasi.class);
        startActivity(intent);
    }

    // Method untuk login dengan Google
    public void signInWithGoogle(View view) {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In berhasil, autentikasi dengan Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In gagal
                Log.w("GoogleSignIn", "Google sign in failed", e);
                Toast.makeText(this, "Authentication failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success
                        Intent intent = new Intent(Login.this, menu_utama_navigasi.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // If sign in fails
                        Toast.makeText(Login.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null) {
            // Tambahkan pengecekan status verifikasi OTP
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String email = mAuth.getCurrentUser().getEmail();

            db.collection("otp_verification")
                    .document(email)
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            String status = document.getString("status");
                            if ("verified".equals(status)) {
                                // Kalau sudah terverifikasi, masuk ke halaman utama
                                startActivity(new Intent(Login.this, menu_utama_navigasi.class));
                                finish();
                            } else {
                                // Kalau belum, arahkan ke OTP page
                                Intent intent = new Intent(Login.this, OTP.class);
                                intent.putExtra("EMAIL", email);
                                startActivity(intent);
                                finish();
                            }
                        }
                    });
        }
    }


    public void notifikasi(View view) {
        Intent intent = new Intent(Login.this, menu_utama_navigasi.class);
        startActivity(intent);
    }
}