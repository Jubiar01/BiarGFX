package com.example.facebooklogin.utils;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * Utility class to manage Facebook cookies
 * Provides functions to save cookies in both JSON and string formats
 */
public class CookieManager {
    private static final String TAG = "CookieManager";

    /**
     * Save cookies as a JSON file
     *
     * @param context Application context
     * @param cookies Map of cookies from login response
     * @param fileName Name of the file to save
     * @return boolean indicating success
     */
    public static boolean saveCookiesJson(Context context, Map<String, String> cookies, String fileName) {
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

            // Write to app's private storage
            File file = new File(context.getFilesDir(), fileName);
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(cookiesArray.toString(2));
            writer.close();

            Log.d(TAG, "Cookies saved to JSON: " + file.getAbsolutePath());
            return true;
        } catch (JSONException | IOException e) {
            Log.e(TAG, "Error saving cookies to JSON", e);
            return false;
        }
    }

    /**
     * Save cookies as a plain text string (cookie string format)
     *
     * @param context Application context
     * @param cookies Map of cookies from login response
     * @param fileName Name of the file to save
     * @return boolean indicating success
     */
    public static boolean saveCookiesString(Context context, Map<String, String> cookies, String fileName) {
        try {
            StringBuilder cookieString = new StringBuilder();
            boolean first = true;

            for (Map.Entry<String, String> entry : cookies.entrySet()) {
                if (!first) {
                    cookieString.append("; ");
                }
                cookieString.append(entry.getKey()).append("=").append(entry.getValue());
                first = false;
            }

            // Write to app's private storage
            File file = new File(context.getFilesDir(), fileName);
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(cookieString.toString());
            writer.close();

            Log.d(TAG, "Cookies saved as string: " + file.getAbsolutePath());
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error saving cookies as string", e);
            return false;
        }
    }

    /**
     * Get mock Facebook cookies for demonstration purposes
     * In a real implementation, these would come from the actual login response
     *
     * @return Map of cookie name-value pairs
     */
    public static Map<String, String> getMockFacebookCookies() {
        Map<String, String> cookies = new HashMap<>();

        // Mock cookies based on the sample in facebook_cookies.json
        cookies.put("c_user", "61574876085475");
        cookies.put("xs", "46:aHP0jjyxN-v8bQ:2:1745381554:-1:-1");
        cookies.put("fr", "0Ojb5wJiynri6hjnH.AWdvRAiA_kaFdFko940gv2mec5XBRCTvj5iul0ENeyVAojQoHY4.BoCGix..AAA.0.0.BoCGix.AWeMp6jAZx29zE9oaX6LAwbW96s");
        cookies.put("datr", "sWgIaIcdVtYtEi-aFN58CcPp");

        return cookies;
    }
}