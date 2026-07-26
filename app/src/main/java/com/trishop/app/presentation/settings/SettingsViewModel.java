package com.trishop.app.presentation.settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.LiveDataReactiveStreams;
import androidx.lifecycle.ViewModel;

import com.trishop.app.data.local.prefs.Appearance;
import com.trishop.app.data.local.prefs.SettingsStore;
import com.trishop.app.domain.usecase.ClearRecentlyViewedUseCase;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

@HiltViewModel
public class SettingsViewModel extends ViewModel {

    private final SettingsStore settings;
    private final ClearRecentlyViewedUseCase clearRecentlyViewed;

    private final LiveData<Appearance> appearance;
    private final LiveData<Boolean> notificationsEnabled;

    private final CompositeDisposable disposables = new CompositeDisposable();

    @Inject
    public SettingsViewModel(SettingsStore settings,
                             ClearRecentlyViewedUseCase clearRecentlyViewed) {
        this.settings = settings;
        this.clearRecentlyViewed = clearRecentlyViewed;
        appearance = LiveDataReactiveStreams.fromPublisher(settings.appearance());
        notificationsEnabled = LiveDataReactiveStreams.fromPublisher(settings.notificationsEnabled());
    }

    @NonNull
    public LiveData<Appearance> getAppearance() {
        return appearance;
    }

    @NonNull
    public LiveData<Boolean> getNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setAppearance(@NonNull Appearance value) {
        disposables.add(settings.setAppearance(value)
                .subscribe(() -> {
                }, error -> {
                }));
        AppCompatDelegate.setDefaultNightMode(toNightMode(value));
    }

    public void setNotifications(boolean enabled) {
        disposables.add(settings.setNotificationsEnabled(enabled)
                .subscribe(() -> {
                }, error -> {
                }));
    }

    public void clearRecent() {
        disposables.add(clearRecentlyViewed.execute().subscribe(() -> {
        }, error -> {
        }));
    }

    public static int toNightMode(@NonNull Appearance appearance) {
        switch (appearance) {
            case LIGHT:
                return AppCompatDelegate.MODE_NIGHT_NO;
            case DARK:
                return AppCompatDelegate.MODE_NIGHT_YES;
            case SYSTEM:
            default:
                return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
    }

    @Override
    protected void onCleared() {
        disposables.clear();
        super.onCleared();
    }
}
