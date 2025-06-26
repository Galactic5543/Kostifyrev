package com.kostify.app;

import android.content.Context;
import android.content.SharedPreferences;
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
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class notifikasi_pemilik extends Fragment {

    private RecyclerView recyclerView;
    private NotifikasiAdapter adapter;
    private final List<Notifikasi> notifikasiList = new ArrayList<>();

    public notifikasi_pemilik() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifikasi_pemilik, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerNotifikasi);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotifikasiAdapter(notifikasiList);
        recyclerView.setAdapter(adapter);

        ambilDataNotifikasi();

        ImageView icSort = view.findViewById(R.id.icsort);
        icSort.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(requireContext(), v);
            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.popup_sort, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.waktu) {
                    Toast.makeText(requireContext(), "Sortir berdasarkan waktu", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == R.id.Huruf) {
                    Toast.makeText(requireContext(), "Sortir berdasarkan kategori", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            });

            popupMenu.show();
        });

        ImageView icBack = view.findViewById(R.id.ic_back);
        icBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
    }

    private void ambilDataNotifikasi() {
        SharedPreferences prefs = requireContext().getSharedPreferences("kostPrefs", Context.MODE_PRIVATE);
        String kostId = prefs.getString("selectedKostId", null);
        if (kostId == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        notifikasiList.clear();

        ambilDariKoleksi(db, kostId, "pengajuan_kerusakan", "mengajukan kerusakan");
        ambilDariKoleksi(db, kostId, "pengajuan_perpanjangan", "mengajukan perpanjangan");
        // bagian pending sudah dihapus
    }

    private void ambilDariKoleksi(FirebaseFirestore db, String kostId, String collection, String aksi) {
        db.collection("kost").document(kostId).collection(collection)
                .get().addOnSuccessListener(query -> {
                    for (QueryDocumentSnapshot doc : query) {
                        String userId = doc.getString("id_user");
                        Date waktu = doc.getDate("waktu_pengajuan");
                        String status = doc.getString("status");
                        String waktuFormat = waktu != null ? formatTanggal(waktu) : "-";

                        ambilNamaDariUsers(db, userId, aksi, waktuFormat, collection, status);
                    }
                });
    }

    private void ambilNamaDariUsers(FirebaseFirestore db, String userId, String aksi, String waktuFormat, String collection, String status) {
        if (userId == null) return;

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    String nama = snapshot.exists() ? snapshot.getString("nama") : "Pengguna Tidak Dikenal";
                    String detail = nama + " " + aksi;
                    notifikasiList.add(new Notifikasi(aksi, detail, waktuFormat, userId, collection, status));
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    String detail = "Pengguna Tidak Dikenal " + aksi;
                    notifikasiList.add(new Notifikasi(aksi, detail, waktuFormat, userId, collection, status));
                    adapter.notifyDataSetChanged();
                });
    }

    private String formatTanggal(Date date) {
        return new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(date);
    }

    // === MODEL ===
    private static class Notifikasi {
        String judul, detail, waktu, userId, collection, status;

        Notifikasi(String judul, String detail, String waktu, String userId, String collection, String status) {
            this.judul = judul;
            this.detail = detail;
            this.waktu = waktu;
            this.userId = userId;
            this.collection = collection;
            this.status = status;
        }
    }

    // === ADAPTER ===
    private class NotifikasiAdapter extends RecyclerView.Adapter<NotifikasiAdapter.ViewHolder> {

        private final List<Notifikasi> list;

        public NotifikasiAdapter(List<Notifikasi> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_notif_pengajuan_kerusakan, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Notifikasi item = list.get(position);
            holder.judul.setText(capitalize(item.judul));
            holder.detail.setText(item.detail);
            holder.waktu.setText(item.waktu);

            // Jika status terkonfirmasi → pudar
            if ("terkonfirmasi".equalsIgnoreCase(item.status)) {
                holder.itemView.setAlpha(0.5f);
            } else {
                holder.itemView.setAlpha(1f);
            }

            holder.itemView.setOnClickListener(v -> {
                Fragment detailFragment = new detail_pengajuan();
                Bundle bundle = new Bundle();
                bundle.putString("userId", item.userId);
                bundle.putString("collection", item.collection);
                bundle.putString("aksi", item.judul);
                detailFragment.setArguments(bundle);

                View container = requireActivity().findViewById(R.id.fragment_container);
                if (container != null) {
                    container.setVisibility(View.VISIBLE);
                }

                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, detailFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView judul, detail, waktu;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                judul = itemView.findViewById(R.id.judulNotifikasi);
                detail = itemView.findViewById(R.id.detailNotifikasi);
                waktu = itemView.findViewById(R.id.waktuNotifikasi);
            }
        }

        private String capitalize(String text) {
            if (text == null || text.isEmpty()) return "";
            return text.substring(0, 1).toUpperCase() + text.substring(1);
        }
    }
}
