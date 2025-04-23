package com.kostify.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

public class Login extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    private SessionManager sessionManager;

    private Button btnGoogleLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Inisialisasi FirebaseAuth dan SessionManager
        mAuth = FirebaseAuth.getInstance();
        sessionManager = new SessionManager(this);

        // Setup Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // ganti dengan ID Client dari Firebase Console
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        btnGoogleLogin = findViewById(R.id.btnlogingoogle);
        btnGoogleLogin.setOnClickListener(v -> signInWithGoogle());
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result from Google Sign-In
        if (requestCode == RC_SIGN_IN) {
            GoogleSignIn.getSignedInAccountFromIntent(data)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Sign in success, authenticate with Firebase
                            GoogleSignInAccount account = task.getResult();
                            firebaseAuthWithGoogle(account);
                        } else {
                            // Sign in failed, show a message
                            Log.w("Login", "Google sign-in failed", task.getException());
                            Toast.makeText(Login.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d("Login", "firebaseAuthWithGoogle:" + account.getId());

        // Get Firebase credentials with Google ID Token
        com.google.firebase.auth.AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // If sign in succeeds, check OTP status and navigate
                        FirebaseUser user = mAuth.getCurrentUser();
                        sessionManager.createLoginSession(user.getEmail());

                        // Skip OTP check for Google login
                        if (user != null) {
                            // For Google login, directly navigate to home
                            startActivity(new Intent(Login.this, menu_utama_navigasi.class));
                            finish();
                        } else {
                            // Handle user sign-in failure
                            Log.w("Login", "signInWithCredential failed", task.getException());
                            Toast.makeText(Login.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // If sign in fails
                        Log.w("Login", "signInWithCredential failed", task.getException());
                        Toast.makeText(Login.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkOtpStatusAndNavigate(String email) {
        // For Google login, skip OTP check
        if (mAuth.getCurrentUser() != null) {
            // This step is skipped for Google login.
            startActivity(new Intent(Login.this, menu_utama_navigasi.class));
            finish();
            return;
        }

        // Proceed with OTP check for non-Google login
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("otp_verification")
                .document(email)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String status = document.getString("status");
                        if ("verified".equals(status)) {
                            // If OTP verified, navigate to home page
                            startActivity(new Intent(Login.this, menu_utama_navigasi.class));
                            finish();
                        } else {
                            // If OTP not verified, navigate to OTP page
                            Intent intent = new Intent(Login.this, OTP.class);
                            intent.putExtra("EMAIL", email);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        // OTP document not found, skip OTP check for Google login
                        Toast.makeText(Login.this, "OTP status not found.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Login.this, menu_utama_navigasi.class));
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w("OTP Verification", "Error checking OTP status", e);
                    Toast.makeText(Login.this, "Error checking OTP status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(Login.this, menu_utama_navigasi.class));
            finish();
            return;
        }

        if (mAuth.getCurrentUser() != null) {
            String email = mAuth.getCurrentUser().getEmail();
            checkOtpStatusAndNavigate(email);
        }
    }

    public void registrasi(View view) {
        Intent intent = new Intent(Login.this, Registrasi.class);
        startActivity(intent);
    }

    public void notifikasi(View view) {
        Intent intent = new Intent(Login.this, menu_utama_navigasi.class);
        startActivity(intent);
    }
}
