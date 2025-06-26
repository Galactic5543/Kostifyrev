package com.kostify.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class Profil extends Fragment {

    private AdView mAdView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profil, container, false);

        // Load iklan
        mAdView = view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RelativeLayout mnLogoutLayout = view.findViewById(R.id.kontainermnlogout);
        RelativeLayout akunLayout = view.findViewById(R.id.kontainermnakun);

        TextView textNamaAkun = view.findViewById(R.id.textnamaakun);
        TextView textNoWhatsapp = view.findViewById(R.id.nowhatsapp);

        // Ambil data user dari Firestore
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance().collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nama = documentSnapshot.getString("nama");
                        String telepon = documentSnapshot.getString("telepon");

                        textNamaAkun.setText(nama != null ? nama : "-");
                        textNoWhatsapp.setText(telepon != null ? telepon : "-");
                    } else {
                        Toast.makeText(requireContext(), "Data pengguna tidak ditemukan", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Gagal memuat data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        // Logout
        mnLogoutLayout.setOnClickListener(v -> {
            SessionManager sessionManager = new SessionManager(requireContext());
            sessionManager.logoutUser();

            FirebaseAuth.getInstance().signOut();

            GoogleSignIn.getClient(
                    requireActivity(),
                    new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            ).signOut();

            Intent intent = new Intent(requireActivity(), Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        // Klik akun → buka popup profil
        akunLayout.setOnClickListener(v -> {
            Fragment popupFragment = new window_data_profil();
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            transaction.add(R.id.frameLayout, popupFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });
    }
}
