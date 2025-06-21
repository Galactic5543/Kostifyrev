package com.kostify.app;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
public class tambah_kost extends Fragment {

    private EditText txtNamaKost, txtAlamat, txtJumlahKamar, txtLinkGrup, txtPeraturan;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate layout fragment_tambah_kost
        View view = inflater.inflate(R.layout.fragment_tambah_kost, container, false);

        // Inisialisasi Firestore
        db = FirebaseFirestore.getInstance();

        // Inisialisasi view
        txtNamaKost = view.findViewById(R.id.txtnamakost);
        txtAlamat = view.findViewById(R.id.txtalamat);
        txtJumlahKamar = view.findViewById(R.id.txtjumlahkamar);
        txtLinkGrup = view.findViewById(R.id.txtlinkgrupwhatsapp);
        txtPeraturan = view.findViewById(R.id.txtperaturan);
        Button btnTambahKost = view.findViewById(R.id.btntambahkost);

        // Tombol kembali
        view.findViewById(R.id.ic_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToPenyewaFragment();
            }
        });

        // Tombol tambah kost
        btnTambahKost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tambahKost();
            }
        });

        return view;
    }

    private void tambahKost() {
        String nama = txtNamaKost.getText().toString().trim();
        String alamat = txtAlamat.getText().toString().trim();
        String jumlahStr = txtJumlahKamar.getText().toString().trim();
        String linkGrup = txtLinkGrup.getText().toString().trim();
        String peraturan = txtPeraturan.getText().toString().trim();

        if (TextUtils.isEmpty(nama) || TextUtils.isEmpty(alamat) || TextUtils.isEmpty(jumlahStr)) {
            Toast.makeText(getContext(), "Harap isi semua data", Toast.LENGTH_SHORT).show();
            return;
        }

        int jumlahKamar;
        try {
            jumlahKamar = Integer.parseInt(jumlahStr);
            if (jumlahKamar <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Jumlah kamar harus berupa angka positif", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "User belum login", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();

        // Buat ID dokumen baru secara manual
        DocumentReference kostRef = db.collection("kost").document();
        String kostId = kostRef.getId();

        // Buat data kost
        Map<String, Object> dataKost = new HashMap<>();
        dataKost.put("id_kost", kostId);
        dataKost.put("nama_kost", nama);
        dataKost.put("alamat", alamat);
        dataKost.put("jumlah_kamar", jumlahKamar);
        dataKost.put("userId", userId);
        dataKost.put("link_grup", linkGrup);
        dataKost.put("peraturan", peraturan);

        // Simpan data langsung ke dokumen utama (bukan subcollection)
        kostRef.set(dataKost)
                .addOnSuccessListener(aVoid -> {
                    // Buat kamar otomatis
                    buatKamarUntukKost(kostId, jumlahKamar);

                    Toast.makeText(getContext(), "Kost berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                    backToPenyewaFragment();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Gagal menambahkan data kost", Toast.LENGTH_SHORT).show();
                });
    }


    private void buatKamarUntukKost(String kostId, int jumlahKamar) {
        for (int i = 1; i <= jumlahKamar; i++) {
            String namaKamar = "Kamar " + i;

            Map<String, Object> dataKamar = new HashMap<>();
            dataKamar.put("nama_kamar", namaKamar);
            dataKamar.put("status", "Kosong");
            dataKamar.put("penyewa", "-");

            db.collection("kost").document(kostId)
                    .collection("kamar")
                    .document("kamar_" + i)
                    .set(dataKamar);
        }
    }

    private void backToPenyewaFragment() {
        Pemilik pemilikFragment = new Pemilik();

        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.frameLayout, pemilikFragment)
                .addToBackStack(null)
                .commit();
    }
}

