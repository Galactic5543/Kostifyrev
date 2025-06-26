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

import java.util.HashMap;
import java.util.Map;

public class window_edit_kamar extends DialogFragment {

    private EditText txtNamaKamar;
    private Button btnSimpan, btnHapus;

    private String kamarId;        // ID dokumen kamar di Firestore
    private String kamarNamaLama;  // Nama kamar sebelumnya

    private FirebaseFirestore db;

    public window_edit_kamar() {
        // Konstruktor default
    }

    // ======= Factory method untuk mengirim data ke dialog ========
    public static window_edit_kamar newInstance(String kamarId, String namaKamar) {
        window_edit_kamar fragment = new window_edit_kamar();
        Bundle args = new Bundle();
        args.putString("kamar_id", kamarId);
        args.putString("nama_kamar", namaKamar);
        fragment.setArguments(args);
        return fragment;
    }

    // ======= Atur tampilan dialog agar transparan dan lebar penuh ========
    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    // ======= Inflate layout dan inisialisasi =========
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_window_edit_kamar, container, false);

        txtNamaKamar = view.findViewById(R.id.txtnamakamar);
        btnSimpan = view.findViewById(R.id.btntambah);
        btnHapus = view.findViewById(R.id.btnhapus); // tombol hapus
        db = FirebaseFirestore.getInstance();

        // Ambil data dari argumen
        if (getArguments() != null) {
            kamarId = getArguments().getString("kamar_id");
            kamarNamaLama = getArguments().getString("nama_kamar");
            txtNamaKamar.setText(kamarNamaLama);
        }

        // Saat tombol disimpan ditekan
        btnSimpan.setOnClickListener(v -> simpanPerubahan());

        // Saat tombol hapus ditekan
        btnHapus.setOnClickListener(v -> hapusKamar());

        return view;
    }

    // ======= Simpan perubahan ke Firestore =========
    private void simpanPerubahan() {
        String namaBaru = txtNamaKamar.getText().toString().trim();

        if (TextUtils.isEmpty(namaBaru)) {
            Toast.makeText(getContext(), "Nama kamar tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = requireContext().getSharedPreferences("kostPrefs", Context.MODE_PRIVATE);
        String kostId = prefs.getString("selectedKostId", null);

        if (kostId == null || kamarId == null) {
            Toast.makeText(getContext(), "Data tidak lengkap", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> update = new HashMap<>();
        update.put("nama_kamar", namaBaru);

        db.collection("kost").document(kostId)
                .collection("kamar").document(kamarId)
                .update(update)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Nama kamar diperbarui", Toast.LENGTH_SHORT).show();
                    dismiss(); // Tutup popup setelah berhasil
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Gagal memperbarui data", Toast.LENGTH_SHORT).show();
                });
    }

    // ======= Hapus kamar dari Firestore =========
    private void hapusKamar() {
        SharedPreferences prefs = requireContext().getSharedPreferences("kostPrefs", Context.MODE_PRIVATE);
        String kostId = prefs.getString("selectedKostId", null);

        if (kostId == null || kamarId == null) {
            Toast.makeText(getContext(), "Data tidak lengkap", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("kost").document(kostId)
                .collection("kamar").document(kamarId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Kamar berhasil dihapus", Toast.LENGTH_SHORT).show();
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Gagal menghapus kamar", Toast.LENGTH_SHORT).show();
                });
    }
}
