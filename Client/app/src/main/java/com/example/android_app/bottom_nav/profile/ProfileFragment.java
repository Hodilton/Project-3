package com.example.android_app.bottom_nav.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.android_app.LoginActivity;
import com.example.android_app.bottom_nav.BaseFragment;
import com.example.android_app.databinding.FragmentProfileBinding;
import com.example.android_app.models.User;

public class ProfileFragment extends BaseFragment<FragmentProfileBinding> {
    @Override
    protected FragmentProfileBinding inflateBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentProfileBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupListeners();
        observeData();
    }

    private void setupListeners() {
        binding.logoutButton.setOnClickListener(v -> logout());
    }

    private void observeData() {
        userViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                updateUserInfo(user);
            } else {
                redirectToLogin();
            }
        });
    }

    private void updateUserInfo(User user) {
        if (user != null) {
            binding.usernameText.setText(user.getUsername());
            binding.emailText.setText(user.getEmail());
        }
    }

    private void logout() {
        userViewModel.logout();
        redirectToLogin();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }
}