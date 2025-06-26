package com.kostify.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Konfirmasi_pending extends Fragment {

    private TextView textPenyewa, textNoWhatsapp;
    private FirebaseFirestore db;
    private String idUser, namaPenyewa;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_konfirmasi_pending, container, false);

        Spinner spinnerKamar = view.findViewById(R.id.txtkamar);
        Spinner spinnerDurasi = view.findViewById(R.id.txtdurasisewa);
        EditText hargaSewa = view.findViewById(R.id.txtharga);
        Button btnKonfirmasi = view.findViewById(R.id.btnkonfirmasi);

        textPenyewa = view.findViewById(R.id.textpenyewa);
        textNoWhatsapp = view.findViewById(R.id.textnowhatsapp);

        db = FirebaseFirestore.getInstance();

        SharedPreferences prefs = requireContext().getSharedPreferences("kostPrefs", Context.MODE_PRIVATE);
        String idKost = prefs.getString("selectedKostId", null);

        Bundle bundle = getArguments();
        if (bundle != null) {
            namaPenyewa = bundle.getString("nama");
            idUser = bundle.getString("id_user");

            if (namaPenyewa != null) {
                textPenyewa.setText("Calon Penyewa : " + namaPenyewa);
            }

            if (idUser != null) {
                ambilDataUser(idUser);
            }
        }

        ArrayAdapter<CharSequence> adapterDurasi = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.list_durasi_perpanjangan,
                android.R.layout.simple_spinner_item
        );
        adapterDurasi.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDurasi.setAdapter(adapterDurasi);

        loadKamarKosongKeSpinner(spinnerKamar);

        btnKonfirmasi.setOnClickListener(v -> {
            if (idKost == null || idUser == null) {
                Toast.makeText(requireContext(), "ID Kost atau ID User tidak ditemukan", Toast.LENGTH_SHORT).show();
                return;
            }

            String kamarDipilih = spinnerKamar.getSelectedItem().toString();
            String durasiStr = spinnerDurasi.getSelectedItem().toString();
            String harga = hargaSewa.getText().toString().trim();

            if (harga.isEmpty()) {
                Toast.makeText(requireContext(), "Harga tidak boleh kosong", Toast.LENGTH_SHORT).show();
                return;
            }

            // Hitung tanggal tenggat
            int jumlahBulan = getJumlahBulanFromDurasi(durasiStr);
            Calendar calendar = Calendar.getInstance();
            Timestamp tanggalKonfirmasi = Timestamp.now();
            calendar.setTime(tanggalKonfirmasi.toDate());
            calendar.add(Calendar.MONTH, jumlahBulan);
            Timestamp tenggat = new Timestamp(calendar.getTime());

            Map<String, Object> dataPenyewaan = new HashMap<>();
            dataPenyewaan.put("id_user", idUser);
            dataPenyewaan.put("nama_penyewa", namaPenyewa);
            dataPenyewaan.put("kamar", kamarDipilih);
            dataPenyewaan.put("durasi", durasiStr);
            dataPenyewaan.put("pembayaran sewa", harga);
            dataPenyewaan.put("tanggal_masuk", tanggalKonfirmasi);
            dataPenyewaan.put("tenggat", tenggat);
            dataPenyewaan.put("perpanjang_terakhir", tanggalKonfirmasi); // disesuaikan

            db.collection("kost")
                    .document(idKost)
                    .collection("penyewa")
                    .add(dataPenyewaan)
                    .addOnSuccessListener(documentReference -> {

                        // Update status kamar
                        db.collection("kost")
                                .document(idKost)
                                .collection("kamar")
                                .whereEqualTo("nama_kamar", kamarDipilih)
                                .get()
                                .addOnSuccessListener(querySnapshot -> {
                                    if (!querySnapshot.isEmpty()) {
                                        DocumentSnapshot kamarDoc = querySnapshot.getDocuments().get(0);
                                        String idKamarDoc = kamarDoc.getId();

                                        Map<String, Object> updateKamar = new HashMap<>();
                                        updateKamar.put("status", "Terisi");
                                        updateKamar.put("penyewa", namaPenyewa);
                                        updateKamar.put("id_penyewa", idUser);

                                        db.collection("kost")
                                                .document(idKost)
                                                .collection("kamar")
                                                .document(idKamarDoc)
                                                .update(updateKamar)
                                                .addOnSuccessListener(unused -> {

                                                    // Hapus data pending
                                                    db.collection("kost")
                                                            .document(idKost)
                                                            .collection("pending")
                                                            .whereEqualTo("id_user", idUser)
                                                            .get()
                                                            .addOnSuccessListener(query -> {
                                                                for (DocumentSnapshot doc : query.getDocuments()) {
                                                                    db.collection("kost")
                                                                            .document(idKost)
                                                                            .collection("pending")
                                                                            .document(doc.getId())
                                                                            .delete();
                                                                }

                                                                Toast.makeText(requireContext(),
                                                                        "Konfirmasi berhasil dan data pending dihapus",
                                                                        Toast.LENGTH_LONG).show();
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Log.e("DELETE_PENDING", "Gagal hapus data pending: " + e.getMessage());
                                                            });
                                                });
                                    }
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Gagal menyimpan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        return view;
    }

    private int getJumlahBulanFromDurasi(String durasi) {
        switch (durasi) {
            case "1 Bulan":
                return 1;
            case "3 Bulan":
                return 3;
            case "6 Bulan":
                return 6;
            case "12 Bulan":
                return 12;
            default:
                return 1;
        }
    }

    private String formatTanggal(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));
        return sdf.format(millis);
    }

    private void loadKamarKosongKeSpinner(Spinner spinnerKamar) {
        SharedPreferences prefs = requireContext().getSharedPreferences("kostPrefs", Context.MODE_PRIVATE);
        String kostId = prefs.getString("selectedKostId", null);

        if (kostId != null) {
            db.collection("kost")
                    .document(kostId)
                    .collection("kamar")
                    .whereEqualTo("status", "Kosong")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<String> daftarKamar = new ArrayList<>();
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            String namaKamar = document.getString("nama_kamar");
                            if (namaKamar != null) {
                                daftarKamar.add(namaKamar);
                            }
                        }

                        if (daftarKamar.isEmpty()) {
                            daftarKamar.add("Tidak ada kamar kosong");
                            spinnerKamar.setEnabled(false);
                        } else {
                            spinnerKamar.setEnabled(true);
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                requireContext(),
                                android.R.layout.simple_spinner_item,
                                daftarKamar
                        );
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerKamar.setAdapter(adapter);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("SPINNER_KAMAR", "Gagal ambil kamar: " + e.getMessage());
                    });
        }
    }

    private void ambilDataUser(String idUser) {
        db.collection("users")
                .document(idUser)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String noWa = documentSnapshot.getString("telepon");
                        if (noWa != null && !noWa.isEmpty()) {
                            textNoWhatsapp.setText("No.Whatsapp : " + noWa);
                        } else {
                            textNoWhatsapp.setText("Nomor WhatsApp tidak tersedia");
                        }
                    } else {
                        textNoWhatsapp.setText("Data pengguna tidak ditemukan");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("KONFIRMASI", "Gagal ambil data user: " + e.getMessage());
                    textNoWhatsapp.setText("Gagal ambil data pengguna");
                });
    }
}
