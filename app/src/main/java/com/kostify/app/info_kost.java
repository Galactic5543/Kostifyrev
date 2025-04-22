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

    ActivityNavigasiInfoKostBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNavigasiInfoKostBinding.inflate(getLayoutInflater());


    }
}
