package com.kostify.app;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;


public class sosial extends Fragment {

    public sosial() {
        // Required empty public constructor
    }

    private AdView mAdView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate layout
        View view = inflater.inflate(R.layout.fragment_sosial, container, false);


        // Tombol back
        ImageView back = view.findViewById(R.id.ic_back);
        back.setOnClickListener(v -> backToPenyewaFragment());

        return view;
    }


    private void backToPenyewaFragment() {
        // Ganti fragment ke Penyewa
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.frameLayout, new Penyewa())
                .addToBackStack(null)
                .commit();
    }
}
