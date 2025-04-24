package com.kostify.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.appcompat.app.AlertDialog;

import android.text.InputType;

import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.firestore.FieldValue;
public class Login extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private EditText txtEmail, txtPassword;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        txtEmail = findViewById(R.id.txtemail);
        txtPassword = findViewById(R.id.txtpassword);
        sessionManager = new SessionManager(this);

        TextView txtLupaPassword = findViewById(R.id.textlupa);
        txtLupaPassword.setOnClickListener(v -> showResetPasswordDialog());

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    public void registrasi(View view) {
        Intent intent = new Intent(Login.this, Registrasi.class);
        startActivity(intent);
    }

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
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
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
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            sessionManager.createLoginSession(user.getEmail());
                        }
                        Intent intent = new Intent(Login.this, menu_utama_navigasi.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(Login.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showResetPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset Password");
        builder.setMessage("Masukkan email untuk menerima link reset password");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        input.setHint("Email");
        builder.setView(input);

        builder.setPositiveButton("Kirim", (dialog, which) -> {
            String email = input.getText().toString().trim();
            if (!email.isEmpty()) {
                sendPasswordResetEmail(email);
            } else {
                Toast.makeText(this, "Email tidak boleh kosong", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Batal", null);
        builder.show();
    }

    private void sendPasswordResetEmail(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(
                                Login.this,
                                "Email reset password telah dikirim ke " + email,
                                Toast.LENGTH_LONG
                        ).show();
                    } else {
                        Toast.makeText(
                                Login.this,
                                "Gagal mengirim email: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Cek apakah user sudah login melalui session
        if (sessionManager.isLoggedIn()) {
            String email = sessionManager.getUserEmail();  // Ambil email dari session
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

            // Jika FirebaseUser valid dan email terverifikasi
            if (currentUser != null && currentUser.getEmail().equals(email) && currentUser.isEmailVerified()) {
                startActivity(new Intent(Login.this, menu_utama_navigasi.class));
                finish();
            } else {
                // Jika email tidak terverifikasi atau pengguna tidak login dengan benar
                Toast.makeText(Login.this, "Email belum terverifikasi atau sesi kadaluarsa", Toast.LENGTH_LONG).show();
                sessionManager.logoutUser();  // Logout jika ada masalah
            }
        }
    }




    public void loginWithEmail(View view) {
        String email = txtEmail.getText().toString().trim();
        String password = txtPassword.getText().toString().trim();

        if(email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email dan password wajib diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if(task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if(user != null) {
                            user.reload().addOnCompleteListener(reloadTask -> {
                                if(user.isEmailVerified()) {
                                    sessionManager.createLoginSession(user.getEmail());
                                    updateVerificationStatus(user.getUid());
                                    startActivity(new Intent(Login.this, menu_utama_navigasi.class));
                                    finish();
                                } else {
                                    Toast.makeText(
                                            Login.this,
                                            "Email belum diverifikasi! Cek email Anda",
                                            Toast.LENGTH_LONG
                                    ).show();
                                    mAuth.signOut();
                                }
                            });
                        }
                    } else {
                        Toast.makeText(
                                Login.this,
                                "Login gagal: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private void updateVerificationStatus(String userId) {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .update("terverifikasi", true)
                .addOnFailureListener(e -> {
                    Log.e("FIRESTORE", "Gagal update status verifikasi: " + e.getMessage());
                });
    }
}
