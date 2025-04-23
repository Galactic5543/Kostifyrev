package com.kostify.app;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.kostify.app.databinding.ActivityNavigasiPembayaranBinding;

public class navigasi_pembayaran extends AppCompatActivity {


    ActivityNavigasiPembayaranBinding binding;
    TextView textMenuInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNavigasiPembayaranBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        textMenuInfo = findViewById(R.id.textmenuinfo);

        String defaultFragment = getIntent().getStringExtra("default_fragment");

        if ("Riwayat".equals(defaultFragment)) {
            replaceFragment(new riwayat_pembayaran(), false);
            updateinfomenu("Riwayat Pembayaran");
            binding.topnav.setSelectedItemId(R.id.navriwayatpembayaran);
        } else {
            replaceFragment(new tagihan(), false); // default-nya jadi Tagihan
            updateinfomenu("Tagihan");
            binding.topnav.setSelectedItemId(R.id.navtagihan);
        }


        // Listener untuk perubahan backstack
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frameLayout);
            updateUIVisibility(currentFragment);
        });

        // Bottom Navigation
        binding.topnav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navtagihan) {
                replaceFragment(new tagihan(), false);
                updateinfomenu("Tagihan");
                return true;
            } else if (itemId == R.id.navriwayatpembayaran) {
                replaceFragment(new riwayat_pembayaran(), false);
                updateinfomenu("Riwayat Pembayaran");
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

        if (fragment instanceof tagihan || fragment instanceof riwayat_pembayaran) {
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