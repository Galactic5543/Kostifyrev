package com.kostify.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class pengajuan_sewa extends Fragment {

    public pengajuan_sewa() {
        // Konstruktor kosong
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate layout satu kali saja
        View view = inflater.inflate(R.layout.fragment_pengajuan_sewa, container, false);

        // Inisialisasi Spinner
        Spinner gantikost = view.findViewById(R.id.dropdownperpanjangan);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.list_durasi_perpanjangan,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gantikost.setAdapter(adapter);

        return view; // Kembalikan view yang sudah dimodifikasi
    }
}
