package com.kostify.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class info_sewa extends Fragment {

    private TextView textNamaKost, textKamar, textTenggat, textHarga, textPenyewa, textNoWa;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info_sewa, container, false);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        textNamaKost = view.findViewById(R.id.namakost);
        textKamar = view.findViewById(R.id.kamar);
        textTenggat = view.findViewById(R.id.texttenggat);
        textHarga = view.findViewById(R.id.texthargasewa);
        textPenyewa = view.findViewById(R.id.textpenyewa);
        textNoWa = view.findViewById(R.id.textnowhatsapp);

        view.findViewById(R.id.ic_back).setOnClickListener(v -> backToPenyewaFragment());

        ambilDataSewaJikaTerdaftar();

        return view;
    }

    private void ambilDataSewaJikaTerdaftar() {
        if (auth.getCurrentUser() == null) {
            isiKosongSemua();
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        db.collection("kost").get().addOnSuccessListener(kostSnapshot -> {
            boolean[] ditemukan = {false};

            for (DocumentSnapshot kostDoc : kostSnapshot) {
                String idKost = kostDoc.getId();
                String namaKost = kostDoc.getString("nama_kost");

                db.collection("kost").document(idKost)
                        .collection("penyewa")
                        .whereEqualTo("id_user", uid)
                        .get()
                        .addOnSuccessListener(penyewaSnapshot -> {
                            if (!penyewaSnapshot.isEmpty() && !ditemukan[0]) {
                                ditemukan[0] = true;

                                DocumentSnapshot penyewaDoc = penyewaSnapshot.getDocuments().get(0);
                                String kamar = penyewaDoc.getString("kamar");
                                String harga = penyewaDoc.getString("pembayaran sewa");
                                String namaPenyewa = penyewaDoc.getString("nama_penyewa");
                                Timestamp tenggat = penyewaDoc.getTimestamp("tenggat");

                                textNamaKost.setText(namaKost != null ? namaKost : "-");
                                textKamar.setText(kamar != null ? kamar : "-");

                                if (tenggat != null) {
                                    String formattedTenggat = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"))
                                            .format(tenggat.toDate());
                                    textTenggat.setText("Hingga " + formattedTenggat);
                                } else {
                                    textTenggat.setText("Hingga -");
                                }

                                textHarga.setText("Harga Sewa / Bulan : Rp. " + (harga != null ? harga : "-"));
                                textPenyewa.setText("Penyewa : " + (namaPenyewa != null ? namaPenyewa : "-"));

                                db.collection("users").document(uid).get().addOnSuccessListener(userDoc -> {
                                    String wa = userDoc.getString("telepon");
                                    textNoWa.setText("No.Whatsapp : " + (wa != null ? wa : "-"));
                                });

                            } else if (!ditemukan[0]) {
                                isiKosongSemua();
                            }
                        });
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Gagal mengambil data kost", Toast.LENGTH_SHORT).show();
            isiKosongSemua();
        });
    }

    private void isiKosongSemua() {
        textNamaKost.setText("Nama Kost");
        textKamar.setText("Kamar -");
        textTenggat.setText("Hingga -");
        textHarga.setText("Harga Sewa / Bulan : -");
        textPenyewa.setText("Penyewa : -");
        textNoWa.setText("No.Whatsapp : -");
    }

    private void backToPenyewaFragment() {
        Penyewa penyewaFragment = new Penyewa();
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.frameLayout, penyewaFragment)
                .addToBackStack(null)
                .commit();
    }
}
