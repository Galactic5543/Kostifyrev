package com.kostify.app;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class pengumuman extends Fragment {
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_pengumuman, container, false);

        // Set listener untuk tombol back
        view.findViewById(R.id.ic_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Kembali ke Fragment Penyewa
                backToPemilikFragment();
            }
        });

        return view;
    }

    // Metode untuk kembali ke Fragment Penyewa
    private void backToPemilikFragment() {
        // Membuat instance fragment Penyewa
        Pemilik pemilikFragment = new Pemilik();

        // Mulai transaksi fragment
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.frameLayout, pemilikFragment)
                .addToBackStack(null)
                .commit();
    }
}