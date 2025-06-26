package com.kostify.app;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class Pemilik extends Fragment {

    private static final String DEFAULT_KOST_OPTION = "Pilih Kost";  // Opsi default spinner

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private AdView mAdView;

    private TextView namakostTextView, alamatTextView, angkajumlahkamarTextView,idkostTextView,angkamarkosongTextView,angakamarisiTextView;
    private Spinner gantikost;
    private ArrayList<String> kostNames; // List nama kost
    private String selectedKostId;       // Menyimpan ID kost yang dipilih

    public Pemilik() {
        // Konstruktor kosong wajib
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_pemilik, container, false);


        mAdView = view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // Inisialisasi Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Inisialisasi komponen UI
        angkamarkosongTextView = view.findViewById(R.id.angkakamarkosong);
        angakamarisiTextView = view.findViewById(R.id.angkakamarisi);

        idkostTextView = view.findViewById(R.id.idkost);
        namakostTextView = view.findViewById(R.id.namakost);
        alamatTextView = view.findViewById(R.id.alamat);
        angkajumlahkamarTextView = view.findViewById(R.id.angkajumlahkamar);
        gantikost = view.findViewById(R.id.gantikost);

        kostNames = new ArrayList<>();

        // Ambil nama kost dari database
        fetchKostNames();

        // Listener Spinner untuk memilih kost
        gantikost.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedKost = (String) parentView.getItemAtPosition(position);
                if (!selectedKost.equals(DEFAULT_KOST_OPTION)) {
                    updateKostData(selectedKost);

                    // SIMPAN ke SharedPreferences
                    saveSelectedKostIdToPrefs(selectedKost);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                setEmptyState();
            }
        });

        idkostTextView.setOnClickListener(v -> {
            String idText = selectedKostId; // atau: idkostTextView.getText().toString()
            ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("ID Kost", idText);
            clipboard.setPrimaryClip(clip);

            Toast.makeText(getContext(), "ID Kost disalin: " + idText, Toast.LENGTH_SHORT).show();
        });


        // Tombol hapus kost
        view.findViewById(R.id.btnhapuskost).setOnClickListener(v -> hapusKost());

        // Navigasi ke fragment atau activity lain
        view.findViewById(R.id.mntambahkost).setOnClickListener(v -> opentambahkost());
        view.findViewById(R.id.mninfokost).setOnClickListener(v -> openinfoKost());
        view.findViewById(R.id.mnpengumuman).setOnClickListener(v -> openpengumuman());
        view.findViewById(R.id.mnpenyewa).setOnClickListener(v -> openlistpenyewa());
        view.findViewById(R.id.mneditkost).setOnClickListener(v -> openeditkost());
        view.findViewById(R.id.mnnotifikasi).setOnClickListener(v -> opennotifikasi());
        view.findViewById(R.id.mnkamar).setOnClickListener(v -> openkamar());

        return view;
    }



    private void saveSelectedKostIdToPrefs(String namaKost) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference kostRef = db.collection("kost");

        kostRef.whereEqualTo("nama_kost", namaKost) // Ganti disini
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        String documentId = documentSnapshot.getId(); // ID kost

                        if (getContext() != null) {
                            SharedPreferences prefs = getContext().getSharedPreferences("kostPrefs", Context.MODE_PRIVATE);
                            prefs.edit().putString("selectedKostId", documentId).apply();
                        }


                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Gagal mengambil data kost", Toast.LENGTH_SHORT).show();
                });
    }



    // Mengambil daftar nama kost milik user dari Firestore
    private void fetchKostNames() {
        if (auth.getCurrentUser() == null) {
            Log.d("Pemilik", "User belum login.");
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        db.collection("kost")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    kostNames.clear();
                    kostNames.add(DEFAULT_KOST_OPTION);

                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            String namaKost = documentSnapshot.getString("nama_kost");
                            if (namaKost != null && !namaKost.isEmpty()) {
                                kostNames.add(namaKost);
                            }
                        }
                    }

                    if (kostNames.size() == 1) {
                        kostNames.add("Kosong");
                    }

                    if (isAdded()) {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, kostNames);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        gantikost.setAdapter(adapter);

                        if (kostNames.size() > 1) {
                            gantikost.setSelection(kostNames.size() - 1);
                        } else {
                            gantikost.setSelection(0);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d("Pemilik", "Gagal mengambil data: " + e.getMessage());
                    if (isAdded()) setEmptyState();
                });
    }



    private void fetchJumlahKamar(String kostId) {
        db.collection("kost").document(kostId).collection("kamar")
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    int jumlahKosong = 0;
                    int jumlahIsi = 0;

                    for (DocumentSnapshot kamar : querySnapshots) {
                        String status = kamar.getString("status");
                        if ("kosong".equalsIgnoreCase(status)) {
                            jumlahKosong++;
                        } else if ("terisi".equalsIgnoreCase(status)) {
                            jumlahIsi++;
                        }
                    }

                    // Set ke TextView (pastikan kamu punya TextView-nya di layout)
                    angkamarkosongTextView.setText(String.valueOf(jumlahIsi));
                    angakamarisiTextView.setText(String.valueOf(jumlahKosong));

                })
                .addOnFailureListener(e -> {
                    Log.e("FetchKamar", "Gagal mengambil data kamar: " + e.getMessage());
                    angkamarkosongTextView.setText("0");
                    angakamarisiTextView.setText("0");
                });
    }



    // Mengupdate data detail kost saat dipilih
    private void updateKostData(String kostName) {
        if (auth.getCurrentUser() == null) {
            Log.d("Pemilik", "User belum login.");
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        db.collection("kost")
                .whereEqualTo("userId", userId)
                .whereEqualTo("nama_kost", kostName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        QueryDocumentSnapshot documentSnapshot = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                        selectedKostId = documentSnapshot.getId();
                        fetchJumlahKamar(selectedKostId);
                        String alamat = documentSnapshot.getString("alamat");
                        Long totalKamar = documentSnapshot.getLong("jumlah_kamar");

                        namakostTextView.setText(kostName);
                        setTextOrDefault(alamat, alamatTextView, "Alamat tidak tersedia");
                        angkajumlahkamarTextView.setText(totalKamar != null ? String.valueOf(totalKamar) : "0");

                        // Tampilkan ID Kost
                        idkostTextView.setText("ID Kost: " + selectedKostId);

                        // Aktifkan copy saat klik
                        idkostTextView.setOnClickListener(v -> {
                            ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("ID Kost", selectedKostId);
                            clipboard.setPrimaryClip(clip);
                            Toast.makeText(getContext(), "ID Kost disalin: " + selectedKostId, Toast.LENGTH_SHORT).show();
                        });

                    } else {
                        Log.d("Pemilik", "Dokumen tidak ditemukan.");
                        setEmptyState();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d("Pemilik", "Gagal mengambil data: " + e.getMessage());
                    setEmptyState();
                });
    }


    // Menghapus kost yang dipilih dari Firestore
    private void hapusKost() {
        if (auth.getCurrentUser() == null || selectedKostId == null) {
            Toast.makeText(getContext(), "Kost belum dipilih", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String kostId = selectedKostId;

        // Daftar semua subkoleksi di dalam kost
        String[] subkoleksi = {
                "kamar",
                "penyewa",
                "pengajuan_perpanjangan",
                "pengajuan_kerusakan",
                "notifikasi"
        };


        // Hitung berapa koleksi yang selesai dihapus
        final int[] selesai = {0};

        for (String namaSubkoleksi : subkoleksi) {
            db.collection("kost").document(kostId).collection(namaSubkoleksi)
                    .get()
                    .addOnSuccessListener(query -> {
                        for (DocumentSnapshot doc : query.getDocuments()) {
                            doc.getReference().delete();
                        }

                        // Cek jika semua subkoleksi sudah selesai
                        selesai[0]++;
                        if (selesai[0] == subkoleksi.length) {
                            // Setelah semua subkoleksi dihapus → hapus dokumen kost
                            db.collection("kost").document(kostId)
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(getContext(), "Kost berhasil dihapus", Toast.LENGTH_SHORT).show();
                                        fetchKostNames(); // refresh spinner
                                        gantikost.setSelection(0);
                                        setEmptyState();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(getContext(), "Gagal menghapus kost", Toast.LENGTH_SHORT).show();
                                        Log.e("Pemilik", "Gagal hapus kost: " + e.getMessage());
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Pemilik", "Gagal hapus koleksi " + namaSubkoleksi + ": " + e.getMessage());
                        Toast.makeText(getContext(), "Gagal hapus " + namaSubkoleksi, Toast.LENGTH_SHORT).show();
                    });
        }
    }


    // Utility method untuk set teks atau default jika null/empty
    private void setTextOrDefault(String value, TextView textView, String defaultText) {
        if (value != null && !value.isEmpty()) {
            textView.setText(value);
        } else {
            textView.setText(defaultText);
        }
    }

    // Reset UI jika data kosong atau error
    private void setEmptyState() {
        namakostTextView.setText("Kosong");
        alamatTextView.setText("Alamat tidak tersedia");
        angkajumlahkamarTextView.setText("0");
    }

    // Navigasi ke fragment tambah kost
    private void opentambahkost() {
        navigateToFragment(new tambah_kost());
    }



    private void openeditkost() {
        edit_kost fragment = new edit_kost();

        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frameLayout, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }



    private void openinfoKost() {
        informasikostpemilik fragment = new informasikostpemilik();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frameLayout, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }



    private void openpengumuman() {
        navigateToFragment(new pengumuman());
    }

    private void openkamar() {
        navigateToFragment(new list_Kamar());
    }



    private void openlistpenyewa() {
        Intent intent = new Intent(requireContext(), nav_list_penyewa.class);
        startActivity(intent);
    }






    // Navigasi ke fragment notifikasi
    private void opennotifikasi() {
        navigateToFragment(new notifikasi_pemilik());
    }

    // Fungsi bantu untuk navigasi fragment
    private void navigateToFragment(Fragment fragment) {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frameLayout, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
