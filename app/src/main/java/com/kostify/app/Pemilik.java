package com.kostify.app;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class Pemilik extends Fragment {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private TextView namakostTextView, alamatTextView, totalkamarkosongTextView;
    private Spinner gantikost;  // Declare Spinner globally
    private ArrayList<String> kostNames; // ArrayList to hold kost names

    public Pemilik() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_pemilik, container, false);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Inisialisasi TextView
        namakostTextView = view.findViewById(R.id.namakost);
        alamatTextView = view.findViewById(R.id.alamat);
        totalkamarkosongTextView = view.findViewById(R.id.totalkamarkosong);

        // Inisialisasi Spinner
        gantikost = view.findViewById(R.id.gantikost);
        kostNames = new ArrayList<>();

        // Fetch data untuk nama kost, alamat dan total kamar kosong
        fetchKostNames();

        // Menambahkan listener untuk Spinner
        gantikost.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedKost = (String) parentView.getItemAtPosition(position);
                if (!selectedKost.equals("Pilih Kost")) {  // Avoid querying for "Pilih Kost"
                    updateKostData(selectedKost);  // Update data berdasarkan kost yang dipilih
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Jika tidak ada yang dipilih, kosongkan data
                setEmptyState();
            }
        });

        // Navigasi ke berbagai fragment
        view.findViewById(R.id.mntambahkost).setOnClickListener(v -> opentambahkost());
        view.findViewById(R.id.mninfokost).setOnClickListener(v -> openinfokost());
        view.findViewById(R.id.mnpenyewa).setOnClickListener(v -> openpenyewa());
        view.findViewById(R.id.mnpengumuman).setOnClickListener(v -> openpengumuman());
        view.findViewById(R.id.btndetailpenyewa).setOnClickListener(v -> openlistpenyewa());

        return view;
    }

    private void fetchKostNames() {
        String userId = auth.getCurrentUser().getUid();

        db.collection("kost")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        kostNames.clear();
                        kostNames.add("Pilih Kost");

                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            String namaKost = documentSnapshot.getString("nama");
                            if (namaKost != null && !namaKost.isEmpty()) {
                                kostNames.add(namaKost);
                            }
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, kostNames);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        gantikost.setAdapter(adapter);

                        // Pilih kost terbaru (yang terakhir ditambahkan)
                        if (kostNames.size() > 1) {
                            gantikost.setSelection(kostNames.size() - 1);
                        }

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


    private void updateKostData(String kostName) {
        String userId = auth.getCurrentUser().getUid(); // Mendapatkan userId dari Firebase Auth

        // Melakukan query untuk mencari kost berdasarkan nama dan userId
        db.collection("kost")
                .whereEqualTo("userId", userId)
                .whereEqualTo("nama", kostName) // Filter berdasarkan nama kost
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Cek jika query tidak kosong
                        QueryDocumentSnapshot documentSnapshot = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                        String alamat = documentSnapshot.getString("alamat");
                        Long totalKamarKosong = documentSnapshot.getLong("jumlah_kamar");

                        Log.d("Pemilik", "Nama Kost: " + kostName + ", Alamat: " + alamat + ", Kamar Kosong: " + totalKamarKosong);

                        // Set nama kost ke TextView
                        namakostTextView.setText(kostName);

                        // Set alamat ke TextView
                        setTextOrDefault(alamat, alamatTextView, "Alamat tidak tersedia");

                        // Set total kamar kosong ke TextView
                        setKamarKosong(totalKamarKosong);
                    } else {
                        // Menangani kasus jika dokumen tidak ditemukan
                        Log.d("Pemilik", "Dokumen tidak ditemukan.");
                        setEmptyState();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d("Pemilik", "Gagal mengambil data: " + e.getMessage());
                    setEmptyState();
                });

    }

    private void setTextOrDefault(String value, TextView textView, String defaultText) {
        if (value != null && !value.isEmpty()) {
            textView.setText(value);
        } else {
            textView.setText(defaultText);
        }
    }

    private void setKamarKosong(Long totalKamarKosong) {
        if (totalKamarKosong != null) {
            totalkamarkosongTextView.setText("0 / " + totalKamarKosong + " kamar");
        } else {
            totalkamarkosongTextView.setText("0 / 0 Kamar");
        }
    }

    private void setEmptyState() {
        namakostTextView.setText("Kosong");
        alamatTextView.setText("Alamat tidak tersedia");
        totalkamarkosongTextView.setText("0 / 0 Kamar");
    }

    private void opentambahkost() {
        Fragment fragment = new tambah_kost();
        navigateToFragment(fragment);
    }

    private void openinfokost() {
        // Navigasi ke informasi kost
    }

    private void openpenyewa() {
        // Navigasi ke penyewa
    }

    private void openpengumuman() {
        Fragment fragment = new pengumuman();
        navigateToFragment(fragment);
    }

    private void openlistpenyewa() {
        // Navigasi ke list penyewa
    }

    private void navigateToFragment(Fragment fragment) {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frameLayout, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
