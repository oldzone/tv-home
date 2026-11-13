package com.oldzone.tvhome;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Arka planda ntfy.sh kanalını dinler. iPhone'dan gelen her komutu
 * ana ekrana iletir; ekran kapalıysa önce onu açar.
 */
public class ListenService extends Service {
    private static final String CHANNEL = "listen";
    static volatile boolean connected = false;

    private volatile boolean running = true;
    private Thread worker;
    private final Handler main = new Handler(Looper.getMainLooper());

    static void start(Context c) {
        Intent i = new Intent(c, ListenService.class);
        try {
            if (Build.VERSION.SDK_INT >= 26) c.startForegroundService(i);
            else c.startService(i);
        } catch (Exception ignored) { }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Notification.Builder nb;
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nm.createNotificationChannel(new NotificationChannel(CHANNEL, "iPhone bağlantısı", NotificationManager.IMPORTANCE_MIN));
            nb = new Notification.Builder(this, CHANNEL);
        } else {
            nb = new Notification.Builder(this);
        }
        Notification n = nb.setContentTitle("TV Home")
                .setContentText("iPhone'dan gelenleri dinliyor")
                .setSmallIcon(R.drawable.icon)
                .setOngoing(true)
                .build();
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(1, n);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (worker == null || !worker.isAlive()) {
            worker = new Thread(this::loop, "ntfy");
            worker.start();
        }
        return START_STICKY;
    }

    private void loop() {
        long backoff = 2000;
        while (running) {
            HttpURLConnection c = null;
            try {
                URL u = new URL("https://ntfy.sh/" + Prefs.topic(this) + "/json");
                c = (HttpURLConnection) u.openConnection();
                c.setConnectTimeout(15000);
                c.setReadTimeout(120000); // ntfy her ~45 sn'de keepalive gönderir
                BufferedReader r = new BufferedReader(new InputStreamReader(c.getInputStream(), "UTF-8"));
                connected = true;
                backoff = 2000;
                String line;
                while (running && (line = r.readLine()) != null) {
                    if (line.isEmpty()) continue;
                    JSONObject o = new JSONObject(line);
                    if ("message".equals(o.optString("event"))) {
                        final String msg = o.optString("message", "");
                        if (!msg.isEmpty()) main.post(() -> handle(msg));
                    }
                }
            } catch (Exception ignored) {
            } finally {
                connected = false;
                if (c != null) c.disconnect();
            }
            try { Thread.sleep(backoff); } catch (InterruptedException e) { return; }
            backoff = Math.min(backoff * 2, 30000);
        }
    }

    private void handle(String msg) {
        String type = "open";
        try { type = new JSONObject(msg).optString("t", "open"); } catch (Exception ignored) { }

        MainActivity live = MainActivity.live;
        if (live != null && live.isVisible()) {
            live.deliver(msg);
            return;
        }
        // "log" sadece geçmişe yazmak içindir; ekran kapalıyken TV'yi açmaz
        if ("log".equals(type)) {
            if (live != null) live.deliver(msg);
            return;
        }
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        i.putExtra("msg", msg);
        try { startActivity(i); } catch (Exception ignored) { }
    }

    @Override
    public void onDestroy() {
        running = false;
        if (worker != null) worker.interrupt();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
