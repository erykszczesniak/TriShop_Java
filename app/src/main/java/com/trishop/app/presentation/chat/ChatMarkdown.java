package com.trishop.app.presentation.chat;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;

import androidx.annotation.NonNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.function.Supplier;

/**
 * The assistant streams Markdown — render the inline syntax (bold, italic,
 * code) while preserving line breaks, instead of showing raw asterisks.
 */
public final class ChatMarkdown {

    private static final Pattern BOLD = Pattern.compile("\\*\\*(.+?)\\*\\*");
    private static final Pattern ITALIC = Pattern.compile("(?<!\\*)\\*([^*]+)\\*(?!\\*)");
    private static final Pattern CODE = Pattern.compile("`([^`]+)`");

    private ChatMarkdown() {
    }

    @NonNull
    public static CharSequence render(@NonNull String text) {
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        apply(builder, BOLD, () -> new StyleSpan(Typeface.BOLD));
        apply(builder, ITALIC, () -> new StyleSpan(Typeface.ITALIC));
        apply(builder, CODE, () -> new TypefaceSpan("monospace"));
        return builder;
    }

    private static void apply(SpannableStringBuilder builder,
                              Pattern pattern,
                              Supplier<Object> spanFactory) {
        int searchStart = 0;
        while (true) {
            Matcher matcher = pattern.matcher(builder);
            if (!matcher.find(searchStart)) break;
            String inner = matcher.group(1);
            if (inner == null) break;
            int start = matcher.start();
            builder.replace(start, matcher.end(), inner);
            builder.setSpan(
                    spanFactory.get(),
                    start,
                    start + inner.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            searchStart = start + inner.length();
        }
    }
}
