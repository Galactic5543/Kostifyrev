package com.kostify.app;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class pembayaran extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_pembayaran, container, false);

        // Set listener untuk tombol back
        view.findViewById(R.id.ic_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Kembali ke Fragment Penyewa
                backToPenyewaFragment();
            }
        });

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
    }
}