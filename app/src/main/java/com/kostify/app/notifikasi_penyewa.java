package com.kostify.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class notifikasi_penyewa extends Fragment {

    private RecyclerView recyclerView;
    private NotifikasiPenyewaAdapter adapter;
    private final List<NotifikasiPenyewa> notifikasiList = new ArrayList<>();

    public notifikasi_penyewa() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifikasi_penyewa, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inisialisasi komponen UI
        recyclerView = view.findViewById(R.id.recyclerNotifikasiPenyewa);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotifikasiPenyewaAdapter(notifikasiList);
        recyclerView.setAdapter(adapter);

        ImageView icSort = view.findViewById(R.id.icsort);
        icSort.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(requireContext(), v);
            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.popup_sort, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.waktu) {
                    Toast.makeText(requireContext(), "Dipilih: Waktu", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == R.id.Huruf) {
                    Toast.makeText(requireContext(), "Dipilih: Kategori", Toast.LENGTH_SHORT).show();
                    return true;
                } else {
                    return false;
                }
            });
            popupMenu.show();
        });

        ImageView icBack = view.findViewById(R.id.ic_back);
        icBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        // Dummy data (ganti dengan Firestore jika perlu)
        notifikasiList.add(new NotifikasiPenyewa("Perpanjangan Disetujui", "Sewa kamar A1 berhasil diperpanjang", "26 Juni 2025"));
        notifikasiList.add(new NotifikasiPenyewa("Laporan Kerusakan", "Permintaan Anda telah diterima", "24 Juni 2025"));
        adapter.notifyDataSetChanged();
    }

    // ================= MODEL =================
    public static class NotifikasiPenyewa {
        private String judul;
        private String detail;
        private String waktu;

        public NotifikasiPenyewa() {}

        public NotifikasiPenyewa(String judul, String detail, String waktu) {
            this.judul = judul;
            this.detail = detail;
            this.waktu = waktu;
        }

        public String getJudul() {
            return judul;
        }

        public String getDetail() {
            return detail;
        }

        public String getWaktu() {
            return waktu;
        }
    }

    // ================= ADAPTER =================
    public static class NotifikasiPenyewaAdapter extends RecyclerView.Adapter<NotifikasiPenyewaAdapter.ViewHolder> {

        private final List<NotifikasiPenyewa> list;

        public NotifikasiPenyewaAdapter(List<NotifikasiPenyewa> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public NotifikasiPenyewaAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_notif_pengajuan_kerusakan, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull NotifikasiPenyewaAdapter.ViewHolder holder, int position) {
            NotifikasiPenyewa item = list.get(position);
            holder.judul.setText(item.getJudul());
            holder.detail.setText(item.getDetail());
            holder.waktu.setText(item.getWaktu());
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView judul, detail, waktu;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                judul = itemView.findViewById(R.id.judulNotifikasi);
                detail = itemView.findViewById(R.id.detailNotifikasi);
                waktu = itemView.findViewById(R.id.waktuNotifikasi);
            }
        }
    }
}
