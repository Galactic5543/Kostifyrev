package com.kostify.app;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class window_tambah_kamar extends DialogFragment {

    private EditText txtJumlahKamar;
    private Button btnTambah;
    private FirebaseFirestore db;

    public window_tambah_kamar() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_window_tambah_kamar, container, false);

        txtJumlahKamar = view.findViewById(R.id.txtjumlahkamar);
        btnTambah = view.findViewById(R.id.btntambah);
        db = FirebaseFirestore.getInstance();

        btnTambah.setOnClickListener(v -> tambahKamarBaru());

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void tambahKamarBaru() {
        String jumlahInput = txtJumlahKamar.getText().toString().trim();

        if (TextUtils.isEmpty(jumlahInput)) {
            Toast.makeText(getContext(), "Masukkan jumlah kamar", Toast.LENGTH_SHORT).show();
            return;
        }

        int jumlahTambah;
        try {
            jumlahTambah = Integer.parseInt(jumlahInput);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Input harus berupa angka", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = requireContext().getSharedPreferences("kostPrefs", Context.MODE_PRIVATE);
        String idKost = prefs.getString("selectedKostId", null);

        if (idKost == null) {
            Toast.makeText(getContext(), "ID kost tidak ditemukan", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ambil semua kamar dulu untuk hitung terakhir
        db.collection("kost").document(idKost).collection("kamar")
                .get()
                .addOnSuccessListener(snapshot -> {
                    int kamarTerakhir = snapshot.size();

                    for (int i = 1; i <= jumlahTambah; i++) {
                        int nomorBaru = kamarTerakhir + i;
                        String idKamar = "kamar_" + nomorBaru;
                        String namaKamar = "Kamar " + nomorBaru;

                        Map<String, Object> dataKamar = new HashMap<>();
                        dataKamar.put("nama_kamar", namaKamar);
                        dataKamar.put("status", "Kosong");
                        dataKamar.put("penyewa", "-");

                        db.collection("kost").document(idKost)
                                .collection("kamar")
                                .document(idKamar) // <== pakai ID manual
                                .set(dataKamar);
                    }

                    Toast.makeText(getContext(), "Berhasil menambah " + jumlahTambah + " kamar", Toast.LENGTH_SHORT).show();
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Gagal mengambil data kamar", Toast.LENGTH_SHORT).show();
                });
    }
}
