package com.oldzone.tvhome;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

/** TV açılınca dinleyiciyi başlatır; izin varsa ana ekranı da açar. */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ListenService.start(context);
        boolean canOpen = Build.VERSION.SDK_INT < 23 || Settings.canDrawOverlays(context);
        if (canOpen) {
            Intent i = new Intent(context, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try { context.startActivity(i); } catch (Exception ignored) { }
        }
    }
}
