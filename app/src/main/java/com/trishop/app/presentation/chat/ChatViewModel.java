package com.trishop.app.presentation.chat;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.trishop.app.data.local.prefs.ChatSessionStore;
import com.trishop.app.data.remote.chat.ChatStreamClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;

@HiltViewModel
public class ChatViewModel extends ViewModel {

    /** Error string resource selector for the UI. */
    public enum ChatError { RATE_LIMITED, UNAVAILABLE, NETWORK }

    private final ChatStreamClient chatClient;
    private final ChatSessionStore sessionStore;

    private final MutableLiveData<List<ChatMessage>> messages =
            new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Boolean> isStreaming = new MutableLiveData<>(false);

    private Disposable streamDisposable;

    @Inject
    public ChatViewModel(ChatStreamClient chatClient, ChatSessionStore sessionStore) {
        this.chatClient = chatClient;
        this.sessionStore = sessionStore;
    }

    @NonNull
    public LiveData<List<ChatMessage>> getMessages() {
        return messages;
    }

    @NonNull
    public LiveData<Boolean> getIsStreaming() {
        return isStreaming;
    }

    public void ensureGreeting(@NonNull String greeting) {
        List<ChatMessage> current = messages.getValue();
        if (current == null || current.isEmpty()) {
            messages.setValue(Collections.singletonList(
                    new ChatMessage(ChatMessage.Role.ASSISTANT, greeting)));
        }
    }

    public void send(@NonNull String text, @NonNull Function<ChatError, String> errorText) {
        String trimmed = text.trim();
        if (trimmed.isEmpty() || Boolean.TRUE.equals(isStreaming.getValue())) return;

        List<ChatMessage> current = messages.getValue();
        List<ChatMessage> updated =
                new ArrayList<>(current != null ? current : Collections.emptyList());
        updated.add(new ChatMessage(ChatMessage.Role.USER, trimmed));
        updated.add(new ChatMessage(ChatMessage.Role.ASSISTANT, ""));
        messages.setValue(updated);
        isStreaming.setValue(true);

        streamDisposable = sessionStore.sessionId()
                .flatMapPublisher(sessionId -> chatClient.stream(trimmed, sessionId))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(event -> {
                    if (event instanceof ChatStreamEvent.Token) {
                        String token = ((ChatStreamEvent.Token) event).text;
                        replaceLastAssistant(message ->
                                message.withText(message.getText() + token));
                    } else if (event instanceof ChatStreamEvent.Products) {
                        List<ChatProduct> streamed = ((ChatStreamEvent.Products) event).products;
                        replaceLastAssistant(message -> {
                            List<ChatProduct> merged = new ArrayList<>(message.getProducts());
                            merged.addAll(streamed);
                            return message.withProducts(merged);
                        });
                    } else if (event instanceof ChatStreamEvent.RateLimited) {
                        replaceLastAssistant(message ->
                                message.asError(errorText.apply(ChatError.RATE_LIMITED)));
                    } else if (event instanceof ChatStreamEvent.Error) {
                        replaceLastAssistant(message ->
                                message.asError(errorText.apply(ChatError.UNAVAILABLE)));
                    } else if (event instanceof ChatStreamEvent.Done) {
                        isStreaming.setValue(false);
                    }
                }, error -> {
                    replaceLastAssistant(message ->
                            message.asError(errorText.apply(ChatError.NETWORK)));
                    isStreaming.setValue(false);
                }, () -> isStreaming.setValue(false));
    }

    private void replaceLastAssistant(UnaryOperator<ChatMessage> transform) {
        List<ChatMessage> current = messages.getValue();
        if (current == null) return;
        List<ChatMessage> updated = new ArrayList<>(current);
        for (int index = updated.size() - 1; index >= 0; index--) {
            ChatMessage message = updated.get(index);
            if (message.getRole() == ChatMessage.Role.ASSISTANT
                    || message.getRole() == ChatMessage.Role.ERROR) {
                updated.set(index, transform.apply(message));
                messages.setValue(updated);
                return;
            }
        }
    }

    @Override
    protected void onCleared() {
        if (streamDisposable != null) {
            streamDisposable.dispose();
        }
        super.onCleared();
    }
}
