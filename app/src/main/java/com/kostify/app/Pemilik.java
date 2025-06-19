package com.kostify.app;

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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

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

    private TextView namakostTextView, alamatTextView, angkajumlahkamarTextView;
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

        // Inisialisasi Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Inisialisasi komponen UI
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

        // Tombol hapus kost
        view.findViewById(R.id.btnhapuskost).setOnClickListener(v -> hapusKost());

        // Navigasi ke fragment atau activity lain
        view.findViewById(R.id.mntambahkost).setOnClickListener(v -> opentambahkost());
        view.findViewById(R.id.mninfokost).setOnClickListener(v -> openinfoKost());
        view.findViewById(R.id.mnpengumuman).setOnClickListener(v -> openpengumuman());
        view.findViewById(R.id.mnpenyewa).setOnClickListener(v -> openlistpenyewa());
        view.findViewById(R.id.mneditkost).setOnClickListener(v -> openeditkost());
        view.findViewById(R.id.mnnotifikasi).setOnClickListener(v -> opennotifikasi());

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

                        String alamat = documentSnapshot.getString("alamat");
                        Long totalKamar = documentSnapshot.getLong("jumlah_kamar");

                        namakostTextView.setText(kostName);
                        setTextOrDefault(alamat, alamatTextView, "Alamat tidak tersedia");
                        angkajumlahkamarTextView.setText(totalKamar != null ? String.valueOf(totalKamar) : "0");
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
            Log.d("Pemilik", "Tidak ada kost yang dipilih.");
            return;
        }

        db.collection("kost").document(selectedKostId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("Pemilik", "Kost berhasil dihapus.");
                    fetchKostNames();
                    gantikost.setSelection(0);
                    setEmptyState();
                })
                .addOnFailureListener(e -> Log.d("Pemilik", "Gagal menghapus kost: " + e.getMessage()));
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



    private void openlistpenyewa() {
        Intent intent = new Intent(requireContext(), nav_list_penyewa.class);
        startActivity(intent);
    }






    // Navigasi ke fragment notifikasi
    private void opennotifikasi() {
        navigateToFragment(new Notifikasi());
    }

    // Fungsi bantu untuk navigasi fragment
    private void navigateToFragment(Fragment fragment) {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frameLayout, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
