/* -*- Mode: Java; c-basic-offset: 4; tab-width: 20; indent-tabs-mode: nil; -*-
 Copyright by MonnyLab */

package com.xlab.vbrowser.web;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;

import com.xlab.vbrowser.utils.Settings;
import com.xlab.vbrowser.R;
import com.xlab.vbrowser.webview.SystemWebView;
import com.xlab.vbrowser.webview.TrackingProtectionWebViewClient;

/**
 * WebViewProvider for creating a WebView based IWebView implementation.
 */
public class WebViewProvider {
    /**
     * Preload webview data. This allows the webview implementation to load resources and other data
     * it might need, in advance of intialising the view (at which time we are probably wanting to
     * show a website immediately).
     */
    public static void preload(final Context context) {
        TrackingProtectionWebViewClient.triggerPreload(context);
    }

    /**
     * A cleanup that should occur when a new browser session starts. This might be able to be merged with
     * {@link #performCleanup(Context)}, but I didn't want to do it now to avoid unforeseen side effects. We can do this
     * when we rethink our erase strategy: #1472.
     *
     * This function must be called before WebView.loadUrl to avoid erasing current session data.
     */
    public static void performNewBrowserSessionCleanup(final Context context) {
        // We run this on the main thread to guarantee it occurs before loadUrl so we don't erase current session data.
        final StrictMode.ThreadPolicy oldPolicy = StrictMode.allowThreadDiskWrites();

        // When left open on erase, some pages, like the google search results, will asynchronously write LocalStorage
        // files to disk after we erase them. To work-around this, we delete this data again when starting a new browser session.
        WebStorage.getInstance().deleteAllData();

        WebView newWebView = new WebView(context);
        SystemWebView.cleanup(newWebView);
        newWebView.destroy();

        StrictMode.setThreadPolicy(oldPolicy);
    }

    public static void signoutOfWebsitesIfNeeded(final Context context) {
        Settings settings = Settings.getInstance(context);

        if (settings.shouldLogoutWhenRemovingTask()) {
            signoutOfWebsites();
        }
    }

    public static void signoutOfWebsites() {
        CookieManager.getInstance().removeAllCookies(null);
        WebStorage.getInstance().deleteAllData();
    }

    public static View create(Context context, AttributeSet attrs) {
        final SystemWebView webkitView = new SystemWebView(context, attrs);
        final WebSettings settings = webkitView.getSettings();

        setupView(webkitView);
        configureDefaultSettings(context, settings);
        applyAppSettings(context, settings);

        return webkitView;
    }

    private static void setupView(WebView webView) {
        webView.setVerticalScrollBarEnabled(true);
        webView.setHorizontalScrollBarEnabled(true);
    }

    @SuppressLint("SetJavaScriptEnabled") // We explicitly want to enable JavaScript
    private static void configureDefaultSettings(Context context, WebSettings settings) {
        settings.setJavaScriptEnabled(true);

        // Needs to be enabled to display some HTML5 sites that use local storage
        settings.setDomStorageEnabled(true);

        // Enabling built in zooming shows the controls by default
        settings.setBuiltInZoomControls(true);

        // So we hide the controls after enabling zooming
        settings.setDisplayZoomControls(false);

        // To respect the html viewport:
        settings.setLoadWithOverviewMode(true);

        // Also increase text size to fill the viewport (this mirrors the behaviour of Firefox,
        // Chrome does this in the current Chrome Dev, but not Chrome release).
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);

        // Disable access to arbitrary local files by webpages - assets can still be loaded
        // via file:///android_asset/res, so at least error page images won't be blocked.
        settings.setAllowFileAccess(false);
        settings.setAllowFileAccessFromFileURLs(false);
        settings.setAllowUniversalAccessFromFileURLs(false);

        requestDesktopSite(context, settings);

        // Right now I do not know why we should allow loading content from a content provider
        settings.setAllowContentAccess(false);

        // The default for those settings should be "false" - But we want to be explicit.
//        settings.setAppCacheEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        // (thuan)We don't allow multiple tabs now
        settings.setSupportMultipleWindows(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);

        // We do not implement the callbacks - So let's disable it.
        settings.setGeolocationEnabled(false);

