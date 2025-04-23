package com.example.facebooklogin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import com.example.facebooklogin.databinding.ActivityMainBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get preferences
        preferences = getSharedPreferences("facebook_login", MODE_PRIVATE);

        // Check if logged in
        if (!preferences.getBoolean("is_logged_in", false)) {
            // Not logged in, redirect to login
            redirectToLogin();
            return;
        }

        // Setup UI with user data
        setupWelcomeUI();

        // Setup logout button
        binding.logoutButton.setOnClickListener(v -> logout());
    }

    private void setupWelcomeUI() {
        // Get user details from preferences
        String userName = preferences.getString("user_name", "User");
        String email = preferences.getString("user_email", "");
        String sessionId = preferences.getString("session_id", "Unknown");
        long timestamp = preferences.getLong("login_timestamp", System.currentTimeMillis());

        // Create device name based on random selection (similar to Python code)
        String[] deviceNames = {"iPhone", "Google Pixel", "Samsung Galaxy", "Android Device"};
        String deviceName = deviceNames[new Random().nextInt(deviceNames.length)];

        // Format timestamp
        String formattedTimestamp = formatTimestamp(timestamp);

        // Update UI
        binding.welcomeMessage.setText("Welcome back, " + userName + "!");
        binding.sessionInfo.setText("Device: " + deviceName + "\nAccount: " + email);
        binding.timestampInfo.setText("Session ID: " + sessionId + "\nTimestamp: " + formattedTimestamp);
    }

    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    private void logout() {
        // Clear login status
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("is_logged_in", false);
        editor.apply();

        // Show toast
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Redirect to login screen
        redirectToLogin();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        // Clear back stack
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}