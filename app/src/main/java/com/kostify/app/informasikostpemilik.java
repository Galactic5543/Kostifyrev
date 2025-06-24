package com.kostify.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class informasikostpemilik extends Fragment {

    private TextView namaKost, alamat, pemilik, whatsapp, linkGrup, isiPeraturan;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public informasikostpemilik() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_informasi_kost_pemilik, container, false);

        // Inisialisasi Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Inisialisasi View dari layout XML
        namaKost = view.findViewById(R.id.namakost);
        alamat = view.findViewById(R.id.alamat);
        pemilik = view.findViewById(R.id.textpemilik);
        whatsapp = view.findViewById(R.id.textwhatsapp);
        linkGrup = view.findViewById(R.id.linkgrupwhatsapp);
        isiPeraturan = view.findViewById(R.id.textisiperaturan);
        ImageView btnBack = view.findViewById(R.id.ic_back);

        // Tombol kembali
        btnBack.setOnClickListener(v -> backToPemilikFragment());

        // Ambil ID kost dari SharedPreferences
        SharedPreferences prefs = getContext().getSharedPreferences("kostPrefs", Context.MODE_PRIVATE);
        String idKost = prefs.getString("selectedKostId", null);

        if (idKost != null) {
            db.collection("kost").document(idKost).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String nama = doc.getString("nama_kost");
                            String alamatKost = doc.getString("alamat");
                            String link = doc.getString("link_grup");
                            String peraturan = doc.getString("peraturan");

                            namaKost.setText(nama != null ? nama : "-");
                            alamat.setText(alamatKost != null ? alamatKost : "-");

                            FirebaseUser user = mAuth.getCurrentUser();
                            String namaPemilik = (user != null && user.getDisplayName() != null) ? user.getDisplayName() : "-";
                            String nomor = (user != null && user.getPhoneNumber() != null) ? user.getPhoneNumber() : "-";

                            pemilik.setText("Pemilik : " + namaPemilik);
                            whatsapp.setText("No. Whatsapp : " + nomor);

                            if (link != null && !link.isEmpty()) {
                                linkGrup.setText(link);
                                linkGrup.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_blue_dark));
                                linkGrup.setPaintFlags(linkGrup.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                                linkGrup.setOnClickListener(v -> bukaLinkWhatsapp(link));
                            } else {
                                linkGrup.setText("-");
                            }

                            isiPeraturan.setText(peraturan != null ? peraturan : "-");

                        } else {
                            Toast.makeText(getContext(), "Data kost tidak ditemukan", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Gagal mengambil data kost", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(getContext(), "ID Kost belum dipilih", Toast.LENGTH_SHORT).show();
        }

        return view;
    }


    private void ambilDataKost(String namaKostTerpilih, FirebaseUser user) {
        db.collection("kost")
                .whereEqualTo("nama", namaKostTerpilih)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);

                        String documentId = document.getId(); // ← ID dokumen kost
                        // Simpan ke SharedPreferences
                        requireContext().getSharedPreferences("kostPrefs", getContext().MODE_PRIVATE)
                                .edit()
                                .putString("selectedKostId", documentId)
                                .apply();

                        String alamatKost = document.getString("alamat");
                        String link = document.getString("link_grup");
                        String peraturan = document.getString("peraturan");

                        namaKost.setText(namaKostTerpilih);
                        alamat.setText(alamatKost != null ? alamatKost : "-");
                        pemilik.setText("Pemilik : " + (user.getDisplayName() != null ? user.getDisplayName() : "-"));
                        whatsapp.setText("No. Whatsapp : " + (user.getPhoneNumber() != null ? user.getPhoneNumber() : "-"));

                        if (link != null && !link.isEmpty()) {
                            linkGrup.setText(link);
                            linkGrup.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_blue_dark));
                            linkGrup.setPaintFlags(linkGrup.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                            linkGrup.setOnClickListener(v -> bukaLinkWhatsapp(link));
                        } else {
                            linkGrup.setText("-");
                        }

                        isiPeraturan.setText(peraturan != null ? peraturan : "-");

                    } else {
                        Toast.makeText(getContext(), "Data kost tidak ditemukan", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Gagal mengambil data kost", Toast.LENGTH_SHORT).show();
                });
    }


    private void backToPemilikFragment() {
        Pemilik pemilikFragment = new Pemilik();
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.frameLayout, pemilikFragment)
                .addToBackStack(null)
                .commit();
    }

    private void bukaLinkWhatsapp(String link) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(link));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Gagal membuka link", Toast.LENGTH_SHORT).show();
        }
    }
}
