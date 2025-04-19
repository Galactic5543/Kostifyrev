package com.kostify.app;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.kostify.app.databinding.ActivityNavigationBinding;

public class Navigation extends AppCompatActivity {

    ActivityNavigationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNavigationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set default fragment
        replaceFragment(new Notifikasi());

        // Bottom Navigation Item Selected
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

    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }
}
