package com.kostify.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

public class Profil extends Fragment {

    private AdView mAdView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate layout fragment_profil.xml
        View view = inflater.inflate(R.layout.fragment_profil, container, false);

        // Inisialisasi dan load iklan
        mAdView = view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Mengakses RelativeLayout dengan ID kontainermnlogout
        RelativeLayout mnLogoutLayout = view.findViewById(R.id.kontainermnlogout);

        // Menambahkan click listener untuk RelativeLayout mnlogout
        mnLogoutLayout.setOnClickListener(v -> {
            // Hapus session
            SessionManager sessionManager = new SessionManager(requireContext());
            sessionManager.logoutUser();

            // Sign out Firebase
            FirebaseAuth.getInstance().signOut();

            // Sign out Google
            GoogleSignIn.getClient(
                    requireActivity(),
                    new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            ).signOut();

            // Pindah ke halaman login
            Intent intent = new Intent(requireActivity(), Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });
    }
}
