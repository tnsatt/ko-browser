/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 Copyright by MonnyLab */

package com.xlab.vbrowser.fragment;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.xlab.vbrowser.locale.LocaleAwareFragment;
import com.xlab.vbrowser.session.Session;
import com.xlab.vbrowser.locale.LocaleManager;
import com.xlab.vbrowser.web.IWebView;

import java.util.Locale;

/**
 * Base implementation for fragments that use an IWebView instance. Based on Android's WebViewFragment.
 */
public abstract class WebFragment extends LocaleAwareFragment {
    private IWebView webView;
    private boolean isWebViewAvailable;

    /**
     * Inflate a layout for this fragment. The layout needs to contain a view implementing IWebView
     * with the id set to "webview".
     */
    @NonNull
    public abstract View inflateLayout(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState);

    public abstract IWebView.Callback createCallback();

    public abstract Session getSession();

    /**
     * Get the initial URL to load after the view has been created.
     */
    @Nullable
    public abstract String getInitialUrl();

    /**
     * Adds ability to add methods to onCreateView without override because onCreateView is final.
     */
    public abstract void onCreateViewCalled();

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflateLayout(inflater, container, savedInstanceState);

        webView = view.findViewById(com.xlab.vbrowser.R.id.webview);

        if (webView == null) {
            return view;
        }

        isWebViewAvailable = true;
        webView.setCallback(createCallback());

        final Session session = getSession();

        if (session != null) {
            webView.setBlockingEnabled(session.isBlockingEnabled());
        }

        if (session == null || !session.hasWebViewState()) {
            final String url = getInitialUrl();
            if (!TextUtils.isEmpty(url)) {
                webView.loadUrl(url);
            }
        } else {
            webView.restoreWebViewState(session);
        }

        onCreateViewCalled();
        return view;
    }

    @Override
    public void applyLocale() {
        Context context = getContext();
        final LocaleManager localeManager = LocaleManager.getInstance();
        if (!localeManager.isMirroringSystemLocale(context)) {
            final Locale currentLocale = localeManager.getCurrentLocale(context);
            Locale.setDefault(currentLocale);
            final Resources resources = context.getResources();
            final Configuration config = resources.getConfiguration();
            config.setLocale(currentLocale);
            context.getResources().updateConfiguration(config, null);
        }
        // We create and destroy a new WebView here to force the internal state of WebView to know
        // about the new language. See issue #666.
        final WebView unneeded = new WebView(getContext());
        unneeded.destroy();
    }

    @Override
    public void onPause() {
        if (webView != null) {
            final Session session = getSession();
            if (session != null) {
                webView.saveWebViewState(session);
            }

            webView.onPause();
        }

        super.onPause();
    }

    @Override
    public void onResume() {
        if (webView != null) {
            webView.onResume();
        }

        super.onResume();
    }

    @Override
    public void onDestroy() {
        if (webView != null) {
            webView.setCallback(null);
            webView.destroy();
            webView = null;
        }

        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        isWebViewAvailable = false;

        super.onDestroyView();
    }

    @Nullable
    public IWebView getWebView() {
        return isWebViewAvailable ? webView : null;
    }
}
