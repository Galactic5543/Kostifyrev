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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pending, container, false);

        recyclerView = view.findViewById(R.id.recyclerPending);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RecyclerViewAdapter(inflater);
        recyclerView.setAdapter(adapter);

        textKosong = view.findViewById(R.id.textKosong);

        loadPendingData();

        return view;
    }

    private void loadPendingData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("kostPrefs", Context.MODE_PRIVATE);
        String kostId = sharedPreferences.getString("selectedKostId", null);

        Log.d("PendingFragment", "ID kost dari SharedPreferences: " + kostId);

        if (kostId != null) {
            db.collection("kost")
                    .document(kostId)
                    .collection("penyewa")
                    .whereEqualTo("status", "pending")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<ModelPending> dataList = new ArrayList<>();
                        Log.d("PendingFragment", "Jumlah data pending: " + queryDocumentSnapshots.size());

                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            String nama = document.getString("nama_user");
                            String tanggal = convertTimestamp(document.get("waktu_pengajuan"));
                            if (nama != null && tanggal != null) {
                                dataList.add(new ModelPending(nama, tanggal));
                            }
                        }

                        if (dataList.isEmpty()) {
                            textKosong.setVisibility(View.VISIBLE);
                        } else {
                            textKosong.setVisibility(View.GONE);
                        }

                        adapter.setItems(dataList);
                    })
                    .addOnFailureListener(e -> Log.e("FIRESTORE_ERROR", "Gagal mengambil data: " + e.getMessage()));
        } else {
            Log.e("PendingFragment", "ID kost dari SharedPreferences null!");
        }
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

        public RecyclerViewAdapter(LayoutInflater inflater) {
            this.inflater = inflater;
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
        String nama, tanggal;

        public ModelPending(String nama, String tanggal) {
            this.nama = nama;
            this.tanggal = tanggal;
        }
    }
}
