package com.kostify.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class Penyewa extends Fragment {

    public Penyewa() {
        // Required empty public constructor
    }
    private AdView mAdView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_penyewa, container, false);

        mAdView = view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // Navigasi ke Informasi Kost
        view.findViewById(R.id.mn_info_kost).setOnClickListener(v -> openInformasiKostFragment());

        // Navigasi ke Sosial
        view.findViewById(R.id.mn_sosial).setOnClickListener(v -> openSosialFragment());

        // Navigasi ke pengajuan_kerusakan
        view.findViewById(R.id.mn_pengajuan).setOnClickListener(v -> openPengajuanFragment());

        // Navigasi ke Info Sewa
        view.findViewById(R.id.mn_infosewa).setOnClickListener(v -> openInfoSewaFragment());

        view.findViewById(R.id.mnnotifikasi).setOnClickListener(v -> opennotifikasi());



        view.findViewById(R.id.mngabung).setOnClickListener(v -> openkodejoin());


        return view;
    }

    private void openInformasiKostFragment() {
        Fragment fragment = new informasikostpemilik();
        navigateToFragment(fragment);
    }

    private void openSosialFragment() {
        Fragment fragment = new sosial();
        navigateToFragment(fragment);
    }

    private void openPengajuanFragment() {
        Intent intent = new Intent(getActivity(), navigasi_pengajuan.class);
        startActivity(intent);
    }


    private void openInfoSewaFragment() {
        Fragment fragment = new info_sewa();
        navigateToFragment(fragment);
    }





    private void openkodejoin() {
        Fragment fragment = new join_kost();
        navigateToFragment(fragment);
    }

    private void openperpanjangsewaa() {
        Intent intent = new Intent(getActivity(), navigasi_pengajuan.class);
        intent.putExtra("default_fragment", "sewa");
        startActivity(intent);
    }



    private void opennotifikasi() {
        Fragment fragment = new Notifikasi();
        navigateToFragment(fragment);
    }

    // Helper method untuk navigasi fragment
    private void navigateToFragment(Fragment fragment) {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frameLayout, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
