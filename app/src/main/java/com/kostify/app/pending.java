package com.kostify.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class pending extends Fragment {

    private RecyclerView recyclerView;
    private RecyclerViewAdapter adapter;
    private TextView textKosong;
    private FirebaseFirestore db;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pending, container, false);

        recyclerView = view.findViewById(R.id.recyclerPending);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        textKosong = view.findViewById(R.id.textKosong);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        adapter = new RecyclerViewAdapter(inflater, item -> {
            Fragment konfirmasiFragment = new Konfirmasi_pending();

            Bundle bundle = new Bundle();
            bundle.putString("nama", item.nama);
            bundle.putString("tanggal", item.tanggal);
            bundle.putString("id_user", item.idUser);
            konfirmasiFragment.setArguments(bundle);

            if (getActivity() instanceof nav_list_penyewa) {
                ((nav_list_penyewa) getActivity()).goToKonfirmasiFragment(item.nama, item.tanggal, item.idUser);
            }
        });

        recyclerView.setAdapter(adapter);

        // Mulai load data jika pemilik valid
        cekPemilikDanTampilkanPending();

        return view;
    }

    private void cekPemilikDanTampilkanPending() {
        SharedPreferences prefs = requireContext().getSharedPreferences("kostPrefs", Context.MODE_PRIVATE);
        String kostId = prefs.getString("selectedKostId", null);

        if (kostId == null || currentUserId == null) {
            Log.e("PendingFragment", "ID kost atau user null");
            textKosong.setText("Kost tidak ditemukan");
            textKosong.setVisibility(View.VISIBLE);
            return;
        }

        db.collection("kost").document(kostId).get()
                .addOnSuccessListener(doc -> {
                    String pemilikId = doc.getString("userId");
                    if (pemilikId != null && pemilikId.equals(currentUserId)) {
                        // ✅ hanya pemilik bisa lihat data pending
                        loadPendingData(kostId);
                    } else {
                        textKosong.setText("Tidak ada penyewa pending");
                        textKosong.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    textKosong.setText("Gagal memverifikasi pemilik");
                    textKosong.setVisibility(View.VISIBLE);
                });
    }

    private void loadPendingData(String kostId) {
        db.collection("kost")
                .document(kostId)
                .collection("pending")
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    List<ModelPending> dataList = new ArrayList<>();

                    for (DocumentSnapshot doc : querySnapshots) {
                        String nama = doc.getString("nama_user");
                        String tanggal = convertTimestamp(doc.get("waktu_pengajuan"));
                        String idUser = doc.getString("id_user");

                        if (nama != null && tanggal != null && idUser != null) {
                            dataList.add(new ModelPending(nama, tanggal, idUser));
                        }
                    }

                    textKosong.setVisibility(dataList.isEmpty() ? View.VISIBLE : View.GONE);
                    recyclerView.setVisibility(dataList.isEmpty() ? View.GONE : View.VISIBLE);
                    adapter.setItems(dataList);
                })
                .addOnFailureListener(e -> {
                    Log.e("PendingFragment", "Gagal mengambil data pending: " + e.getMessage());
                    textKosong.setText("Gagal mengambil data");
                    textKosong.setVisibility(View.VISIBLE);
                });
    }

    private String convertTimestamp(Object waktuObj) {
        if (waktuObj instanceof Timestamp) {
            Date date = ((Timestamp) waktuObj).toDate();
            return new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(date);
        } else {
            return "Tanggal tidak valid";
        }
    }

    private static class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
        private final LayoutInflater inflater;
        private final List<ModelPending> items = new ArrayList<>();
        private final OnItemClickListener listener;

        interface OnItemClickListener {
            void onItemClick(ModelPending item);
        }

        public RecyclerViewAdapter(LayoutInflater inflater, OnItemClickListener listener) {
            this.inflater = inflater;
            this.listener = listener;
        }

        public void setItems(List<ModelPending> newItems) {
            items.clear();
            items.addAll(newItems);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.fragment_item_penyewa_pending, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ModelPending item = items.get(position);
            holder.nama.setText(item.nama);
            holder.tanggal.setText("Tanggal Pengajuan : " + item.tanggal);
            holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView nama, tanggal;
            ImageView notifIcon;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                nama = itemView.findViewById(R.id.namapenghuni);
                tanggal = itemView.findViewById(R.id.tanggalpengajuan);
                notifIcon = itemView.findViewById(R.id.ic_notif);
            }
        }
    }

    private static class ModelPending {
        String nama, tanggal, idUser;

        public ModelPending(String nama, String tanggal, String idUser) {
            this.nama = nama;
            this.tanggal = tanggal;
            this.idUser = idUser;
        }
    }
}
