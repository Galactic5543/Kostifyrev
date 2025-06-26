package com.kostify.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Penyewa extends Fragment {

    private static final String TAG = "PenyewaFragment";

    private AdView mAdView;
    private TextView textNamaKost, textAlamat, textKamar, textTenggat;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public Penyewa() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_penyewa, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        textNamaKost = view.findViewById(R.id.namakost);
        textAlamat = view.findViewById(R.id.alamat);
        textKamar = view.findViewById(R.id.kamar);
        textTenggat = view.findViewById(R.id.tenggat);

        // Load data kost
        loadDataKost();

        // Load iklan
        mAdView = view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // Navigasi menu
        view.findViewById(R.id.mn_info_kost).setOnClickListener(v -> openInformasiKostFragment());
        view.findViewById(R.id.mn_sosial).setOnClickListener(v -> openSosialFragment());
        view.findViewById(R.id.mn_pengajuan).setOnClickListener(v -> openPengajuanFragment());
        view.findViewById(R.id.mn_infosewa).setOnClickListener(v -> openInfoSewaFragment());
        view.findViewById(R.id.mnnotifikasi).setOnClickListener(v -> openNotifikasiFragment());
        view.findViewById(R.id.mngabung).setOnClickListener(v -> openJoinKostFragment());

        return view;
    }

    private void loadDataKost() {
        if (mAuth.getCurrentUser() == null) {
            setEmptyState();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        db.collection("kost")
                .get()
                .addOnSuccessListener(kostSnapshot -> {
                    if (kostSnapshot.isEmpty()) {
                        setEmptyState();
                        return;
                    }

                    boolean[] isFound = {false};

                    for (DocumentSnapshot kostDoc : kostSnapshot.getDocuments()) {
                        if (isFound[0]) break;

                        String idKost = kostDoc.getId();

                        db.collection("kost").document(idKost)
                                .collection("penyewa")
                                .whereEqualTo("id_user", userId)
                                .get()
                                .addOnSuccessListener(penyewaSnapshot -> {
                                    if (!penyewaSnapshot.isEmpty() && !isFound[0]) {
                                        isFound[0] = true;

                                        DocumentSnapshot penyewaDoc = penyewaSnapshot.getDocuments().get(0);

                                        String namaKost = kostDoc.getString("nama_kost");
                                        String alamat = kostDoc.getString("alamat");

                                        textNamaKost.setText(namaKost != null ? namaKost : "-");
                                        textAlamat.setText(alamat != null ? alamat : "-");

                                        Timestamp tenggat = penyewaDoc.getTimestamp("tenggat");

                                        if (tenggat != null) {
                                            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));
                                            textTenggat.setText(sdf.format(tenggat.toDate()));
                                        } else {
                                            textTenggat.setText("-");
                                        }

                                        db.collection("kost").document(idKost)
                                                .collection("kamar")
                                                .whereEqualTo("id_penyewa", userId)
                                                .get()
                                                .addOnSuccessListener(kamarSnapshot -> {
                                                    if (!kamarSnapshot.isEmpty()) {
                                                        String namaKamar = kamarSnapshot.getDocuments().get(0).getString("nama_kamar");
                                                        textKamar.setText(namaKamar != null ? namaKamar : "-");
                                                    } else {
                                                        textKamar.setText("-");
                                                    }
                                                });
                                    }
                                })
                                .addOnFailureListener(e -> Log.e(TAG, "Gagal cek penyewa: " + e.getMessage()));
                    }

                    new android.os.Handler().postDelayed(() -> {
                        if (!isFound[0]) {
                            setEmptyState();
                        }
                    }, 1500);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Gagal mengambil data", Toast.LENGTH_SHORT).show();
                    setEmptyState();
                });
    }

    private String hitungMasaSewa(Date tanggalAwal, String durasi) {
        if (tanggalAwal == null || durasi == null) return "-";
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(tanggalAwal);
            int jumlahBulan = durasi.toLowerCase().contains("bulan") ? Integer.parseInt(durasi.split(" ")[0]) : 0;
            cal.add(Calendar.MONTH, jumlahBulan);
            return new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(cal.getTime());
        } catch (Exception e) {
            return "-";
        }
    }

    private void setEmptyState() {
        textNamaKost.setText("Kosong");
        textAlamat.setText("-");
        textKamar.setText("-");
        textTenggat.setText("-");
    }

    // Navigasi Fragment
    private void openInformasiKostFragment() {
        navigateToFragment(new informasikostpenyewa());
    }

    private void openSosialFragment() {
        navigateToFragment(new sosial());
    }

    private void openPengajuanFragment() {
        startActivity(new Intent(getActivity(), navigasi_pengajuan.class));
    }

    private void openInfoSewaFragment() {
        navigateToFragment(new info_sewa());
    }

    private void openNotifikasiFragment() {
        navigateToFragment(new notifikasi_penyewa());
    }

    private void openJoinKostFragment() {
        navigateToFragment(new join_kost());
    }

    private void navigateToFragment(Fragment fragment) {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frameLayout, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
