package com.kostify.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class window_data_profil extends Fragment {

    private static final String TAG = "WindowDataProfil";

    private EditText txtNama, txtNoTelp;
    private Button btnSimpan;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public window_data_profil() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_window_data_profil, container, false);

        // Inisialisasi Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Inisialisasi UI
        txtNama = view.findViewById(R.id.txtnama);
        txtNoTelp = view.findViewById(R.id.txtnotelp);
        btnSimpan = view.findViewById(R.id.btnsimpan);

        ambilDataUser();

        // Tombol simpan diklik
        btnSimpan.setOnClickListener(v -> updateDataUser());

        return view;
    }

    private void ambilDataUser() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "User belum login", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        txtNama.setText(doc.getString("nama") != null ? doc.getString("nama") : "");
                        txtNoTelp.setText(doc.getString("telepon") != null ? doc.getString("telepon") : "");
                    } else {
                        Toast.makeText(getContext(), "Data tidak ditemukan", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Gagal ambil data user", e);
                    Toast.makeText(getContext(), "Gagal mengambil data", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateDataUser() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "User belum login", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = mAuth.getCurrentUser().getUid();
        String namaBaru = txtNama.getText().toString().trim();
        String telpBaru = txtNoTelp.getText().toString().trim();

        if (namaBaru.isEmpty() || telpBaru.isEmpty()) {
            Toast.makeText(getContext(), "Field tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("nama", namaBaru);
        updateData.put("telepon", telpBaru);

        db.collection("users").document(uid)
                .update(updateData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Data berhasil diperbarui", Toast.LENGTH_SHORT).show();

                    // Pindah ke menu_utama_navigasi dan arahkan ke fragment Profil
                    Intent intent = new Intent(requireContext(), menu_utama_navigasi.class);
                    intent.putExtra("fragment", "profil");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Gagal update data", e);
                    Toast.makeText(getContext(), "Gagal memperbarui data", Toast.LENGTH_SHORT).show();
                });
    }
}
