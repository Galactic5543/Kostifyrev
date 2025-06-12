package com.kostify.app;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.kostify.app.databinding.ActivityNavListPenyewaBinding;

public class nav_list_penyewa extends AppCompatActivity {

    ActivityNavListPenyewaBinding binding;
    TextView textMenuInfo;
    String namaKost; // ⬅️ Variabel global untuk menyimpan nama kost

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNavListPenyewaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Terima data dari intent
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            namaKost = bundle.getString("nama_kost");
            Log.d("nav_list_penyewa", "Nama kost diterima: " + namaKost);
        }

        textMenuInfo = findViewById(R.id.textmenuinfo);
        replaceFragment(new list_penyewa(), false);
        updateinfomenu("List Penyewa");

        // Atur navigasi bawah
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frameLayout);
            updateUIVisibility(currentFragment);
        });

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

        binding.icBack.setOnClickListener(v -> finish());
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
        if (topMenu != null) {
            if (fragment instanceof list_penyewa || fragment instanceof pending) {
                topMenu.setVisibility(View.VISIBLE);
            } else {
                topMenu.setVisibility(View.GONE);
            }
        }

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
