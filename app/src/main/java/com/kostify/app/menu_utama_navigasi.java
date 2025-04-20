package com.kostify.app;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.kostify.app.databinding.ActivityNavigasiUtamaBinding;

public class menu_utama_navigasi extends AppCompatActivity {

    ActivityNavigasiUtamaBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNavigasiUtamaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set default fragment
        replaceFragment(new Notifikasi());

        // Bottom menu_utama_navigasi Item Selected
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.penyewa) {
                replaceFragment(new Penyewa());
                return true;
            } else if (itemId == R.id.pemilik) {
                replaceFragment(new Pemilik());
                return true;
            } else if (itemId == R.id.notifikasi) {
                replaceFragment(new Notifikasi());
                return true;
            } else if (itemId == R.id.profil) {
                replaceFragment(new Profil());
                return true;
            }
            return false;
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);

        // Atur visibilitas bottom navigation berdasarkan fragment yang sedang aktif
        if (fragment instanceof informasi_kost) {
            binding.bottomNav.setVisibility(View.GONE); // Sembunyikan bottom navigation untuk InformasiKost
        } else {
            binding.bottomNav.setVisibility(View.VISIBLE); // Tampilkan bottom navigation untuk fragment lain
        }

        fragmentTransaction.commit();
    }

}
