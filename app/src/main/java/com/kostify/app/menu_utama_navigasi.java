package com.kostify.app;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kostify.app.databinding.ActivityNavigasiUtamaBinding;

public class menu_utama_navigasi extends AppCompatActivity {

    ActivityNavigasiUtamaBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNavigasiUtamaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Ambil UID user yang sedang login
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance().collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String telepon = documentSnapshot.getString("telepon");

                        if (telepon == null || telepon.trim().isEmpty()) {
                            Toast.makeText(this, "Silakan lengkapi data terlebih dahulu", Toast.LENGTH_LONG).show();
                            replaceFragment(new Profil(), false);
                            binding.bottomNav.setVisibility(View.GONE);
                        } else {
                            // Jika telepon lengkap, tampilkan fragment default
                            String fragmentTujuan = getIntent().getStringExtra("fragment");
                            if ("penyewa".equals(fragmentTujuan)) {
                                replaceFragment(new Penyewa(), false);
                                binding.bottomNav.setSelectedItemId(R.id.penyewa);
                            } else {
                                replaceFragment(new Penyewa(), false);
                            }

                            // Aktifkan bottom nav listener
                            setupBottomNav();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal memuat data pengguna: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });

        // Pantau perubahan backstack
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frameLayout);
            updateBottomNavVisibility(currentFragment);
        });
    }

    // Fungsi setup listener Bottom Navigation
    private void setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.penyewa) {
                replaceFragment(new Penyewa(), false);
                return true;
            } else if (itemId == R.id.pemilik) {
                replaceFragment(new Pemilik(), false);
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
        if (fragment instanceof informasikostpemilik ||
                fragment instanceof Penyewa ||
                fragment instanceof Pemilik ||
                fragment instanceof Profil) {
            binding.bottomNav.setVisibility(View.VISIBLE);
        } else {
            binding.bottomNav.setVisibility(View.GONE);
        }
    }
}
