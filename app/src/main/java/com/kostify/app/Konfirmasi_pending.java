package com.kostify.app;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;
public class Konfirmasi_pending extends Fragment {

    private TextView textPenyewa, textGrupWhatsapp;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate layout
        View view = inflater.inflate(R.layout.fragment_konfirmasi_pending, container, false);

        // Inisialisasi Firestore
        db = FirebaseFirestore.getInstance();

        // Ambil ID dari layout
        textPenyewa = view.findViewById(R.id.textpenyewa);
        textGrupWhatsapp = view.findViewById(R.id.textgrupwhatsapp);

        // Ambil userId dari Bundle
        String userId = getArguments() != null ? getArguments().getString("userId") : null;

        if (userId != null) {
            db.collection("users")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String nama = documentSnapshot.getString("nama");
                            String nowa = documentSnapshot.getString("nowhatsapp");

                            textPenyewa.setText("Penyewa : " + (nama != null ? nama : "Tidak diketahui"));
                            textGrupWhatsapp.setText("No.Whatsapp : " + (nowa != null ? nowa : "Tidak diketahui"));
                        } else {
                            textPenyewa.setText("Penyewa : Tidak ditemukan");
                            textGrupWhatsapp.setText("No.Whatsapp : Tidak ditemukan");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Konfirmasi_pending", "Gagal ambil data: " + e.getMessage());
                        textPenyewa.setText("Penyewa : Gagal memuat");
                        textGrupWhatsapp.setText("No.Whatsapp : Gagal memuat");
                    });
        }

        return view;
    }
}


