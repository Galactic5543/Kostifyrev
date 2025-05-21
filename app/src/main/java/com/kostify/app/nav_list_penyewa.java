package com.kostify.app;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.kostify.app.databinding.ActivityNavListPenyewaBinding;
import com.kostify.app.databinding.ActivityNavigasiUtamaBinding;

public class nav_list_penyewa extends AppCompatActivity {

    ActivityNavListPenyewaBinding binding;
    TextView textMenuInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNavListPenyewaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inisialisasi TextView untuk judul
        textMenuInfo = findViewById(R.id.textmenuinfo);

        // Ganti fragment pertama yang muncul
        replaceFragment(new list_penyewa(), false);
        updateinfomenu("List Penyewa");

        // Pantau perubahan di backstack (optional)
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frameLayout);
            updateUIVisibility(currentFragment);
        });

        // Bottom Navigation
        binding.topnav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.listpenyewa) {
                replaceFragment(new list_penyewa(), false);
                updateinfomenu("List Penyewa");
                return true;
            } else if (itemId == R.id.pending) {
                replaceFragment(new pending(), false);
                updateinfomenu("Pending");
                return true;
            }

            return false;
        });

        // Tombol kembali ke fragment pemilik
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
            if (fragment instanceof list_penyewa || fragment instanceof pending) {
                topMenu.setVisibility(View.VISIBLE);
            } else {
                topMenu.setVisibility(View.GONE);
            }
        }

        // Update bottom navigation visibility
        if (binding.topnav != null) {
            if (fragment instanceof list_penyewa || fragment instanceof pending) {
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