package com.kostify.app;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class join_kost extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_join_kost, container, false);

        view.findViewById(R.id.btnkonfirmasi).setOnClickListener(v -> opennotifikasi());

        return view;
    }

    private void opennotifikasi() {
        Intent intent = new Intent(getActivity(), menu_utama_navigasi.class);
        startActivity(intent);
    }
}
