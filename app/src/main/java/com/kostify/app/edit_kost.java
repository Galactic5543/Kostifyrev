package com.kostify.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class edit_kost extends Fragment {

    private static final String TAG = "EditKostFragment";

    private EditText alamat, linkWhatsapp, peraturan;
    private Button btnSimpan;

    private String keyKost, currentUserId;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_kost, container, false);

        // Firebase init
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Ambil ID kost dari SharedPreferences yang disimpan di fragment pemilik
        SharedPreferences prefs = requireContext().getSharedPreferences("kostPrefs", Context.MODE_PRIVATE);
        keyKost = prefs.getString("selectedKostId", "");

        if (keyKost == null || keyKost.isEmpty()) {
            Toast.makeText(getContext(), "ID Kost tidak ditemukan di SharedPreferences", Toast.LENGTH_SHORT).show();
            return view;
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "User belum login", Toast.LENGTH_SHORT).show();
            return view;
        }

        currentUserId = user.getUid();

        // Inisialisasi UI
        alamat = view.findViewById(R.id.txtalamat);
        linkWhatsapp = view.findViewById(R.id.txtlinkgrupwhatsapp);
        peraturan = view.findViewById(R.id.txtperaturan);
        btnSimpan = view.findViewById(R.id.btnsimpan);

        view.findViewById(R.id.ic_back).setOnClickListener(v -> backToPemilik());
        btnSimpan.setOnClickListener(v -> simpanData());

        // Tampilkan data
        tampilkanData();

        return view;
    }


    private void tampilkanData() {
        db.collection("kost").document(keyKost)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        alamat.setText(document.getString("alamat"));
                        peraturan.setText(document.getString("peraturan"));
                        linkWhatsapp.setText(document.getString("link_grup"));
                    } else {
                        Toast.makeText(getContext(), "Data kost tidak ditemukan", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Gagal mengambil data kost", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Gagal mengambil data dari Firestore", e);
                });
    }

    private void simpanData() {
        String alamatVal = alamat.getText().toString().trim();
        String peraturanVal = peraturan.getText().toString().trim();
        String waLink = linkWhatsapp.getText().toString().trim();

        if (alamatVal.isEmpty() || peraturanVal.isEmpty()) {
            Toast.makeText(getContext(), "Semua field harus diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        HashMap<String, Object> dataMap = new HashMap<>();
        dataMap.put("alamat", alamatVal);
        dataMap.put("peraturan", peraturanVal);
        dataMap.put("link_grup", waLink);

        db.collection("kost").document(keyKost)
                .update(dataMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Data berhasil diperbarui", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Gagal memperbarui data", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Gagal update dokumen", e);
                });
    }

    private void backToPemilik() {
        Pemilik pemilikFragment = new Pemilik();
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.frameLayout, pemilikFragment)
                .addToBackStack(null)
                .commit();
    }
}