        // We do not want to save any data...
        settings.setSaveFormData(true);
        //noinspection deprecation - This method is deprecated but let's call it in case WebView implementations still obey it.
        settings.setSavePassword(true);
    }

    public static void applyAppSettings(Context context, WebSettings settings) {
        // We could consider calling setLoadsImagesAutomatically() here too (This will block images not loaded over the network too)
        settings.setBlockNetworkImage(Settings.getInstance(context).shouldBlockImages());
        settings.setLoadsImagesAutomatically(!Settings.getInstance(context).shouldBlockImages());
    }

    public static void enableLoadingImage(Context context, WebSettings settings, boolean isEnabled) {
        if (Settings.getInstance(context).shouldBlockImages()) {
            return;
        }

        if (settings.getBlockNetworkImage() == isEnabled) {
            settings.setBlockNetworkImage(!isEnabled);
        }

        if (!settings.getLoadsImagesAutomatically()) {
            settings.setLoadsImagesAutomatically(true);
        }
    }

    //(thuan): We don't use this way to generate UA due to some website as Youtube which checking UA to allow playing video or not
    /**
     * Build the browser specific portion of the UA String, based on the webview's existing UA String.
     */
    /*@VisibleForTesting static String getUABrowserString(final String existingUAString, final String focusToken) {
        // Use the default WebView agent string here for everything after the platform, but insert
        // Focus in front of Chrome.
        // E.g. a default webview UA string might be:
        // Mozilla/5.0 (Linux; Android 7.1.1; Pixel XL Build/NOF26V; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/56.0.2924.87 Mobile Safari/537.36
        // And we reuse everything from AppleWebKit onwards, except for adding Focus.
        int start = existingUAString.indexOf("AppleWebKit");
        if (start == -1) {
            // I don't know if any devices don't include AppleWebKit, but given the diversity of Android
            // devices we should have a fallback: we search for the end of the platform String, and
            // treat the next token as the start:
            start = existingUAString.indexOf(")") + 2;

            // If this was located at the very end, then there's nothing we can do, so let's just
            // return focus:
            if (start >= existingUAString.length()) {
                return focusToken;
            }
        }

        final String[] tokens = existingUAString.substring(start).split(" ");

        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].startsWith("Chrome")) {
                tokens[i] = focusToken + " " + tokens[i];

                return TextUtils.join(" ", tokens);
            }
        }

        // If we didn't find a chrome token, we just append the focus token at the end:
        return TextUtils.join(" ", tokens) + " " + focusToken;
    }

    @VisibleForTesting static String buildUserAgentString(final Context context, final WebSettings settings, final String appName) {
        final StringBuilder uaBuilder = new StringBuilder();

        uaBuilder.append("Mozilla/5.0");

        // WebView by default includes "; wv" as part of the platform string, but we're a full browser
        // so we shouldn't include that.
        // Most webview based browsers (and chrome), include the device name AND build ID, e.g.
        // "Pixel XL Build/NOF26V", that seems unnecessary (and not great from a privacy perspective),
        // so we skip that too.
        uaBuilder.append(" (Linux; Android ").append(Build.VERSION.RELEASE).append(") ");

        final String existingWebViewUA = settings.getUserAgentString();

        final String appVersion;
        try {
            appVersion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // This should be impossible - we should always be able to get information about ourselves:
            throw new IllegalStateException("Unable find package details for Focus", e);
        }

        final String focusToken = appName + "/" + appVersion;
        uaBuilder.append(getUABrowserString(existingWebViewUA, focusToken));

        return uaBuilder.toString();
    }*/

    static String buildMobileUserAgentString(final Context context, final WebSettings settings, final String appName) {
        // WebView by default includes "; wv" as part of the platform string, but we're a full browser
        // so we shouldn't include that.
        // Most webview based browsers (and chrome), include the device name AND build ID, e.g.
        // "Pixel XL Build/NOF26V", that seems unnecessary (and not great from a privacy perspective),
        // so we skip that too.

        String existingWebViewUA = settings.getUserAgentString();

        if (existingWebViewUA.contains("wv")){
            existingWebViewUA = existingWebViewUA.replace("wv","");
        }

        StringBuilder sb = new StringBuilder(existingWebViewUA);
        if(appName!=null && !appName.isEmpty()) sb.append(" " + appName);

        String newUA = sb.toString();

        //Save Mobile UA for later using
        Settings.getInstance(context).setMobileUserAgentString(newUA);

        return newUA;
    }

    static String buildDesktopUserAgentString(Context context) {
        return context.getResources().getString(R.string.useragent_desktopsite);
    }

    static String getMobileUserAgentString(final Context context,  final WebSettings webSettings, final String appName) {
        Settings settings = Settings.getInstance(context);
        String savedUA = settings.getMobileUserAgentString();

        if (savedUA == null || TextUtils.isEmpty(savedUA)) {
            savedUA = buildMobileUserAgentString(context, webSettings, appName);
        }

        return savedUA;
    }

    public static void requestDesktopSite(Context context, WebSettings webSettings) {
        String appName = context.getResources().getString(R.string.useragent_appname);

        String userAgent = Settings.getInstance(context).shouldRequestDesktopSite() ? buildDesktopUserAgentString(context) :
                getMobileUserAgentString(context, webSettings, appName);

        Log.d("requestDesktopSite", userAgent);
        webSettings.setUserAgentString(userAgent);
    }
}
