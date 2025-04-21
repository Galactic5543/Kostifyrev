package com.kostify.app;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.kostify.app.databinding.ActivityNavigasiPengajuanBinding;

public class navigasi_pengajuan extends AppCompatActivity {

    ActivityNavigasiPengajuanBinding binding;
    TextView textMenuInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNavigasiPengajuanBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inisialisasi TextView untuk judul
        textMenuInfo = findViewById(R.id.textmenuinfo);  // Ganti ID sesuai yang baru

        // Set default fragment
        replaceFragment(new pengajuan_kerusakan(), false);
        updateinfomenu("Pengajuan Kerusakan");

        // Pantau perubahan di backstack (optional)
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frameLayout);
            updateBottomNavVisibility(currentFragment);
        });

        // Bottom Navigation
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.kerusakan) {
                replaceFragment(new pengajuan_kerusakan(), false);
                updateinfomenu("Pengajuan Kerusakan");
                return true;
            } else if (itemId == R.id.sewa) {
                replaceFragment(new pengajuan_sewa(), false);
                updateinfomenu("Pengajuan Sewa");
                return true;
            }

            return false;
        });

        // Set listener untuk tombol back (ic_back)
        // Set listener untuk tombol back (ic_back)
        binding.icBack.setOnClickListener(v -> {
            // Arahkan langsung ke fragment Penyewa
            replaceFragment(new Penyewa(), false);
            updateinfomenu("Penyewa");
        });

    }

    // Fungsi replace fragment yang fleksibel (dengan/ tanpa addToBackStack)
    public void replaceFragment(Fragment fragment, boolean addToBackStack) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Ganti fragment di FrameLayout
        fragmentTransaction.replace(R.id.frameLayout, fragment);

        if (addToBackStack) {
            fragmentTransaction.addToBackStack(null);
        }

        fragmentTransaction.commit();

        // Atur visibilitas Bottom Navigation
        updateBottomNavVisibility(fragment);
    }

    private void updateBottomNavVisibility(Fragment fragment) {
        // Menyembunyikan Bottom Navigation jika fragment tertentu
        if (fragment instanceof pengajuan_kerusakan ||
                fragment instanceof pengajuan_sewa) {
            binding.bottomNav.setVisibility(View.VISIBLE);  // Menyembunyikan bottom navigation
        } else {
            // Tampilkan bottom navigation pada fragment lain
            binding.bottomNav.setVisibility(View.GONE);  // Menampilkan bottom navigation
        }
    }

    // Fungsi untuk mengupdate teks di TextView berdasarkan fragment aktif
    private void updateinfomenu(String text) {
        textMenuInfo.setText(text);  // Update teks di TextView sesuai dengan fragment aktif
    }
}
