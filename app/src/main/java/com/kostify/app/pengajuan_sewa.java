package com.kostify.app;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class pengajuan_sewa extends Fragment {

    private Spinner dropdownDurasi;
    private EditText txtAlasan;
    private Button btnAjukan;
    private TextView namaKost, kamar, tenggat, hari;
    private ProgressBar progressBar;
    private FirebaseFirestore db;

    public pengajuan_sewa() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pengajuan_sewa, container, false);

        db = FirebaseFirestore.getInstance();

        dropdownDurasi = view.findViewById(R.id.dropdownperpanjangan);
        txtAlasan = view.findViewById(R.id.txtalasanperpanjangan);
        btnAjukan = view.findViewById(R.id.btnajukanperpanjangansewa);
        namaKost = view.findViewById(R.id.namakost);
        kamar = view.findViewById(R.id.kamar);
        tenggat = view.findViewById(R.id.tenggat);
        hari = view.findViewById(R.id.hari);
        progressBar = view.findViewById(R.id.progressewa);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.list_durasi_perpanjangan,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdownDurasi.setAdapter(adapter);

        ambilDataSewaSaatIni();

        btnAjukan.setOnClickListener(v -> ajukanPerpanjangan());

        return view;
    }

    private void ambilDataSewaSaatIni() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        db.collection("kost").get().addOnSuccessListener(kostSnapshot -> {
            for (DocumentSnapshot kostDoc : kostSnapshot.getDocuments()) {
                String idKost = kostDoc.getId();

                db.collection("kost").document(idKost)
                        .collection("penyewa")
                        .whereEqualTo("id_user", userId)
                        .get()
                        .addOnSuccessListener(penyewaSnapshot -> {
                            if (!penyewaSnapshot.isEmpty()) {
                                DocumentSnapshot penyewaDoc = penyewaSnapshot.getDocuments().get(0);

                                String nama = kostDoc.getString("nama_kost");
                                String namaKamar = penyewaDoc.getString("kamar");

                                namaKost.setText(nama != null ? nama : "Kost -");
                                kamar.setText(namaKamar != null ? namaKamar : "Kamar -");

                                Timestamp tenggatTime = penyewaDoc.getTimestamp("tenggat");

                                if (tenggatTime != null) {
                                    Date tanggalTenggat = tenggatTime.toDate();
                                    String formatted = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID")).format(tanggalTenggat);
                                    tenggat.setText("Hingga " + formatted);

                                    int sisaHari = hitungHariSisa(tanggalTenggat);
                                    hari.setText(sisaHari + " Hari");
                                    progressBar.setMax(sisaHari);
                                    progressBar.setProgress(sisaHari);
                                } else {
                                    tenggat.setText("Hingga -");
                                    hari.setText("- Hari");
                                }
                            }
                        });
            }
        });
    }

    private void ajukanPerpanjangan() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            Toast.makeText(getContext(), "User belum login", Toast.LENGTH_SHORT).show();
            return;
        }

        String durasi = dropdownDurasi.getSelectedItem().toString();
        String alasan = txtAlasan.getText().toString().trim();

        if (TextUtils.isEmpty(durasi)) {
            Toast.makeText(getContext(), "Pilih durasi perpanjangan", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("kost").get().addOnSuccessListener(kostSnapshot -> {
            for (DocumentSnapshot kostDoc : kostSnapshot.getDocuments()) {
                String idKost = kostDoc.getId();

                db.collection("kost").document(idKost)
                        .collection("penyewa")
                        .whereEqualTo("id_user", userId)
                        .get()
                        .addOnSuccessListener(penyewaSnapshot -> {
                            if (!penyewaSnapshot.isEmpty()) {
                                Map<String, Object> data = new HashMap<>();
                                data.put("id_user", userId);
                                data.put("durasi", durasi);
                                data.put("alasan", alasan.isEmpty() ? "-" : alasan);
                                data.put("waktu_pengajuan", Timestamp.now());
                                data.put("status", "diajukan");

                                db.collection("kost").document(idKost)
                                        .collection("pengajuan_perpanjangan")
                                        .add(data)
                                        .addOnSuccessListener(docRef -> {
                                            Toast.makeText(getContext(), "Pengajuan berhasil dikirim", Toast.LENGTH_SHORT).show();
                                            txtAlasan.setText("");
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(getContext(), "Gagal menyimpan pengajuan", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        });
            }
        });
    }

    private int hitungHariSisa(Date tenggat) {
        long millisSisa = tenggat.getTime() - System.currentTimeMillis();
        return Math.max(0, (int) (millisSisa / (1000 * 60 * 60 * 24)));
    }
}
