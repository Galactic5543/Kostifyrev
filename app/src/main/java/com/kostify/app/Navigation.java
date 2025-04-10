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
        replacefragment(new Notifikasi());

        // Mengatur Bottom Navigation
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.penyewa) {
                replacefragment(new Penyewa());
                return true;

            }

            else if (itemId == R.id.pemilik) {
                replacefragment(new Pemilik());
                return true;

            }

            else if (itemId == R.id.notifikasi) {
                replacefragment(new Notifikasi());
                return true;

            }

            else if (itemId == R.id.profil) {
                replacefragment(new Profil());
                return true;

            }
            return false;
        });
    }

    private void replacefragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout,fragment);
        fragmentTransaction.commit();
    }

}
