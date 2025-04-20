package com.kostify.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import android.widget.ImageView;

public class informasi_kost extends Fragment {

    public informasi_kost() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_informasi_kost, container, false);

        // Set listener untuk tombol back
        view.findViewById(R.id.ic_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Kembali ke Fragment Penyewa
                backToPenyewaFragment();
            }
        });

        // Menyembunyikan bottom navigation ketika fragment ini ditampilkan
        if (getActivity() != null) {
            menu_utama_navigasi activity = (menu_utama_navigasi) getActivity();
            if (activity != null) {
                activity.binding.bottomNav.setVisibility(View.GONE); // Sembunyikan bottom navigation
            }
        }

        return view;
    }

    // Metode untuk kembali ke Fragment Penyewa
    private void backToPenyewaFragment() {
        // Membuat instance fragment Penyewa
        Penyewa penyewaFragment = new Penyewa();

        // Mulai transaksi fragment
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.frameLayout, penyewaFragment)
                .addToBackStack(null)
                .commit();

        // Tampilkan kembali bottom navigation saat kembali ke Penyewa
        if (getActivity() != null) {
            menu_utama_navigasi activity = (menu_utama_navigasi) getActivity();
            if (activity != null) {
                activity.binding.bottomNav.setVisibility(View.VISIBLE); // Tampilkan kembali bottom navigation
            }
        }
    }
}
