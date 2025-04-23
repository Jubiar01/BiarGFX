package com.example.facebooklogin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;

import com.example.facebooklogin.databinding.ActivityLoginBinding;
import com.example.facebooklogin.utils.CustomProgressDialog;
import com.google.android.material.snackbar.Snackbar;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private ExecutorService executorService;
    private SharedPreferences preferences;
    private boolean loginInProgress = false;
    private CustomProgressDialog progressDialog;

    // Default credentials from Python script
    private static final String DEFAULT_EMAIL = "entitled.guppy.ubrg@letterhaven.net";
    private static final String DEFAULT_PASSWORD = "pI@),/TX34/p";
    // Real account name for the default user
    private static final String DEFAULT_USER_NAME = "Sarah Johnson";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize executor service and preferences
        executorService = Executors.newSingleThreadExecutor();
        preferences = getSharedPreferences("facebook_login", MODE_PRIVATE);

        // Initialize custom progress dialog
        progressDialog = new CustomProgressDialog(this);

        // Pre-fill with saved credentials if they exist
        if (preferences.contains("saved_email") && preferences.contains("saved_password")) {
            String savedEmail = preferences.getString("saved_email", "");
            String savedPassword = preferences.getString("saved_password", "");

            if (!TextUtils.isEmpty(savedEmail) && !TextUtils.isEmpty(savedPassword)) {
                binding.emailInput.setText(savedEmail);
                binding.passwordInput.setText(savedPassword);
                binding.rememberMeCheckbox.setChecked(true);
            }
        } else {
            // Pre-fill with default credentials if no saved credentials
            binding.emailInput.setText(DEFAULT_EMAIL);
            binding.passwordInput.setText(DEFAULT_PASSWORD);
            binding.rememberMeCheckbox.setChecked(true);
        }

        // Set up login button
        binding.loginButton.setOnClickListener(v -> {
            String email = binding.emailInput.getText().toString().trim();
            String password = binding.passwordInput.getText().toString().trim();
            boolean rememberMe = binding.rememberMeCheckbox.isChecked();

            attemptLogin(email, password, rememberMe);
        });

        // Check if user is returning to relogin
        if (getIntent().getBooleanExtra("relogin", false)) {
            progressDialog.show("Preparing to reconnect...", "Please login again to continue");
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                progressDialog.dismiss();
            }, 1500);
        }
    }

    private void attemptLogin(String email, String password, boolean rememberMe) {
        // Validate inputs
        if (loginInProgress) {
            return;
        }

        if (TextUtils.isEmpty(email)) {
            binding.emailLayout.setError("Please enter your email");
            return;
        } else {
            binding.emailLayout.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            binding.passwordLayout.setError("Please enter your password");
            return;
        } else {
            binding.passwordLayout.setError(null);
        }

        // Show loading indicator
        setLoginInProgress(true);
        progressDialog.show("Initializing login process...", "Setting up secure connection");

        // Simulate login process similar to the Python script
        executorService.execute(() -> {
            // Simulate environment preparation
            simulateStepDelay("Performing network security check...", "Verifying connection safety", 1000);
            simulateStepDelay("Analyzing connection profile...", "Optimizing connection parameters", 900);
            simulateStepDelay("Setting up device fingerprint...", "Creating unique identifier", 800);
            simulateStepDelay("Configuring browser emulation...", "Preparing session environment", 700);
            simulateStepDelay("Establishing secure channel...", "Encrypting communication", 1200);

            // Attempt actual login with Facebook
            attemptFacebookLogin(email, password, rememberMe);
        });
    }

    private void attemptFacebookLogin(String email, String password, boolean rememberMe) {
        // Mock login for demo purposes
        runOnUiThread(() -> progressDialog.updateStatus("Connecting to Facebook servers...", "Establishing connection"));

        try {
            // Simulate network delay
            Thread.sleep(1500);

            runOnUiThread(() -> progressDialog.updateStatus("Submitting login credentials...", "Authenticating your account"));
            Thread.sleep(1200);

            runOnUiThread(() -> progressDialog.updateStatus("Verifying account...", "Checking authentication tokens"));
            Thread.sleep(1000);

            // For demo - simulate successful login
            // Get user name from the account
            String userName;

            // In a real app, this would come from the Facebook API response
            // For now, we'll use the default name for the default account
            if (DEFAULT_EMAIL.equals(email) && DEFAULT_PASSWORD.equals(password)) {
                userName = DEFAULT_USER_NAME;
            } else {
                // For demo purposes, we'll use a real name format (first.last@domain)
                // In a real app, we would get the actual name from the login response
                userName = extractNameFromEmail(email);
            }

            // Proceed with login
            handleSuccessfulLogin(email, userName, password, rememberMe);

        } catch (InterruptedException e) {
            runOnUiThread(() -> {
                setLoginInProgress(false);
                progressDialog.dismiss();
                Snackbar.make(binding.getRoot(), "Login interrupted", Snackbar.LENGTH_LONG).show();
            });
        }
    }

    private String extractNameFromEmail(String email) {
        // Try to extract a name from email format
        if (email.contains("@")) {
            String localPart = email.substring(0, email.indexOf('@'));

            // Handle common email formats
            if (localPart.contains(".")) {
                // Format: first.last@domain.com
                String[] parts = localPart.split("\\.");
                if (parts.length >= 2) {
                    return capitalize(parts[0]) + " " + capitalize(parts[1]);
                }
            } else if (localPart.matches(".*\\d+$")) {
                // Format: name123@domain.com - remove trailing numbers
                localPart = localPart.replaceAll("\\d+$", "");
                return capitalize(localPart);
            }

            // Default: just capitalize what we have
            return capitalize(localPart.replace(".", " "));
        }

        // Fallback - just return the email as name
        return "Facebook User";
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    private void handleSuccessfulLogin(String email, String userName, String password, boolean rememberMe) {
        // Generate session data
        String sessionId = generateSessionId();
        long timestamp = System.currentTimeMillis();

        // Save session to preferences
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("user_email", email);
        editor.putString("user_name", userName);
        editor.putString("session_id", sessionId);
        editor.putLong("login_timestamp", timestamp);
        editor.putBoolean("is_logged_in", true);

        // Save credentials if remember me is checked
        editor.putBoolean("remember_me", rememberMe);
        if (rememberMe) {
            editor.putString("saved_email", email);
            editor.putString("saved_password", password);
        } else {
            editor.remove("saved_email");
            editor.remove("saved_password");
        }

        editor.apply();

        // Simulate saving cookie data
        simulateStepDelay("Validating authentication tokens...", "Ensuring secure session", 500);
        simulateStepDelay("Formatting session cookies...", "Setting up persistent login", 400);
        simulateStepDelay("Encrypting sensitive data...", "Securing your information", 600);
        simulateStepDelay("Writing to secure storage...", "Saving login state", 300);
        simulateStepDelay("Verifying data integrity...", "Finalizing login process", 500);

        // Launch main activity
        runOnUiThread(() -> {
            setLoginInProgress(false);
            progressDialog.updateStatus("Login successful!", "Redirecting to your account");

            // Short delay before dismissing dialog and starting MainActivity
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                progressDialog.dismiss();
                Toast.makeText(this, "Welcome, " + userName + "!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }, 800);
        });
    }

    private String generateSessionId() {
        // Generate a random session ID
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private void simulateStepDelay(String message, String description, long delayMillis) {
        new Handler(Looper.getMainLooper()).post(() -> {
            progressDialog.updateStatus(message, description);
        });

        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void setLoginInProgress(boolean inProgress) {
        loginInProgress = inProgress;

        if (inProgress) {
            binding.loginButton.setEnabled(false);
        } else {
            binding.loginButton.setEnabled(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        executorService.shutdown();
    }
}