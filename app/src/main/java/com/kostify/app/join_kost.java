package com.kostify.app;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class join_kost extends Fragment {

    private EditText txtKode;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_join_kost, container, false);

        txtKode = view.findViewById(R.id.txtkode);

        view.findViewById(R.id.btnkonfirmasi).setOnClickListener(v -> ajukanGabungKost());
        view.findViewById(R.id.btnkembali).setOnClickListener(v -> requireActivity().onBackPressed());

        return view;
    }

    private void ajukanGabungKost() {
        String inputIdKost = txtKode.getText().toString().trim();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (inputIdKost.isEmpty()) {
            Toast.makeText(getContext(), "Masukkan ID Kost", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUser == null) {
            Toast.makeText(getContext(), "User belum login", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference kostRef = db.collection("kost").document(inputIdKost);

        kostRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String displayName = currentUser.getDisplayName();

                if (displayName == null || displayName.isEmpty()) {
                    // Ambil nama dari koleksi 'users/<uid>/nama'
                    db.collection("users").document(currentUser.getUid())
                            .get()
                            .addOnSuccessListener(userDoc -> {
                                String namaUser = userDoc.contains("nama") ? userDoc.getString("nama") : "Pengguna Tanpa Nama";
                                simpanPengajuan(kostRef, currentUser.getUid(), namaUser);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Gagal mengambil nama user", Toast.LENGTH_SHORT).show();
                                Log.e("JoinKost", "Error ambil nama dari koleksi users", e);
                            });
                } else {
                    simpanPengajuan(kostRef, currentUser.getUid(), displayName);
                }
            } else {
                Toast.makeText(getContext(), "ID Kost tidak ditemukan", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Gagal mengambil data kost", Toast.LENGTH_SHORT).show();
            Log.e("JoinKost", "Error mengambil data kost", e);
        });
    }
    private void simpanPengajuan(DocumentReference kostRef, String userId, String namaUser) {
        Map<String, Object> penyewaData = new HashMap<>();
        penyewaData.put("id_user", userId);
        penyewaData.put("nama_user", namaUser);
        penyewaData.put("status", "pending");
        penyewaData.put("waktu_pengajuan", FieldValue.serverTimestamp());

        kostRef.collection("pending")
                .add(penyewaData)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(getContext(), "Pengajuan berhasil, menunggu persetujuan", Toast.LENGTH_SHORT).show();
                    txtKode.setText("");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Gagal menyimpan pengajuan", Toast.LENGTH_SHORT).show();
                    Log.e("JoinKost", "Error menyimpan data penyewa", e);
                });
    }


}
