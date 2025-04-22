package com.kostify.app;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kostify.app.databinding.ActivityNavigasiInfoKostBinding;
import com.kostify.app.databinding.ActivityNavigasiPengajuanBinding;
import com.kostify.app.databinding.FragmentInfoKostBinding; // Pastikan ini sesuai dengan nama layout XML Anda

public class info_kost extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate layout fragment_info_kost.xml
        return inflater.inflate(R.layout.fragment_info_kost, container, false);
    }
}

