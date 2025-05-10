package com.kostify.app;

import android.content.Intent;
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

    private static final String DEFAULT_KOST_OPTION = "Pilih Kost";  // Konstanta untuk "Pilih Kost"
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private TextView namakostTextView, alamatTextView, totalkamarkosongTextView;
    private Spinner gantikost;  // Declare Spinner globally
    private ArrayList<String> kostNames; // ArrayList to hold kost names
    private String selectedKostId; // Variable to store the selected Kost ID

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
                if (!selectedKost.equals(DEFAULT_KOST_OPTION)) {  // Avoid querying for "Pilih Kost"
                    updateKostData(selectedKost);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Jika tidak ada yang dipilih, kosongkan data
                setEmptyState();
            }
        });

        // Menambahkan fungsi untuk menghapus kost
        view.findViewById(R.id.btnhapuskost).setOnClickListener(v -> hapusKost());

        // Navigasi ke berbagai fragment
        view.findViewById(R.id.mntambahkost).setOnClickListener(v -> opentambahkost());
        view.findViewById(R.id.mninfokost).setOnClickListener(v -> openinfoKost());
        view.findViewById(R.id.mnpenyewa).setOnClickListener(v -> openlistpenyewa());
        view.findViewById(R.id.mnpengumuman).setOnClickListener(v -> openpengumuman());
        view.findViewById(R.id.btndetailpenyewa).setOnClickListener(v -> openlistpenyewa());

        return view;
    }




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
                    if (!queryDocumentSnapshots.isEmpty()) {
                        kostNames.clear();
                        kostNames.add(DEFAULT_KOST_OPTION);  // "Pilih Kost"
                        // Menambahkan nama kost ke list
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            String namaKost = documentSnapshot.getString("nama");
                            if (namaKost != null && !namaKost.isEmpty()) {
                                kostNames.add(namaKost);
                            }
                        }

                        // Menambahkan "Kosong" jika tidak ada data
                        if (kostNames.size() == 1) {  // Jika hanya ada pilihan "Pilih Kost"
                            kostNames.add("Kosong");
                        }

                        if (isAdded()) {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, kostNames);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            gantikost.setAdapter(adapter);

                            // Pilih kost terbaru (yang terakhir ditambahkan)
                            if (kostNames.size() > 1) {
                                gantikost.setSelection(kostNames.size() - 1);
                            } else {
                                // Jika hanya ada "Kosong", pilih itu
                                gantikost.setSelection(0);
                            }
                        }

                    } else {
                        Log.d("Pemilik", "Dokumen tidak ditemukan.");
                        if (isAdded()) {
                            // Menambahkan "Kosong" jika tidak ada kost sama sekali
                            kostNames.clear();
                            kostNames.add("Kosong");
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, kostNames);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            gantikost.setAdapter(adapter);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d("Pemilik", "Gagal mengambil data: " + e.getMessage());
                    if (isAdded()) {
                        setEmptyState();
                    }
                });
    }


    private void updateKostData(String kostName) {
        if (auth.getCurrentUser() == null) {
            Log.d("Pemilik", "User belum login.");
            return;
        }

        String userId = auth.getCurrentUser().getUid(); // Mendapatkan userId dari Firebase Auth

        // Melakukan query untuk mencari kost berdasarkan nama dan userId
        db.collection("kost")
                .whereEqualTo("userId", userId)
                .whereEqualTo("nama", kostName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Cek jika query tidak kosong
                        QueryDocumentSnapshot documentSnapshot = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                        selectedKostId = documentSnapshot.getId(); // Menyimpan ID Kost yang dipilih
                        String alamat = documentSnapshot.getString("alamat");
                        Long totalKamar = documentSnapshot.getLong("jumlah_kamar");

                        Log.d("Pemilik", "Nama Kost: " + kostName + ", Alamat: " + alamat + ", Jumlah Kamar: " + totalKamar);

                        // Set nama kost ke TextView
                        namakostTextView.setText(kostName);

                        // Set alamat ke TextView
                        setTextOrDefault(alamat, alamatTextView, "Alamat tidak tersedia");


                        // Set jumlah kamar ke TextView
                        if (totalKamar != null) {
                            totalkamarkosongTextView.setText( totalKamar + " / "+ totalKamar + " Kamar");
                        } else {
                            totalkamarkosongTextView.setText("Jumlah Kamar tidak tersedia");
                        }

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


    private void hapusKost() {
        if (auth.getCurrentUser() == null || selectedKostId == null) {
            Log.d("Pemilik", "Tidak ada kost yang dipilih.");
            return;
        }

        // Menghapus koleksi kost yang dipilih
        db.collection("kost").document(selectedKostId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("Pemilik", "Kost berhasil dihapus.");
                    // Mengupdate Spinner dan TextView
                    fetchKostNames();  // Update spinner setelah penghapusan

                    // Mengatur spinner ke posisi default (Pilih Kost)
                    gantikost.setSelection(0);

                    // Reset data TextView
                    setEmptyState();
                })
                .addOnFailureListener(e -> Log.d("Pemilik", "Gagal menghapus kost: " + e.getMessage()));
    }


    private void setTextOrDefault(String value, TextView textView, String defaultText) {
        if (value != null && !value.isEmpty()) {
            textView.setText(value);
        } else {
            textView.setText(defaultText);
        }
    }

    private void setEmptyState() {
        namakostTextView.setText("Kosong");
        alamatTextView.setText("Alamat tidak tersedia");
        totalkamarkosongTextView.setText("0 / 0 Kamar");
    }

    private void opentambahkost() {
        Fragment fragment = new tambah_kost();  // Perbaikan penamaan
        navigateToFragment(fragment);
    }

    private void openinfoKost() {
        Intent intent = new Intent(requireActivity(), navigasi_info_kost.class);  // Perbaikan penamaan
        startActivity(intent);
    }

    private void openpengumuman() {
        Fragment fragment = new pengumuman();  // Perbaikan penamaan
        navigateToFragment(fragment);
    }

    private void openlistpenyewa() {
        Intent intent = new Intent(requireActivity(), nav_list_penyewa.class);  // Perbaikan penamaan
        startActivity(intent);
    }

    private void navigateToFragment(Fragment fragment) {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frameLayout, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
    }


