package com.kostify.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class Pemilik extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public Pemilik() {
        // Required empty public constructor
    }

    public static Pemilik newInstance(String param1, String param2) {
        Pemilik fragment = new Pemilik();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pemilik, container, false);

        // Inisialisasi Spinner
        Spinner gantikost = view.findViewById(R.id.gantikost);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.list_kost,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gantikost.setAdapter(adapter);

        // Navigasi ke Informasi Kost
        view.findViewById(R.id.mntambahkost).setOnClickListener(v -> opentambahkost());

        // Navigasi ke Sosial
        view.findViewById(R.id.mninfokost).setOnClickListener(v -> openinfokost());

        // Navigasi ke pengajuan_kerusakan
        view.findViewById(R.id.mnpenyewa).setOnClickListener(v -> openpenyewa());

        // Navigasi ke Pembayaran
        view.findViewById(R.id.mnpengumuman).setOnClickListener(v -> openpengumuman());

        view.findViewById(R.id.btndetailpenyewa).setOnClickListener(v -> openlistpenyewa());

        return view;
    }

    private void opentambahkost() {
        Fragment fragment = new tambah_kost();
        navigateToFragment(fragment);
    }

    private void openinfokost() {
        Intent intent = new Intent(getActivity(), navigasi_info_kost.class);
        startActivity(intent);
    }

    private void openpenyewa() {
        Intent intent = new Intent(getActivity(), nav_list_penyewa.class);
        startActivity(intent);
    }


    private void openpengumuman() {
        Fragment fragment = new pengumuman();
        navigateToFragment(fragment);
    }

    private void openlistpenyewa() {
        Intent intent = new Intent(getActivity(), nav_list_penyewa.class);
        startActivity(intent);
    }


    // Helper method untuk navigasi fragment
    private void navigateToFragment(Fragment fragment) {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frameLayout, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
