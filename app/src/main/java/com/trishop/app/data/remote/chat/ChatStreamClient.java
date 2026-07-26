package com.trishop.app.data.remote.chat;

import androidx.annotation.NonNull;

import com.trishop.app.BuildConfig;
import com.trishop.app.presentation.chat.ChatProduct;
import com.trishop.app.presentation.chat.ChatStreamEvent;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSource;

/**
 * Client for the standalone AI assistant — POSTs a message and consumes the
 * server-sent-event stream ({@code message} token deltas, {@code products},
 * {@code error}, {@code done}).
 */
@Singleton
public class ChatStreamClient {

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    @Inject
    public ChatStreamClient() {
    }

    @NonNull
    public Flowable<ChatStreamEvent> stream(@NonNull String message, @NonNull String sessionId) {
        return Flowable.<ChatStreamEvent>create(emitter -> {
            String body = new JSONObject()
                    .put("message", message)
                    .put("session_id", sessionId)
                    .toString();
            Request request = new Request.Builder()
                    .url(BuildConfig.CHAT_STREAM_URL)
                    .post(RequestBody.create(body, MediaType.parse("application/json")))
                    .build();

            try (Response response = client.newCall(request).execute()) {
                emitter.setCancellable(response::close);
                if (response.code() == 429) {
                    emitter.onNext(new ChatStreamEvent.RateLimited());
                    emitter.onComplete();
                    return;
                }
                if (!response.isSuccessful() || response.body() == null) {
                    emitter.onNext(new ChatStreamEvent.Error());
                    emitter.onComplete();
                    return;
                }
                BufferedSource source = response.body().source();

                String currentEvent = "message";
                while (!emitter.isCancelled()) {
                    String line = source.readUtf8Line();
                    if (line == null) break;
                    if (line.startsWith("event:")) {
                        currentEvent = line.substring("event:".length()).trim();
                    } else if (line.startsWith("data:")) {
                        String payload = line.substring("data:".length()).trim();
                        ChatStreamEvent event = parse(currentEvent, payload);
                        if (event != null) {
                            emitter.onNext(event);
                        }
                        if ("done".equals(currentEvent)) break;
                        // Each data line completes a frame in this protocol.
                        currentEvent = "message";
                    }
                }
                emitter.onNext(new ChatStreamEvent.Done());
                emitter.onComplete();
            }
        }, BackpressureStrategy.BUFFER).subscribeOn(Schedulers.io());
    }

    private static ChatStreamEvent parse(String event, String payload) {
        try {
            switch (event) {
                case "message": {
                    String text = new JSONObject(payload).optString("text");
                    return text.isEmpty() ? null : new ChatStreamEvent.Token(text);
                }
                case "products": {
                    JSONArray array = new JSONArray(payload);
                    List<ChatProduct> products = new ArrayList<>();
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject item = array.getJSONObject(i);
                        products.add(new ChatProduct(
                                item.getLong("id"),
                                item.getString("name"),
                                item.optString("brand"),
                                item.optString("category"),
                                item.getDouble("price"),
                                resolveImage(item.optString("image_url"))));
                    }
                    return new ChatStreamEvent.Products(products);
                }
                case "error":
                    return new ChatStreamEvent.Error();
                case "done":
                    return new ChatStreamEvent.Done();
                default:
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private static String resolveImage(String url) {
        if (url.startsWith("/")) {
            String base = BuildConfig.BASE_URL;
            while (base.endsWith("/")) {
                base = base.substring(0, base.length() - 1);
            }
            return base + url;
        }
        return url;
    }
}
