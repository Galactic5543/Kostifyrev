package com.kostify.app;

import android.content.Intent;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class join_kost extends Fragment {

    private EditText txtKode;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_join_kost, container, false);

        txtKode = view.findViewById(R.id.txtkode);
        db = FirebaseFirestore.getInstance();

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

        String uid = currentUser.getUid();

        // 🔍 Cek jika user sudah ada di pending kost yang dimasukkan
        db.collection("kost").document(inputIdKost)
                .collection("pending")
                .whereEqualTo("id_user", uid)
                .get()
                .addOnSuccessListener(result -> {
                    if (!result.isEmpty()) {
                        Toast.makeText(getContext(), "Kamu sudah mengajukan permintaan ke kost ini", Toast.LENGTH_LONG).show();
                    } else {
                        // ✅ Lanjut cek apakah sudah join di kost lain
                        cekSudahJoinManapun(uid, isSudahJoin -> {
                            if (isSudahJoin) {
                                Toast.makeText(getContext(), "Kamu sudah bergabung atau pending di kost lain", Toast.LENGTH_LONG).show();
                            } else {
                                prosesSimpanPengajuan(inputIdKost, currentUser);
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Gagal memeriksa status pengajuan", Toast.LENGTH_SHORT).show();
                    Log.e("JoinKost", "Gagal cek pending khusus kost", e);
                });
    }

    private void cekSudahJoinManapun(String userId, OnCekSelesai callback) {
        db.collection("kost").get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot kostDoc : querySnapshot) {
                        String idKost = kostDoc.getId();

                        db.collection("kost").document(idKost).collection("pending")
                                .whereEqualTo("id_user", userId)
                                .get()
                                .addOnSuccessListener(pendingSnapshot -> {
                                    if (!pendingSnapshot.isEmpty()) {
                                        callback.onCek(true);
                                    } else {
                                        db.collection("kost").document(idKost).collection("penyewa")
                                                .whereEqualTo("id_user", userId)
                                                .get()
                                                .addOnSuccessListener(penyewaSnapshot -> {
                                                    callback.onCek(!penyewaSnapshot.isEmpty());
                                                })
                                                .addOnFailureListener(e -> callback.onCek(false));
                                    }
                                })
                                .addOnFailureListener(e -> callback.onCek(false));
                    }
                })
                .addOnFailureListener(e -> callback.onCek(false));
    }

    interface OnCekSelesai {
        void onCek(boolean isSudahJoin);
    }

    private void prosesSimpanPengajuan(String inputIdKost, FirebaseUser currentUser) {
        DocumentReference kostRef = db.collection("kost").document(inputIdKost);
        String uid = currentUser.getUid();

        String displayName = currentUser.getDisplayName();
        if (displayName == null || displayName.isEmpty()) {
            db.collection("users").document(uid).get()
                    .addOnSuccessListener(userDoc -> {
                        String namaUser = userDoc.contains("nama") ? userDoc.getString("nama") : "Pengguna Tanpa Nama";
                        simpanPengajuan(kostRef, uid, namaUser);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Gagal mengambil nama user", Toast.LENGTH_SHORT).show();
                        Log.e("JoinKost", "Gagal ambil nama user", e);
                    });
        } else {
            simpanPengajuan(kostRef, uid, displayName);
        }
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

                    Intent intent = new Intent(requireContext(), menu_utama_navigasi.class);
                    intent.putExtra("fragment", "profil");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Gagal menyimpan pengajuan", Toast.LENGTH_SHORT).show();
                    Log.e("JoinKost", "Gagal simpan data penyewa", e);
                });
    }
}
