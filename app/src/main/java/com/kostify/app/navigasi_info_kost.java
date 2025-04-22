package com.kostify.app;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.kostify.app.databinding.ActivityNavigasiInfoKostBinding;

public class navigasi_info_kost extends AppCompatActivity {

    ActivityNavigasiInfoKostBinding binding;
    TextView textMenuInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNavigasiInfoKostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inisialisasi TextView untuk judul
        textMenuInfo = findViewById(R.id.textmenuinfo);

        // Ganti fragment pertama yang muncul
        replaceFragment(new info_kost(), false);
        updateinfomenu("Info Kost");

        // Pantau perubahan di backstack (optional)
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frameLayout);
            updateUIVisibility(currentFragment);
        });

        // Bottom Navigation
        binding.topnav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navinfokost) {
                replaceFragment(new info_kost(), false);
                updateinfomenu("Info Kost");
                return true;
            } else if (itemId == R.id.naveditkost) {
                replaceFragment(new edit_kost(), false);
                updateinfomenu("Edit Kost");
                return true;
            }

            return false;
        });

        // Tombol kembali ke fragment Penyewa
        binding.icBack.setOnClickListener(v -> {
            // Tutup activity saat ini untuk kembali ke menu utama
            finish();
        });

    }

    public void replaceFragment(Fragment fragment, boolean addToBackStack) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);

        if (addToBackStack) {
            fragmentTransaction.addToBackStack(null);
        }

        fragmentTransaction.commit();

        // Perbarui tampilan UI berdasarkan fragment aktif
        updateUIVisibility(fragment);
    }

    private void updateUIVisibility(Fragment fragment) {
        // Update top menu visibility
        View topMenu = findViewById(R.id.kontainermenutop);
        if (topMenu != null) {
            if (fragment instanceof info_kost || fragment instanceof edit_kost) {
                topMenu.setVisibility(View.VISIBLE);
            } else {
                topMenu.setVisibility(View.GONE);
            }
        }

        // Update bottom navigation visibility
        if (binding.topnav != null) {
            if (fragment instanceof info_kost || fragment instanceof edit_kost) {
                binding.topnav.setVisibility(View.VISIBLE);
            } else {
                binding.topnav.setVisibility(View.GONE);
            }
        }
    }

    private void updateinfomenu(String text) {
        textMenuInfo.setText(text);
    }
}
