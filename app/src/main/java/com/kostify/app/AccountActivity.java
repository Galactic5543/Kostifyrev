package com.kostify.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import androidx.appcompat.app.AlertDialog;

public class AccountActivity extends AppCompatActivity {
    private TextView tvNama, tvEmail, tvTelepon;
    private EditText etNama, etEmail, etTelepon;
    private Button btnEdit, btnSave, btnCancel;
    private LinearLayout containerSaveCancel;
    private FirebaseFirestore db;
    private String userId;
    private user originalUser;  // Menggunakan class lowercase
    private Button btnDeleteAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        // Inisialisasi view
        tvNama = findViewById(R.id.tvNama);
        tvEmail = findViewById(R.id.tvEmail);
        tvTelepon = findViewById(R.id.tvTelepon);
        etNama = findViewById(R.id.etNama);
        etEmail = findViewById(R.id.etEmail);
        etTelepon = findViewById(R.id.etTelepon);
        btnEdit = findViewById(R.id.btnEdit);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        containerSaveCancel = findViewById(R.id.containerSaveCancel);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
        btnDeleteAccount.setOnClickListener(v -> showDeleteConfirmationDialog());

        // Firestore setup
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Load data
        loadUserData();

        // Button listeners
        btnEdit.setOnClickListener(v -> enterEditMode());
        btnCancel.setOnClickListener(v -> cancelEdit());
        btnSave.setOnClickListener(v -> saveChanges());
    }

    private void loadUserData() {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    originalUser = documentSnapshot.toObject(user.class);  // Diubah ke lowercase
                    if(originalUser != null) {
                        tvNama.setText(originalUser.nama);
                        tvEmail.setText(originalUser.email);
                        tvTelepon.setText(originalUser.telepon);

                        etNama.setText(originalUser.nama);
                        etEmail.setText(originalUser.email);
                        etTelepon.setText(originalUser.telepon);
                    }
                });
    }

    private void enterEditMode() {
        // Tampilkan EditText dan sembunyikan TextView
        tvNama.setVisibility(View.GONE);
        etNama.setVisibility(View.VISIBLE);
        tvEmail.setVisibility(View.GONE);
        etEmail.setVisibility(View.VISIBLE);
        tvTelepon.setVisibility(View.GONE);
        etTelepon.setVisibility(View.VISIBLE);

        // Ganti tombol
        btnEdit.setVisibility(View.GONE);
        containerSaveCancel.setVisibility(View.VISIBLE);
    }

    private void cancelEdit() {
        // Kembalikan nilai awal
        etNama.setText(originalUser.nama);
        etEmail.setText(originalUser.email);
        etTelepon.setText(originalUser.telepon);
        exitEditMode();
    }

    private void saveChanges() {
        user updatedUser = new user(  // Diubah ke lowercase
                etNama.getText().toString(),
                etEmail.getText().toString(),
                etTelepon.getText().toString()
        );

        // Update ke Firestore
        db.collection("users").document(userId)
                .set(updatedUser)
                .addOnSuccessListener(aVoid -> {
                    tvNama.setText(updatedUser.nama);
                    tvEmail.setText(updatedUser.email);
                    tvTelepon.setText(updatedUser.telepon);
                    exitEditMode();
                    Toast.makeText(this, "Perubahan disimpan", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal menyimpan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void exitEditMode() {
        // Tampilkan TextView dan sembunyikan EditText
        tvNama.setVisibility(View.VISIBLE);
        etNama.setVisibility(View.GONE);
        tvEmail.setVisibility(View.VISIBLE);
        etEmail.setVisibility(View.GONE);
        tvTelepon.setVisibility(View.VISIBLE);
        etTelepon.setVisibility(View.GONE);

        // Ganti tombol
        btnEdit.setVisibility(View.VISIBLE);
        containerSaveCancel.setVisibility(View.GONE);
    }
    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Akun")
                .setMessage("Apakah Anda yakin ingin menghapus akun? Tindakan ini tidak dapat dibatalkan!")
                .setPositiveButton("Hapus", (dialog, which) -> deleteAccount())
                .setNegativeButton("Batal", null)
                .show();
    }

    private void deleteAccount() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (currentUser != null) {
            // Hapus data dari Firestore
            db.collection("users").document(currentUser.getUid())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        // Hapus akun dari Firebase Auth
                        currentUser.delete()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        // Redirect ke halaman login
                                        startActivity(new Intent(this, Login.class));
                                        finish();
                                        Toast.makeText(this, "Akun berhasil dihapus", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(this, "Gagal menghapus akun: " + task.getException(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Gagal menghapus data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}