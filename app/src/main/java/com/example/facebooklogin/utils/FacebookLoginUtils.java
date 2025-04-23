package com.example.facebooklogin.utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.UUID;

/**
 * Utility class that contains functionality to assist with Facebook login
 * This is a Java implementation inspired by the Python script
 */
public class FacebookLoginUtils {
    private static final String TAG = "FacebookLoginUtils";

    /**
     * Generate a device ID that will be consistent for the same email
     *
     * @param email User's email address
     * @return Device ID string
     */
    public static String generateDeviceId(String email) {
        try {
            // Create MD5 hash of email similar to Python implementation
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] emailBytes = email.getBytes();
            byte[] digest = md.digest(emailBytes);

            // Convert to hex string and use first 16 chars
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }

            return "device_" + sb.substring(0, 16);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "MD5 algorithm not available", e);
            // Fallback to UUID-based device ID
            return "device_" + UUID.randomUUID().toString().substring(0, 16);
        }
    }

    /**
     * Format cookies into JSON structure similar to the Python script
     *
     * @param cookies Map of cookies from the login response
     * @return JSON string representation of cookies
     */
    public static String formatCookiesJson(Map<String, String> cookies) {
        try {
            JSONArray cookiesArray = new JSONArray();
            SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            iso8601Format.setTimeZone(TimeZone.getTimeZone("UTC"));
            String currentTime = iso8601Format.format(new Date());

            for (Map.Entry<String, String> entry : cookies.entrySet()) {
                JSONObject cookie = new JSONObject();
                cookie.put("key", entry.getKey());
                cookie.put("value", entry.getValue());
                cookie.put("domain", "facebook.com");
                cookie.put("path", "/");
                cookie.put("hostOnly", false);
                cookie.put("creation", currentTime);
                cookie.put("lastAccessed", currentTime);

                cookiesArray.put(cookie);
            }

            return cookiesArray.toString(2); // Pretty print with indent of 2
        } catch (JSONException e) {
            Log.e(TAG, "Error formatting cookies to JSON", e);
            return "[]"; // Return empty array on error
        }
    }

    /**
     * Save cookies as a string in the 'Cookie Appstate (String)' format
     *
     * @param cookies Map of cookies from login response
     * @return String representation of cookies
     */
    public static String formatCookiesString(Map<String, String> cookies) {
        StringBuilder cookieString = new StringBuilder();
        boolean first = true;

        for (Map.Entry<String, String> entry : cookies.entrySet()) {
            if (!first) {
                cookieString.append("; ");
            }
            cookieString.append(entry.getKey()).append("=").append(entry.getValue());
            first = false;
        }

        return cookieString.toString();
    }

    /**
     * Generate Facebook authentication headers similar to the Python implementation
     *
     * @param userAgent User agent string
     * @return Map of HTTP headers
     */
    public static Map<String, String> generateFacebookHeaders(String userAgent) {
        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", userAgent);

        // Common browser-like headers
        headers.put("Accept-Language", "en-US,en;q=0.9");
        headers.put("Accept-Encoding", "gzip, deflate, br");
        headers.put("Connection", "keep-alive");

        // For mobile browsers
        if (userAgent.contains("Mobile") || userAgent.contains("Android") || userAgent.contains("iPhone")) {
            headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");

            // Add mobile-specific Facebook headers
            headers.put("X-FB-Connection-Type", getRandomConnectionType(true));
            headers.put("X-FB-Connection-Quality", getRandomConnectionQuality());
            headers.put("X-FB-Connection-Bandwidth", String.valueOf(getRandomBandwidth()));
            headers.put("X-FB-Friendly-Name", getRandomFriendlyName());
            headers.put("X-FB-Device-Group", String.valueOf(1000 + new Random().nextInt(9000)));
        } else {
            // Desktop browser headers
            headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8");
            headers.put("Upgrade-Insecure-Requests", "1");
            headers.put("Sec-Fetch-Dest", "document");
            headers.put("Sec-Fetch-Mode", "navigate");
            headers.put("Sec-Fetch-Site", "none");
            headers.put("Sec-Fetch-User", "?1");
        }

        return headers;
    }

    private static String getRandomConnectionType(boolean isMobile) {
        if (isMobile) {
            String[] types = {"WIFI", "5G", "4G", "LTE", "3G"};
            return types[new Random().nextInt(types.length)];
        } else {
            String[] types = {"WIFI", "ETHERNET"};
            return types[new Random().nextInt(types.length)];
        }
    }

    private static String getRandomConnectionQuality() {
        String[] qualities = {"EXCELLENT", "GOOD", "MODERATE"};
        int[] weights = {70, 25, 5};

        return weightedRandomChoice(qualities, weights);
    }

    private static int getRandomBandwidth() {
        // Values approximated from the Python implementation
        return 5000000 + new Random().nextInt(95000000);
    }

    private static String getRandomFriendlyName() {
        String[] names = {
                "FBAndroidAuthHandler",
                "Authentication.Login",
                "graphservice",
                "loginsdk",
                "m_login",
                "auth.login"
        };

        return names[new Random().nextInt(names.length)];
    }

    private static String weightedRandomChoice(String[] items, int[] weights) {
        int totalWeight = 0;
        for (int weight : weights) {
            totalWeight += weight;
        }

        int randomValue = new Random().nextInt(totalWeight);
        int weightSum = 0;

        for (int i = 0; i < items.length; i++) {
            weightSum += weights[i];
            if (randomValue < weightSum) {
                return items[i];
            }
        }

        // Default to the first item if something goes wrong
        return items[0];
    }
}