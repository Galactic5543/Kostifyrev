package com.kostify.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class detail_pengajuan extends Fragment {

    private TextView tvTitle, tvNama, tvWaktu, tvDurasi, tvAlasan, tvJenisKerusakan, tvDeskripsi;
    private Button btnKonfirmasi;
    private String userId, collection, aksi, kostId;

    public detail_pengajuan() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail_pengajuan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvTitle = view.findViewById(R.id.detailTitle);
        tvNama = view.findViewById(R.id.detailNamaPenyewa);
        tvWaktu = view.findViewById(R.id.detailWaktu);
        tvDurasi = view.findViewById(R.id.detailDurasiPerpanjangan);
        tvAlasan = view.findViewById(R.id.detailAlasan);
        tvJenisKerusakan = view.findViewById(R.id.detailJenisKerusakan);
        tvDeskripsi = view.findViewById(R.id.detailDeskripsi);
        btnKonfirmasi = view.findViewById(R.id.btnkonfirmasi);

        Bundle args = getArguments();
        if (args != null) {
            userId = args.getString("userId");
            collection = args.getString("collection");
            aksi = args.getString("aksi");
        }

        SharedPreferences prefs = requireContext().getSharedPreferences("kostPrefs", Context.MODE_PRIVATE);
        kostId = prefs.getString("selectedKostId", null);

        if (userId == null || collection == null || aksi == null || kostId == null) {
            Toast.makeText(getContext(), "Data tidak lengkap", Toast.LENGTH_SHORT).show();
            return;
        }

        ambilNamaUser();

        btnKonfirmasi.setOnClickListener(v -> konfirmasiPengajuan());
    }

    private void ambilNamaUser() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(snapshot -> {
                    String nama = snapshot.exists() ? snapshot.getString("nama") : "Tidak diketahui";
                    ambilDataPengajuan(nama);
                })
                .addOnFailureListener(e -> ambilDataPengajuan("Tidak diketahui"));
    }

    private void ambilDataPengajuan(String nama) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("kost").document(kostId).collection(collection)
                .whereEqualTo("id_user", userId)
                .limit(1)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        DocumentSnapshot doc = query.getDocuments().get(0);

                        String waktu = doc.contains("waktu_pengajuan")
                                ? formatWaktu(doc.getDate("waktu_pengajuan")) : "-";

                        tvTitle.setText(capitalize(aksi));
                        tvNama.setText("Nama: " + nama);
                        tvWaktu.setText("Diajukan pada: " + waktu);

                        String status = doc.contains("status") ? doc.getString("status") : "";
                        btnKonfirmasi.setVisibility("terkonfirmasi".equalsIgnoreCase(status) ? View.GONE : View.VISIBLE);

                        switch (collection) {
                            case "pengajuan_perpanjangan":
                            case "pending":
                                tampilkanPerpanjangan(doc);
                                break;
                            case "pengajuan_kerusakan":
                                tampilkanKerusakan(doc);
                                break;
                        }

                    } else {
                        Toast.makeText(getContext(), "Data tidak ditemukan", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Gagal mengambil data pengajuan", Toast.LENGTH_SHORT).show());
    }

    private void tampilkanPerpanjangan(DocumentSnapshot doc) {
        tvDurasi.setVisibility(View.VISIBLE);
        tvAlasan.setVisibility(View.VISIBLE);
        tvJenisKerusakan.setVisibility(View.GONE);
        tvDeskripsi.setVisibility(View.GONE);
        tvDurasi.setText("Durasi: " + doc.getString("durasi"));
        tvAlasan.setText("Alasan: " + doc.getString("alasan"));
    }

    private void tampilkanKerusakan(DocumentSnapshot doc) {
        tvDurasi.setVisibility(View.GONE);
        tvAlasan.setVisibility(View.GONE);
        tvJenisKerusakan.setVisibility(View.VISIBLE);
        tvDeskripsi.setVisibility(View.VISIBLE);
        tvJenisKerusakan.setText("Jenis Kerusakan: " + doc.getString("jenis_kerusakan"));
        tvDeskripsi.setText("Deskripsi: " + doc.getString("deskripsi"));
    }

    private void konfirmasiPengajuan() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("kost").document(kostId).collection(collection)
                .whereEqualTo("id_user", userId)
                .limit(1)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        DocumentSnapshot pengajuanDoc = query.getDocuments().get(0);
                        String docId = pengajuanDoc.getId();

                        // Update status pengajuan
                        db.collection("kost").document(kostId)
                                .collection(collection).document(docId)
                                .update("status", "terkonfirmasi")
                                .addOnSuccessListener(aVoid -> {
                                    if (collection.equals("pengajuan_perpanjangan")) {
                                        // Lanjut update penyewa
                                        String durasiBaruStr = pengajuanDoc.getString("durasi");
                                        int durasiBaru = durasiToBulan(durasiBaruStr);
                                        Timestamp sekarang = Timestamp.now();

                                        // Cari dokumen penyewa yang cocok
                                        db.collection("kost").document(kostId)
                                                .collection("penyewa")
                                                .whereEqualTo("id_user", userId)
                                                .limit(1)
                                                .get()
                                                .addOnSuccessListener(penyewaQuery -> {
                                                    if (!penyewaQuery.isEmpty()) {
                                                        DocumentSnapshot penyewaDoc = penyewaQuery.getDocuments().get(0);
                                                        String penyewaDocId = penyewaDoc.getId();
                                                        Timestamp tenggatLama = penyewaDoc.getTimestamp("tenggat");

                                                        Calendar cal = Calendar.getInstance();
                                                        if (tenggatLama != null) {
                                                            cal.setTime(tenggatLama.toDate());
                                                        } else {
                                                            cal.setTime(new Date());
                                                        }
                                                        cal.add(Calendar.MONTH, durasiBaru);
                                                        Timestamp tenggatBaru = new Timestamp(cal.getTime());

                                                        db.collection("kost").document(kostId)
                                                                .collection("penyewa")
                                                                .document(penyewaDocId)
                                                                .update(
                                                                        "durasi", durasiBaruStr,
                                                                        "tenggat", tenggatBaru,
                                                                        "perpanjang_terakhir", sekarang
                                                                )
                                                                .addOnSuccessListener(unused -> {
                                                                    Toast.makeText(getContext(), "Perpanjangan dikonfirmasi dan diperbarui", Toast.LENGTH_SHORT).show();
                                                                    requireActivity().getSupportFragmentManager().popBackStack();
                                                                })
                                                                .addOnFailureListener(e -> Toast.makeText(getContext(), "Gagal update penyewa", Toast.LENGTH_SHORT).show());
                                                    } else {
                                                        Toast.makeText(getContext(), "Data penyewa tidak ditemukan", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    } else {
                                        Toast.makeText(getContext(), "Pengajuan dikonfirmasi", Toast.LENGTH_SHORT).show();
                                        requireActivity().getSupportFragmentManager().popBackStack();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Gagal mengambil data pengajuan", Toast.LENGTH_SHORT).show());
    }


    private int durasiToBulan(String durasi) {
        try {
            return Integer.parseInt(durasi.split(" ")[0]);
        } catch (Exception e) {
            return 0;
        }
    }

    private String formatWaktu(@Nullable java.util.Date date) {
        if (date == null) return "-";
        return new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(date);
    }

    private String capitalize(String text) {
        if (text == null || text.isEmpty()) return "";
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }
}
