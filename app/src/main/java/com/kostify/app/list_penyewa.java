package com.kostify.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class list_penyewa extends Fragment {

    private RecyclerView recyclerView;
    private final List<ModelPenyewa> penyewaList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_penyewa, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerPenyewa);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new PenyewaAdapter());

        loadDataPenyewa();
    }

    private void loadDataPenyewa() {
        SharedPreferences prefs = requireContext().getSharedPreferences("kostPrefs", Context.MODE_PRIVATE);
        String selectedKostId = prefs.getString("selectedKostId", null);

        if (selectedKostId == null) {
            Toast.makeText(getContext(), "ID Kost tidak ditemukan", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference penyewaRef = db.collection("kost")
                .document(selectedKostId)
                .collection("penyewa");

        penyewaRef.get()
                .addOnSuccessListener(querySnapshots -> {
                    penyewaList.clear();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                    for (DocumentSnapshot doc : querySnapshots.getDocuments()) {
                        String nama = doc.getString("nama_penyewa");
                        String kamar = doc.getString("kamar");
                        String durasi = doc.getString("durasi");
                        Timestamp perpanjangTerakhir = doc.getTimestamp("perpanjang_terakhir");

                        String tanggalGabung = "-";
                        String masaSewaHingga = "-";

                        if (perpanjangTerakhir != null) {
                            Date tanggal = perpanjangTerakhir.toDate();
                            tanggalGabung = sdf.format(tanggal);
                            masaSewaHingga = hitungMasaSewa(tanggal, durasi);
                        }

                        penyewaList.add(new ModelPenyewa(nama, kamar, tanggalGabung, masaSewaHingga));
                    }

                    recyclerView.getAdapter().notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Gagal mengambil data penyewa", Toast.LENGTH_SHORT).show());
    }

    private String hitungMasaSewa(Date tanggalAwal, String durasi) {
        if (tanggalAwal == null || durasi == null) return "-";

        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(tanggalAwal);

            int jumlahBulan = 0;
            if (durasi.toLowerCase().contains("bulan")) {
                jumlahBulan = Integer.parseInt(durasi.split(" ")[0]);
            }

            cal.add(Calendar.MONTH, jumlahBulan);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return sdf.format(cal.getTime());
        } catch (Exception e) {
            e.printStackTrace();
            return "-";
        }
    }

    // ===================== MODEL ======================
    private static class ModelPenyewa {
        String nama, kamar, tanggalGabung, masaSewaHingga;

        public ModelPenyewa(String nama, String kamar, String tanggalGabung, String masaSewaHingga) {
            this.nama = nama;
            this.kamar = kamar;
            this.tanggalGabung = tanggalGabung;
            this.masaSewaHingga = masaSewaHingga;
        }
    }

    // ===================== ADAPTER ======================
    private class PenyewaAdapter extends RecyclerView.Adapter<PenyewaAdapter.PenyewaViewHolder> {

        @NonNull
        @Override
        public PenyewaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_list_penyewa, parent, false);
            return new PenyewaViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PenyewaViewHolder holder, int position) {
            ModelPenyewa penyewa = penyewaList.get(position);

            holder.namaPenghuni.setText(penyewa.nama != null ? penyewa.nama : "-");
            holder.kamarText.setText(penyewa.kamar != null ? penyewa.kamar : "-");
            holder.masaSewaText.setText("Masa Sewa Hingga : " + penyewa.masaSewaHingga);
            holder.tanggalGabungText.setText("Bergabung pada : " + penyewa.tanggalGabung);
        }

        @Override
        public int getItemCount() {
            return penyewaList.size();
        }

        class PenyewaViewHolder extends RecyclerView.ViewHolder {
            TextView namaPenghuni, kamarText, masaSewaText, tanggalGabungText;

            public PenyewaViewHolder(@NonNull View itemView) {
                super(itemView);
                namaPenghuni = itemView.findViewById(R.id.namapenghuni);
                kamarText = itemView.findViewById(R.id.kamar);
                masaSewaText = itemView.findViewById(R.id.textperiodesewa);
                tanggalGabungText = itemView.findViewById(R.id.texttanggalmasuk);
            }
        }
    }
}
