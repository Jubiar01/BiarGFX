package com.example.facebooklogin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.example.facebooklogin.databinding.ActivityLoginBinding;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private OkHttpClient client;
    private ExecutorService executorService;
    private SharedPreferences preferences;
    private boolean loginInProgress = false;

    // Default credentials from Python script
    private static final String DEFAULT_EMAIL = "entitled.guppy.ubrg@letterhaven.net";
    private static final String DEFAULT_PASSWORD = "pI@),/TX34/p";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize OkHttp client and executor service
        client = new OkHttpClient.Builder().build();
        executorService = Executors.newSingleThreadExecutor();
        preferences = getSharedPreferences("facebook_login", MODE_PRIVATE);

        // Pre-fill with default credentials
        binding.emailInput.setText(DEFAULT_EMAIL);
        binding.passwordInput.setText(DEFAULT_PASSWORD);

        // Set up login button
        binding.loginButton.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        // Validate inputs
        if (loginInProgress) {
            return;
        }

        String email = binding.emailInput.getText().toString().trim();
        String password = binding.passwordInput.getText().toString().trim();

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

        // Simulate login process similar to the Python script
        executorService.execute(() -> {
            // Simulate environment preparation
            simulateStepDelay("Performing network security check...", 1000);
            simulateStepDelay("Analyzing connection profile...", 900);
            simulateStepDelay("Setting up device fingerprint...", 800);
            simulateStepDelay("Configuring browser emulation...", 700);
            simulateStepDelay("Establishing secure channel...", 1200);

            // Attempt actual login with Facebook
            attemptFacebookLogin(email, password);
        });
    }

    private void attemptFacebookLogin(String email, String password) {
        // Mock login for demo purposes
        // In a real app, you'd implement the actual Facebook login logic here
        runOnUiThread(() -> simulateStepProgress("Connecting to Facebook servers..."));

        try {
            // Simulate network delay
            Thread.sleep(1500);

            runOnUiThread(() -> simulateStepProgress("Submitting login credentials..."));
            Thread.sleep(1200);

            runOnUiThread(() -> simulateStepProgress("Verifying account..."));
            Thread.sleep(1000);

            // Check if using default credentials or not
            if (DEFAULT_EMAIL.equals(email) && DEFAULT_PASSWORD.equals(password)) {
                // Simulate successful login with default credentials
                handleSuccessfulLogin(email);
            } else {
                // For demo - other credentials will fail
                runOnUiThread(() -> {
                    setLoginInProgress(false);
                    Snackbar.make(binding.getRoot(), "Invalid credentials", Snackbar.LENGTH_LONG).show();
                });
            }
        } catch (InterruptedException e) {
            runOnUiThread(() -> {
                setLoginInProgress(false);
                Toast.makeText(LoginActivity.this, "Login interrupted", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void handleSuccessfulLogin(String email) {
        // Generate session data
        String sessionId = generateSessionId();
        long timestamp = System.currentTimeMillis(); // Fix: Use long directly instead of String

        // Extract name from email (simulate getting user info)
        String userName = extractNameFromEmail(email);

        // Save session to preferences
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("user_email", email);
        editor.putString("user_name", userName);
        editor.putString("session_id", sessionId);
        editor.putLong("login_timestamp", timestamp); // Store the timestamp as Long
        editor.putBoolean("is_logged_in", true);
        editor.apply();

        // Simulate saving cookie data
        simulateStepDelay("Validating authentication tokens...", 500);
        simulateStepDelay("Formatting session cookies...", 400);
        simulateStepDelay("Encrypting sensitive data...", 600);
        simulateStepDelay("Writing to secure storage...", 300);
        simulateStepDelay("Verifying data integrity...", 500);

        // Launch main activity
        runOnUiThread(() -> {
            setLoginInProgress(false);
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private String extractNameFromEmail(String email) {
        // Simple function to extract a name from the email address
        if (email != null && email.contains("@")) {
            String[] parts = email.split("@")[0].split("\\.");
            StringBuilder nameBuilder = new StringBuilder();

            for (String part : parts) {
                if (!part.isEmpty()) {
                    // Capitalize first letter of each part
                    nameBuilder.append(part.substring(0, 1).toUpperCase());
                    if (part.length() > 1) {
                        nameBuilder.append(part.substring(1));
                    }
                    nameBuilder.append(" ");
                }
            }

            return nameBuilder.toString().trim();
        }
        return "User";
    }

    private String generateSessionId() {
        // Generate a random session ID
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private void simulateStepProgress(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void simulateStepDelay(String message, long delayMillis) {
        new Handler(Looper.getMainLooper()).post(() -> {
            simulateStepProgress(message);
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
            binding.loginProgress.setVisibility(View.VISIBLE);
        } else {
            binding.loginButton.setEnabled(true);
            binding.loginProgress.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}