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

        textMenuInfo = findViewById(R.id.textmenuinfo);

        // Cek data intent apakah ingin langsung buka pengajuan_sewa
        String defaultFragment = getIntent().getStringExtra("default_fragment");

        if ("sewa".equals(defaultFragment)) {
            replaceFragment(new pengajuan_sewa(), false);
            updateinfomenu("Sewa");
            binding.topnav.setSelectedItemId(R.id.sewa); // Tandai menu sewa
        } else {
            replaceFragment(new pengajuan_kerusakan(), false);
            updateinfomenu("Kerusakan");
            binding.topnav.setSelectedItemId(R.id.kerusakan);
        }

        // Listener untuk perubahan backstack
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frameLayout);
            updateUIVisibility(currentFragment);
        });

        // Bottom Navigation
        binding.topnav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.kerusakan) {
                replaceFragment(new pengajuan_kerusakan(), false);
                updateinfomenu("Kerusakan");
                return true;
            } else if (itemId == R.id.sewa) {
                replaceFragment(new pengajuan_sewa(), false);
                updateinfomenu("Sewa");
                return true;
            }

            return false;
        });

        // Tombol kembali
        binding.icBack.setOnClickListener(v -> {
            finish(); // Tutup activity ini
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
        updateUIVisibility(fragment);
    }

    private void updateUIVisibility(Fragment fragment) {
        View topMenu = findViewById(R.id.kontainermenutop);

        if (fragment instanceof pengajuan_kerusakan || fragment instanceof pengajuan_sewa) {
            topMenu.setVisibility(View.VISIBLE);
            binding.topnav.setVisibility(View.VISIBLE);
        } else {
            topMenu.setVisibility(View.GONE);
            binding.topnav.setVisibility(View.GONE);
        }
    }

    private void updateinfomenu(String text) {
        textMenuInfo.setText(text);
    }
}
