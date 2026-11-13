package com.oldzone.tvhome;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Tam ekran Apple TV tarzı ana ekran. Arayüz GitHub Pages'teki tv.html'dir;
 * bu sınıf ona uygulama açma, intent başlatma ve öne gelme yeteneklerini verir.
 */
public class MainActivity extends Activity {
    static final String HOME_URL = "https://oldzone.github.io/tv-home/tv.html";
    static volatile MainActivity live;

    private WebView web;
    private boolean onHome = false;
    private boolean pageReady = false;
    private boolean visible = false;
    private final ArrayList<String> queue = new ArrayList<>();

    boolean isVisible() { return visible; }

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        live = this;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        web = new WebView(this);
        web.setBackgroundColor(Color.BLACK);
        web.setFocusable(true);
        web.setFocusableInTouchMode(true);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);

        web.addJavascriptInterface(new Bridge(), "fully");
        web.addJavascriptInterface(new Native(), "TVHomeNative");
        web.setWebChromeClient(new WebChromeClient());
        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest req) {
                return route(req.getUrl().toString());
            }

            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return route(url);
            }

            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                onHome = url != null && url.startsWith(HOME_URL);
                pageReady = false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                onHome = url != null && url.startsWith(HOME_URL);
                pageReady = onHome;
                if (pageReady) flush();
            }
        });
        setContentView(web);
        web.requestFocus(View.FOCUS_DOWN);

        ListenService.start(this);
        loadHome();
        askOverlayOnce();
        handleIntent(getIntent());
    }

    private void loadHome() {
        onHome = false;
        pageReady = false;
        web.loadUrl(HOME_URL + "?native=1#k=" + Prefs.code(this));
    }

    /** intent:, market: vb. adresleri Android'e yönlendir; http(s) WebView'de açılsın. */
    private boolean route(String url) {
        if (url == null) return false;
        if (url.startsWith("http://") || url.startsWith("https://")) return false;
        if (url.startsWith("intent:")) { startIntentUri(url); return true; }
        try {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        } catch (Exception ignored) { }
        return true;
    }

    private void startIntentUri(String uri) {
        Intent it = null;
        try {
            it = Intent.parseUri(uri, Intent.URI_INTENT_SCHEME);
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(it);
        } catch (Exception e) {
            String fb = it != null ? it.getStringExtra("browser_fallback_url") : null;
            if (fb != null) runOnUiThread(() -> web.loadUrl(fb));
            else toast("Uygulama açılamadı");
        }
    }

    private void startApp(String pkg) {
        PackageManager pm = getPackageManager();
        Intent i = pm.getLeanbackLaunchIntentForPackage(pkg);
        if (i == null) i = pm.getLaunchIntentForPackage(pkg);
        if (i == null) {
            toast("Uygulama yüklü değil");
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + pkg))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            } catch (ActivityNotFoundException ignored) { }
            return;
        }
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    /** iPhone'dan gelen bir komutu ekrana ilet. */
    void deliver(String msg) {
        runOnUiThread(() -> {
            String type = "open";
            try { type = new JSONObject(msg).optString("t", "open"); } catch (Exception ignored) { }
            queue.add(msg);
            if (!onHome && !"log".equals(type)) { loadHome(); return; }
            if (pageReady) flush();
        });
    }

    private void flush() {
        while (!queue.isEmpty()) {
            String m = queue.remove(0);
            web.evaluateJavascript("window.incoming&&incoming(" + JSONObject.quote(m) + ")", null);
        }
    }

    private void handleIntent(Intent i) {
        if (i == null) return;
        String msg = i.getStringExtra("msg");
        if (msg != null) {
            i.removeExtra("msg");
            deliver(msg);
        } else if (Intent.ACTION_MAIN.equals(i.getAction()) && !onHome && web != null && web.getUrl() != null) {
            loadHome(); // Home tuşu / uygulama ikonu: ana ekrana dön
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    protected void onResume() { super.onResume(); live = this; visible = true; }

    @Override
    protected void onPause() { visible = false; super.onPause(); }

    @Override
    protected void onDestroy() { if (live == this) live = null; super.onDestroy(); }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (e.getAction() == KeyEvent.ACTION_UP) {
                if (!onHome) {
                    if (web.canGoBack()) web.goBack(); else loadHome();
                } else {
                    web.evaluateJavascript("window.nativeBack&&nativeBack()", null);
                }
            }
            return true; // ana ekrandan çıkılmaz
        }
        return super.dispatchKeyEvent(e);
    }

    private void askOverlayOnce() {
        if (Build.VERSION.SDK_INT < 23 || Settings.canDrawOverlays(this)) return;
        if (Prefs.sp(this).getBoolean("askedOverlay", false)) return;
        Prefs.sp(this).edit().putBoolean("askedOverlay", true).apply();
        toast("iPhone'dan açılabilmesi için 'Diğer uygulamaların üzerinde göster' iznini ver");
        try {
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())));
        } catch (Exception ignored) { }
    }

    private void toast(String t) {
        runOnUiThread(() -> Toast.makeText(this, t, Toast.LENGTH_LONG).show());
    }

    /** tv.html'in kullandığı "fully" arayüzünün yerel karşılığı. */
    class Bridge {
        @JavascriptInterface public void bringToForeground() { }
        @JavascriptInterface public void startApplication(String pkg) { runOnUiThread(() -> startApp(pkg)); }
        @JavascriptInterface public void startIntent(String uri) { runOnUiThread(() -> startIntentUri(uri)); }
    }

    class Native {
        @JavascriptInterface public boolean isConnected() { return ListenService.connected; }
        @JavascriptInterface public String code() { return Prefs.code(MainActivity.this); }
        @JavascriptInterface public boolean canOpenFromBackground() {
            return Build.VERSION.SDK_INT < 23 || Settings.canDrawOverlays(MainActivity.this);
        }
    }
}
