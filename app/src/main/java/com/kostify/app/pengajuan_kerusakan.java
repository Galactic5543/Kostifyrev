package com.kostify.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;

public class pengajuan_kerusakan extends Fragment {

    public pengajuan_kerusakan() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate layout satu kali saja
        View view = inflater.inflate(R.layout.fragment_pengajuan_kerusakan, container, false);

        // Inisialisasi Spinner
        Spinner gantikost = view.findViewById(R.id.dropdownkerusakan);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.list_kerusakan,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gantikost.setAdapter(adapter);

        return view; // Kembalikan view yang sudah dimodifikasi
    }
}
