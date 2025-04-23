package com.example.facebooklogin.utils;

import java.util.Random;

/**
 * Utility class for generating realistic user agents similar to the Python implementation
 */
public class UserAgentHelper {

    /**
     * Return a random mobile user agent string to avoid detection
     *
     * @return A random mobile user agent string
     */
    public static String getRandomUserAgent() {
        // List of realistic mobile user agents (updated for 2025)
        String[] mobileAgents = {
                // Android
                "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.6412.42 Mobile Safari/537.36",
                "Mozilla/5.0 (Linux; Android 14; SM-S928B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.6478.15 Mobile Safari/537.36",
                "Mozilla/5.0 (Linux; Android 13; SM-A546B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.6367.98 Mobile Safari/537.36",
                "Mozilla/5.0 (Linux; Android 14; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.6478.24 Mobile Safari/537.36",
                "Mozilla/5.0 (Linux; Android 14; SM-G991B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.6422.76 Mobile Safari/537.36",
                // iOS
                "Mozilla/5.0 (iPhone; CPU iPhone OS 18_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.0 Mobile/15E148 Safari/604.1",
                "Mozilla/5.0 (iPhone; CPU iPhone OS 17_5 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/126.0.6478.32 Mobile/15E148 Safari/604.1",
                "Mozilla/5.0 (iPhone; CPU iPhone OS 18_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.0 Mobile/15E148 Safari/604.1",
                "Mozilla/5.0 (iPhone; CPU iPhone OS 17_4_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) FxiOS/125.0 Mobile/15E148 Safari/605.1.15",
                "Mozilla/5.0 (iPhone; CPU iPhone OS 17_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1",
                // Desktop browsers that mobile sites may accept
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.3 Safari/605.1.15",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.6478.35 Safari/537.36",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.6478.35 Safari/537.36 Edg/126.0.2478.35",
                "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:125.0) Gecko/20100101 Firefox/125.0",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:125.0) Gecko/20100101 Firefox/125.0",
        };

        // The Facebook mobile site sometimes blocks older user agents, so let's prefer newer ones
        int[] weights = new int[mobileAgents.length];
        // First 10 are mobile agents (weight 2), last 5 are desktop (weight 1)
        for (int i = 0; i < mobileAgents.length; i++) {
            weights[i] = (i < 10) ? 2 : 1;
        }

        return weightedRandomChoice(mobileAgents, weights);
    }

    /**
     * Select a random item from an array based on weights
     */
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