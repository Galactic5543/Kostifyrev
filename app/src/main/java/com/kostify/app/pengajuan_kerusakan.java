package com.kostify.app;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class pengajuan_kerusakan extends Fragment {

    private Spinner spinnerKerusakan;
    private EditText inputDeskripsi;
    private Button btnAjukan;
    private FirebaseFirestore db;

    public pengajuan_kerusakan() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pengajuan_kerusakan, container, false);

        spinnerKerusakan = view.findViewById(R.id.dropdownkerusakan);
        inputDeskripsi = view.findViewById(R.id.textperaturan);
        btnAjukan = view.findViewById(R.id.btnajukankerusakan);
        db = FirebaseFirestore.getInstance();

        // Isi spinner dari array XML
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.list_kerusakan,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerKerusakan.setAdapter(adapter);

        btnAjukan.setOnClickListener(v -> simpanPengajuanKerusakan());

        return view;
    }

    private void simpanPengajuanKerusakan() {
        String jenis = spinnerKerusakan.getSelectedItem().toString();
        String deskripsi = inputDeskripsi.getText().toString().trim();
        String userId = FirebaseAuth.getInstance().getUid();

        Log.d("DEBUG_KERUSAKAN", "Jenis: " + jenis);
        Log.d("DEBUG_KERUSAKAN", "Deskripsi: " + deskripsi);
        Log.d("DEBUG_KERUSAKAN", "User ID: " + userId);

        if (TextUtils.isEmpty(deskripsi)) {
            Toast.makeText(getContext(), "Deskripsi tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userId == null) {
            Toast.makeText(getContext(), "User belum login", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("kost").get().addOnSuccessListener(snapshot -> {
            boolean[] pengajuanSudahDikirim = {false};

            Log.d("DEBUG_KERUSAKAN", "Jumlah kost: " + snapshot.size());

            for (DocumentSnapshot kostDoc : snapshot.getDocuments()) {
                if (pengajuanSudahDikirim[0]) break;

                String idKost = kostDoc.getId();
                Log.d("DEBUG_KERUSAKAN", "Cek penyewa di kost ID: " + idKost);

                db.collection("kost").document(idKost)
                        .collection("penyewa")
                        .whereEqualTo("id_user", userId) // pakai whereEqualTo bukan .document(userId)
                        .get()
                        .addOnSuccessListener(penyewaSnapshot -> {
                            if (!penyewaSnapshot.isEmpty() && !pengajuanSudahDikirim[0]) {
                                pengajuanSudahDikirim[0] = true;

                                Map<String, Object> data = new HashMap<>();
                                data.put("id_user", userId);
                                data.put("jenis_kerusakan", jenis);
                                data.put("deskripsi", deskripsi);
                                data.put("waktu_pengajuan", Timestamp.now());
                                data.put("status", "diajukan");

                                Log.d("DEBUG_KERUSAKAN", "Mengirim pengajuan ke kost: " + idKost);

                                db.collection("kost").document(idKost)
                                        .collection("pengajuan_kerusakan")
                                        .add(data)
                                        .addOnSuccessListener(docRef -> {
                                            Toast.makeText(getContext(), "Pengajuan berhasil dikirim", Toast.LENGTH_SHORT).show();
                                            inputDeskripsi.setText("");
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("DEBUG_KERUSAKAN", "Gagal menyimpan data: " + e.getMessage());
                                            Toast.makeText(getContext(), "Gagal menyimpan data", Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                Log.d("DEBUG_KERUSAKAN", "User tidak ditemukan di penyewa kost ID: " + idKost);
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("DEBUG_KERUSAKAN", "Gagal cek penyewa: " + e.getMessage());
                            Toast.makeText(getContext(), "Gagal memeriksa status penyewa", Toast.LENGTH_SHORT).show();
                        });
            }
        }).addOnFailureListener(e -> {
            Log.e("DEBUG_KERUSAKAN", "Gagal ambil data kost: " + e.getMessage());
            Toast.makeText(getContext(), "Gagal mengambil data kost", Toast.LENGTH_SHORT).show();
        });
    }

}
