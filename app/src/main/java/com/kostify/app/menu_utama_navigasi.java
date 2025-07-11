package com.kostify.app;

import android.os.Bundle;
import android.view.View;
import android.content.Intent;

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

        // Ambil data fragment tujuan dari Intent
        String fragmentTujuan = getIntent().getStringExtra("fragment");
        if ("penyewa".equals(fragmentTujuan)) {
            replaceFragment(new Penyewa(), false);
            binding.bottomNav.setSelectedItemId(R.id.penyewa); // opsional untuk highlight menu
        } else {
            replaceFragment(new Notifikasi(), false); // default fragment
        }

        // Pantau perubahan di backstack
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frameLayout);
            updateBottomNavVisibility(currentFragment);
        });

        // Bottom Navigation
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.penyewa) {
                replaceFragment(new Penyewa(), false);
                return true;
            } else if (itemId == R.id.pemilik) {
                replaceFragment(new Pemilik(), false);
                return true;
            } else if (itemId == R.id.notifikasi) {
                replaceFragment(new Notifikasi(), false);
                return true;
            } else if (itemId == R.id.profil) {
                replaceFragment(new Profil(), false);
                return true;
            }

            return false;
        });
    }

    // Fungsi replace fragment yang fleksibel (dengan/ tanpa addToBackStack)
    public void replaceFragment(Fragment fragment, boolean addToBackStack) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.frameLayout, fragment);

        if (addToBackStack) {
            fragmentTransaction.addToBackStack(null);
        }

        fragmentTransaction.commit();

        // Atur visibilitas Bottom Navigation
        updateBottomNavVisibility(fragment);
    }

    // Fungsi pengecekan fragment mana yang perlu menyembunyikan bottom nav
    private void updateBottomNavVisibility(Fragment fragment) {
        if (fragment instanceof informasi_kost ||
                fragment instanceof Notifikasi ||
                fragment instanceof Penyewa ||
                fragment instanceof Pemilik ||
                fragment instanceof Profil) {
            binding.bottomNav.setVisibility(View.VISIBLE);
        } else {
            binding.bottomNav.setVisibility(View.GONE);
        }
    }
}
