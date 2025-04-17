package com.kostify.app;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class Penyewa extends Fragment {

    private ProgressBar progressBar;

    public Penyewa() {
        // Required empty public constructor
    }

    public static Penyewa newInstance(String param1, String param2) {
        Penyewa fragment = new Penyewa();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout untuk fragment
        View view = inflater.inflate(R.layout.fragment_penyewa, container, false);

        // Inisialisasi ProgressBar dan TextView
        ProgressBar progressBar = view.findViewById(R.id.progrespenyewa);
        TextView hariText = view.findViewById(R.id.hari);

        // Set progres awal
        int progress = 20; // misalnya 20 dari 100
        progressBar.setProgress(progress);

        // Tampilkan progres ke dalam TextView sebagai "20 Hari"
        hariText.setText(progress + " Hari");

        return view;
    }

}
