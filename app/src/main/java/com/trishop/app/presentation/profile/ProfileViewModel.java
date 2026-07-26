package com.trishop.app.presentation.profile;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.LiveDataReactiveStreams;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.trishop.app.core.UiState;
import com.trishop.app.domain.model.User;
import com.trishop.app.domain.repository.AuthRepository;
import com.trishop.app.domain.repository.NewsletterResult;
import com.trishop.app.domain.usecase.DeleteAvatarUseCase;
import com.trishop.app.domain.usecase.GetProfileUseCase;
import com.trishop.app.domain.usecase.LogoutUseCase;
import com.trishop.app.domain.usecase.SubscribeNewsletterUseCase;
import com.trishop.app.domain.usecase.UploadAvatarUseCase;
import com.trishop.app.presentation.common.SingleLiveEvent;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

@HiltViewModel
public class ProfileViewModel extends ViewModel {

    public enum NewsletterState {
        IDLE, SENDING, SUCCESS, ALREADY_SUBSCRIBED, FAILURE
    }

    /** One-shot feedback for avatar upload/remove. */
    public enum AvatarEvent {
        UPLOADED, REMOVED, FAILED, TOO_LARGE
    }

    private final GetProfileUseCase getProfile;
    private final LogoutUseCase logoutUseCase;
    private final SubscribeNewsletterUseCase subscribeNewsletter;
    private final UploadAvatarUseCase uploadAvatarUseCase;
    private final DeleteAvatarUseCase deleteAvatarUseCase;

    private final MutableLiveData<NewsletterState> newsletterState =
            new MutableLiveData<>(NewsletterState.IDLE);
    private final MutableLiveData<UiState<User>> profile =
            new MutableLiveData<>(UiState.loading());
    private final MutableLiveData<Boolean> avatarUploading = new MutableLiveData<>(false);
    private final SingleLiveEvent<AvatarEvent> avatarEvents = new SingleLiveEvent<>();
    /** Avatar image URL served by the API, or null when none is set. */
    private final MediatorLiveData<String> avatarUrl = new MediatorLiveData<>();
    /** The Profile tab swaps to the auth flow when signed out, like iOS. */
    private final LiveData<Boolean> isLoggedIn;

    private final CompositeDisposable disposables = new CompositeDisposable();

    @Inject
    public ProfileViewModel(GetProfileUseCase getProfile,
                            LogoutUseCase logoutUseCase,
                            SubscribeNewsletterUseCase subscribeNewsletter,
                            UploadAvatarUseCase uploadAvatarUseCase,
                            DeleteAvatarUseCase deleteAvatarUseCase,
                            AuthRepository authRepository) {
        this.getProfile = getProfile;
        this.logoutUseCase = logoutUseCase;
        this.subscribeNewsletter = subscribeNewsletter;
        this.uploadAvatarUseCase = uploadAvatarUseCase;
        this.deleteAvatarUseCase = deleteAvatarUseCase;

        avatarUrl.addSource(profile, state -> avatarUrl.setValue(
                state instanceof UiState.Success
                        ? ((UiState.Success<User>) state).getData().getAvatarUrl()
                        : null));

        isLoggedIn = LiveDataReactiveStreams.fromPublisher(authRepository.isLoggedIn());
    }

    @NonNull
    public LiveData<NewsletterState> getNewsletterState() {
        return newsletterState;
    }

    @NonNull
    public LiveData<UiState<User>> getProfile() {
        return profile;
    }

    @NonNull
    public LiveData<Boolean> getAvatarUploading() {
        return avatarUploading;
    }

    @NonNull
    public LiveData<AvatarEvent> getAvatarEvents() {
        return avatarEvents;
    }

    @NonNull
    public LiveData<String> getAvatarUrl() {
        return avatarUrl;
    }

    @NonNull
    public LiveData<Boolean> getIsLoggedIn() {
        return isLoggedIn;
    }

    public void load() {
        profile.setValue(UiState.loading());
        disposables.add(getProfile.execute()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(user -> profile.setValue(UiState.success(user)),
                        error -> {
                            String message = error.getLocalizedMessage();
                            profile.setValue(UiState.error(message != null ? message : "Error"));
                        }));
    }

    public void uploadAvatar(@NonNull byte[] bytes, @NonNull String mimeType) {
        avatarUploading.setValue(true);
        disposables.add(uploadAvatarUseCase.execute(bytes, mimeType)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(user -> {
                    profile.setValue(UiState.success(user));
                    avatarEvents.setValue(AvatarEvent.UPLOADED);
                    avatarUploading.setValue(false);
                }, error -> {
                    avatarEvents.setValue(AvatarEvent.FAILED);
                    avatarUploading.setValue(false);
                }));
    }

    public void deleteAvatar() {
        disposables.add(deleteAvatarUseCase.execute()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(user -> {
                    profile.setValue(UiState.success(user));
                    avatarEvents.setValue(AvatarEvent.REMOVED);
                }, error -> avatarEvents.setValue(AvatarEvent.FAILED)));
    }

    public void onAvatarTooLarge() {
        avatarEvents.setValue(AvatarEvent.TOO_LARGE);
    }

    public void subscribe(@NonNull String email) {
        if (email.trim().isEmpty()) return;
        newsletterState.setValue(NewsletterState.SENDING);
        disposables.add(subscribeNewsletter.execute(email.trim())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> newsletterState.setValue(
                                result == NewsletterResult.ALREADY_SUBSCRIBED
                                        ? NewsletterState.ALREADY_SUBSCRIBED
                                        : NewsletterState.SUCCESS),
                        error -> newsletterState.setValue(NewsletterState.FAILURE)));
    }

    public void logout() {
        disposables.add(logoutUseCase.execute().subscribe(() -> {
        }, error -> {
        }));
    }

    @Override
    protected void onCleared() {
        disposables.clear();
        super.onCleared();
    }
}
