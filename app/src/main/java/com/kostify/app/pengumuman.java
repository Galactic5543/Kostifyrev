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

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class pengumuman extends Fragment {

    private static final String TAG = "PengumumanFragment";

    private EditText txtJudul, txtIsi;
    private Button btnUmumkan;

    private String idKost;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pengumuman, container, false);

        // Inisialisasi Firestore
        db = FirebaseFirestore.getInstance();

        // Ambil ID kost dari SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("kostPrefs", Context.MODE_PRIVATE);
        idKost = prefs.getString("selectedKostId", "");

        if (idKost == null || idKost.isEmpty()) {
            Toast.makeText(getContext(), "ID Kost tidak ditemukan di SharedPreferences", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "ID Kost kosong/null");
            return view;
        }

        // Inisialisasi UI
        txtJudul = view.findViewById(R.id.txtjudul);
        txtIsi = view.findViewById(R.id.txtisi);
        btnUmumkan = view.findViewById(R.id.btnajukankerusakan);

        view.findViewById(R.id.ic_back).setOnClickListener(v -> backToPemilik());
        btnUmumkan.setOnClickListener(v -> umumkanPengumuman());

        return view;
    }

    private void umumkanPengumuman() {
        String judul = txtJudul.getText().toString().trim();
        String isi = txtIsi.getText().toString().trim();

        if (judul.isEmpty() || isi.isEmpty()) {
            Toast.makeText(getContext(), "Judul dan isi pengumuman tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("judul", judul);
        data.put("isi", isi);
        data.put("timestamp", System.currentTimeMillis());

        db.collection("kost")
                .document(idKost)
                .collection("pengumuman")
                .add(data)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Pengumuman berhasil dikirim", Toast.LENGTH_SHORT).show();
                    txtJudul.setText("");
                    txtIsi.setText("");
                    Log.d(TAG, "Pengumuman berhasil ditambahkan ke kost ID: " + idKost);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Gagal mengirim pengumuman", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Gagal menambahkan pengumuman", e);
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
