package com.kostify.app;

import android.app.Dialog;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class riwayat_pembayaran extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Menginflasi layout fragment_riwayat_pembayaran
        View view = inflater.inflate(R.layout.fragment_riwayat_pembayaran, container, false);

        // Menambahkan event listener untuk tombol Detail
        Button btnDetail = view.findViewById(R.id.btndetail);
        btnDetail.setOnClickListener(v -> showPopupDetailPembayaran());

        return view;
    }

    private void showPopupDetailPembayaran() {
        // Membuat dialog baru
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.fragment_popup_detail_riwayat_pembayaran); // Layout popup
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.background_light); // Membuat background transparan
        dialog.setCancelable(true);

        // Ambil referensi view di popup
        TextView textKamar = dialog.findViewById(R.id.textkamar);
        TextView textPenyewa = dialog.findViewById(R.id.textpenyewa);
        TextView textDurasi = dialog.findViewById(R.id.textdurasisewa);
        TextView textBulan = dialog.findViewById(R.id.textpembayaranbulan);
        TextView textStatus = dialog.findViewById(R.id.textstatus);
        TextView textHarga = dialog.findViewById(R.id.texthargasewa);
        TextView textJatuhTempo = dialog.findViewById(R.id.textjatuhtempo);
        Button btnTutup = dialog.findViewById(R.id.txttutup);

        // Set data jika ingin diisi dinamis (opsional)
        textKamar.setText("Kamar A1");
        textPenyewa.setText("Penyewa : Fajar Nugroho");
        textDurasi.setText("Durasi Sewa : 1 Bulan");
        textBulan.setText("Pembayaran Bulan : April");
        textStatus.setText("Status Pembayaran : Telat");
        textHarga.setText("Rp. 1.500.000");
        textJatuhTempo.setText("Jatuh Tempo : 30/3/2025");

        // Event tombol tutup
        btnTutup.setOnClickListener(v -> dialog.dismiss());

        // Tampilkan popup
        dialog.show();
    }
}
