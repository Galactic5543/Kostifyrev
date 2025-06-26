package com.kostify.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class list_Kamar extends Fragment {

    private RecyclerView recyclerView;
    private KamarAdapter adapter;
    private final List<Kamar> kamarList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list__kamar, container, false);

        recyclerView = view.findViewById(R.id.recyclerKamar);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new KamarAdapter(getContext(), kamarList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        Button btnTambah = view.findViewById(R.id.btntambahkamar);
        btnTambah.setOnClickListener(v -> {
            window_tambah_kamar dialog = new window_tambah_kamar();
            dialog.show(getParentFragmentManager(), "TambahKamarDialog");
        });

        ambilDataKamar();
        return view;
    }

    private void ambilDataKamar() {
        SharedPreferences prefs = requireContext().getSharedPreferences("kostPrefs", Context.MODE_PRIVATE);
        String selectedKostId = prefs.getString("selectedKostId", null);

        if (selectedKostId == null) {
            Toast.makeText(getContext(), "ID Kost tidak ditemukan", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("KAMAR_DEBUG", "ID Kost dari SharedPreferences: " + selectedKostId);

        CollectionReference kamarRef = db.collection("kost").document(selectedKostId).collection("kamar");

        kamarRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            kamarList.clear();
            if (queryDocumentSnapshots.isEmpty()) {
                Log.d("KAMAR_DEBUG", "Tidak ada kamar ditemukan.");
            } else {
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    Kamar kamar = doc.toObject(Kamar.class);
                    kamar.setId(doc.getId()); // <-- Simpan ID dokumen Firestore
                    kamarList.add(kamar);
                }
            }
            adapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            Log.e("KAMAR_DEBUG", "Gagal mengambil data kamar: " + e.getMessage());
        });
    }

    // ==================== MODEL ====================
    public static class Kamar {
        private String id; // ← Firestore Document ID
        private String nama_kamar;
        private String status;
        private String penyewa;

        public Kamar() {}

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getNama_kamar() {
            return nama_kamar != null ? nama_kamar : "-";
        }

        public String getStatus() {
            return status != null ? status : "-";
        }

        public String getPenyewa() {
            return penyewa != null ? penyewa : "-";
        }
    }

    // ==================== ADAPTER ====================
    public class KamarAdapter extends RecyclerView.Adapter<KamarAdapter.KamarViewHolder> {

        private final Context context;
        private final List<Kamar> kamarList;

        public KamarAdapter(Context context, List<Kamar> kamarList) {
            this.context = context;
            this.kamarList = kamarList;
        }

        @NonNull
        @Override
        public KamarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.fragment_item_list_kamar, parent, false);
            return new KamarViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull KamarViewHolder holder, int position) {
            Kamar kamar = kamarList.get(position);
            holder.namaKamar.setText(kamar.getNama_kamar());
            holder.status.setText("Status : " + kamar.getStatus());
            holder.penyewa.setText("Penyewa : " + kamar.getPenyewa());

            // Tombol Edit
            holder.btnEdit.setOnClickListener(v -> {
                window_edit_kamar dialog = window_edit_kamar.newInstance(kamar.getId(), kamar.getNama_kamar());
                dialog.show(((FragmentActivity) context).getSupportFragmentManager(), "EditKamarDialog");
            });
        }

        @Override
        public int getItemCount() {
            return kamarList.size();
        }

        class KamarViewHolder extends RecyclerView.ViewHolder {
            TextView namaKamar, status, penyewa;
            Button btnEdit;

            public KamarViewHolder(@NonNull View itemView) {
                super(itemView);
                namaKamar = itemView.findViewById(R.id.textnamakamar);
                status = itemView.findViewById(R.id.textstatus);
                penyewa = itemView.findViewById(R.id.textpenyewa);
                btnEdit = itemView.findViewById(R.id.btneditkamar);
            }
        }
    }
}
