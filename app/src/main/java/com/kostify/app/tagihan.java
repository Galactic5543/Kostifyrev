package com.kostify.app;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class tagihan extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tagihan, container, false);

        view.findViewById(R.id.btnbayar).setOnClickListener(v -> openpembayaran());

        return view;
    }

    private void openpembayaran() {
        Fragment fragment = new Pembayaran();
        navigateToFragment(fragment);
    }

    private void navigateToFragment(Fragment fragment) {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frameLayout, fragment); // pastikan ID ini sesuai dengan container fragment kamu
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
