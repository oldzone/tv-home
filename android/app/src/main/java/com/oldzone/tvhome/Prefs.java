package com.oldzone.tvhome;

import android.content.Context;
import android.content.SharedPreferences;

import java.security.SecureRandom;

/** Eşleşme kodunu saklar. Kod, ntfy kanal adının bir parçasıdır: tvhome-KOD */
final class Prefs {
    private static final String ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";

    static SharedPreferences sp(Context c) {
        return c.getSharedPreferences("tvhome", Context.MODE_PRIVATE);
    }

    static String code(Context c) {
        String code = sp(c).getString("code", null);
        if (code == null) {
            SecureRandom r = new SecureRandom();
            StringBuilder b = new StringBuilder();
            for (int i = 0; i < 6; i++) b.append(ALPHABET.charAt(r.nextInt(ALPHABET.length())));
            code = b.toString();
            sp(c).edit().putString("code", code).apply();
        }
        return code;
    }

    static String topic(Context c) {
        return "tvhome-" + code(c);
    }
}
