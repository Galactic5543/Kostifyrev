    package com.kostify.app;

    import android.content.Intent;
    import android.os.Bundle;
    import android.util.Log; // Tambahkan ini
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

        private static final String TAG = "PenyewaFragment"; // TAG untuk logcat

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

            loadDataKost();

            mAdView = view.findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);

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
                            if (isFound[0]) break; // berhenti jika sudah ditemukan

                            String idKost = kostDoc.getId();

                            db.collection("kost").document(idKost)
                                    .collection("penyewa")
                                    .whereEqualTo("id_user", userId)
                                    .get()
                                    .addOnSuccessListener(penyewaSnapshot -> {
                                        if (!penyewaSnapshot.isEmpty() && !isFound[0]) {
                                            isFound[0] = true; // tandai sudah ketemu

                                            DocumentSnapshot penyewaDoc = penyewaSnapshot.getDocuments().get(0);

                                            String namaKost = kostDoc.getString("nama_kost");
                                            String alamat = kostDoc.getString("alamat");

                                            textNamaKost.setText(namaKost != null ? namaKost : "-");
                                            textAlamat.setText(alamat != null ? alamat : "-");

                                            String durasi = penyewaDoc.getString("durasi");
                                            Timestamp tanggalAwal = penyewaDoc.getTimestamp("perpanjang_terakhir");

                                            if (tanggalAwal != null && durasi != null) {
                                                textTenggat.setText(hitungMasaSewa(tanggalAwal.toDate(), durasi));
                                            } else {
                                                textTenggat.setText("-");
                                            }

                                            // Ambil kamar dari subkoleksi kamar yang cocok
                                            db.collection("kost").document(idKost)
                                                    .collection("kamar")
                                                    .whereEqualTo("id_penyewa", userId)
                                                    .get()
                                                    .addOnSuccessListener(kamarSnapshot -> {
                                                        if (!kamarSnapshot.isEmpty()) {
                                                            DocumentSnapshot kamarDoc = kamarSnapshot.getDocuments().get(0);
                                                            String namaKamar = kamarDoc.getString("nama_kamar");
                                                            textKamar.setText(namaKamar != null ? namaKamar : "-");
                                                        } else {
                                                            textKamar.setText("-");
                                                        }
                                                    });
                                        }
                                    })
                                    .addOnFailureListener(e -> Log.e("PenyewaFragment", "Gagal cek penyewa: " + e.getMessage()));
                        }

                        // Jika setelah semua dicek dan tidak ditemukan
                        new android.os.Handler().postDelayed(() -> {
                            if (!isFound[0]) {
                                setEmptyState();
                            }
                        }, 1500); // waktu tunggu untuk async selesai
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Gagal mengambil data", Toast.LENGTH_SHORT).show();
                        setEmptyState();
                    });
        }




        // Tambahkan metode ini
        private String hitungMasaSewa(Date tanggalAwal, String durasi) {
            if (tanggalAwal == null || durasi == null) return "-";

            try {
                Calendar cal = Calendar.getInstance();
                cal.setTime(tanggalAwal);

                int jumlahBulan = 0;
                if (durasi.toLowerCase().contains("bulan")) {
                    jumlahBulan = Integer.parseInt(durasi.split(" ")[0]);
                }

                cal.add(Calendar.MONTH, jumlahBulan);

                SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());
                return sdf.format(cal.getTime());
            } catch (Exception e) {
                e.printStackTrace();
                return "-";
            }
        }


        private void setEmptyState() {
            textNamaKost.setText("Kosong");
            textAlamat.setText("-");
            textKamar.setText("-");
            textTenggat.setText("-");
        }

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
            navigateToFragment(new Notifikasi());
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
