package com.kostify.app;

import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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

public class informasikostpenyewa extends Fragment {

    private TextView namaKost, alamat, pemilik, whatsapp, linkGrup, isiPeraturan;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public informasikostpenyewa() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_informasi_kost_penyewa, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Inisialisasi UI
        namaKost = view.findViewById(R.id.namakost);
        alamat = view.findViewById(R.id.alamat);
        pemilik = view.findViewById(R.id.textpemilik);
        whatsapp = view.findViewById(R.id.textwhatsapp);
        linkGrup = view.findViewById(R.id.linkgrupwhatsapp);
        isiPeraturan = view.findViewById(R.id.textisiperaturan);
        ImageView btnBack = view.findViewById(R.id.ic_back);

        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        // Cek posisi user
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            cariKostYangDitempati(userId);
        } else {
            Toast.makeText(getContext(), "User belum login", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void cariKostYangDitempati(String userId) {
        db.collection("kost").get().addOnSuccessListener(kostSnapshot -> {
            for (DocumentSnapshot kostDoc : kostSnapshot.getDocuments()) {
                String idKost = kostDoc.getId();

                db.collection("kost")
                        .document(idKost)
                        .collection("penyewa")
                        .whereEqualTo("id_user", userId)
                        .get()
                        .addOnSuccessListener(penyewaSnapshot -> {
                            if (!penyewaSnapshot.isEmpty()) {
                                // Jika ditemukan, ambil data kost
                                tampilkanInformasiKost(kostDoc);
                            }
                        });
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Gagal mencari kost", Toast.LENGTH_SHORT).show();
            Log.e("InfoKostPenyewa", "Error cari kost", e);
        });
    }

    private void tampilkanInformasiKost(DocumentSnapshot kostDoc) {
        String nama = kostDoc.getString("nama_kost");
        String alamatKost = kostDoc.getString("alamat");
        String link = kostDoc.getString("link_grup");
        String peraturan = kostDoc.getString("peraturan");

        namaKost.setText(nama != null ? nama : "-");
        alamat.setText(alamatKost != null ? alamatKost : "-");
        isiPeraturan.setText(peraturan != null ? peraturan : "-");

        String userIdPemilik = kostDoc.getString("userId"); // userId disimpan di dokumen kost
        Log.d("InfoKostPenyewa", "User ID pemilik dari kost: " + userIdPemilik);

        if (userIdPemilik != null && !userIdPemilik.isEmpty()) {
            db.collection("users").document(userIdPemilik).get()
                    .addOnSuccessListener(userDoc -> {
                        if (userDoc.exists()) {
                            Log.d("InfoKostPenyewa", "Dokumen user ditemukan untuk ID: " + userIdPemilik);
                            Log.d("InfoKostPenyewa", "Isi dokumen user: " + userDoc.getData());

                            String namaPemilik = userDoc.getString("nama");
                            String nomor = userDoc.getString("telepon");

                            pemilik.setText("Pemilik : " + (namaPemilik != null ? namaPemilik : "-"));
                            whatsapp.setText("No. Whatsapp : " + (nomor != null ? nomor : "-"));

                            Log.d("InfoKostPenyewa", "Nama pemilik: " + namaPemilik);
                            Log.d("InfoKostPenyewa", "Nomor telepon: " + nomor);
                        } else {
                            Log.e("InfoKostPenyewa", "Dokumen user tidak ditemukan untuk ID: " + userIdPemilik);
                            pemilik.setText("Pemilik : -");
                            whatsapp.setText("No. Whatsapp : -");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("InfoKostPenyewa", "Gagal mengambil dokumen user", e);
                        pemilik.setText("Pemilik : -");
                        whatsapp.setText("No. Whatsapp : -");
                    });
        } else {
            Log.e("InfoKostPenyewa", "User ID pemilik kosong atau null");
            pemilik.setText("Pemilik : -");
            whatsapp.setText("No. Whatsapp : -");
        }

        if (link != null && !link.isEmpty()) {
            linkGrup.setText(link);
            linkGrup.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_blue_dark));
            linkGrup.setPaintFlags(linkGrup.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            linkGrup.setOnClickListener(v -> bukaLinkWhatsapp(link));
        } else {
            linkGrup.setText("-");
        }
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
