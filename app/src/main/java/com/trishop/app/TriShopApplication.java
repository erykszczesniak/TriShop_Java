package com.trishop.app;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.trishop.app.data.local.prefs.Appearance;
import com.trishop.app.data.local.prefs.SettingsStore;
import com.trishop.app.presentation.settings.SettingsViewModel;

import javax.inject.Inject;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class TriShopApplication extends Application {

    @Inject
    SettingsStore settingsStore;

    @Override
    public void onCreate() {
        super.onCreate();
        // Apply the persisted appearance before any activity inflates.
        Appearance appearance = settingsStore.currentAppearance();
        AppCompatDelegate.setDefaultNightMode(SettingsViewModel.toNightMode(appearance));
    }
}
