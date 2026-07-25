package com.trishop.app.presentation.common;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.trishop.app.R;

/**
 * Looping "open water sonar" motif, mirroring iOS SonarRingsView: three
 * concentric accent rings that expand (scale 0.6 → 1.5 of the view radius) and
 * fade (0.9 → 0) on a staggered cycle. Purely decorative.
 */
public class SonarRingsView extends View {

    private static final float[] BASE_ALPHAS = {0.45f, 0.30f, 0.18f};

    // iOS: 3.2s per ring, staggered by 1.1s. Slightly slower here to read as calm.
    private static final float PERIOD_MS = 3600f;
    private static final float STAGGER_MS = 1200f;
    private static final float MIN_SCALE = 0.6f;
    private static final float MAX_SCALE = 1.5f;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final DecelerateInterpolator easeOut = new DecelerateInterpolator();
    private final ValueAnimator animator;
    private float elapsed = 0f;

    public SonarRingsView(@NonNull Context context) {
        this(context, null);
    }

    public SonarRingsView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SonarRingsView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.5f * getResources().getDisplayMetrics().density);
        paint.setColor(ContextCompat.getColor(context, R.color.accent));

        animator = ValueAnimator.ofFloat(0f, PERIOD_MS);
        animator.setDuration((long) PERIOD_MS);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(null);
        animator.addUpdateListener(animation -> {
            elapsed = (float) animation.getAnimatedValue();
            invalidate();
        });

        setClickable(false);
        setFocusable(false);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!animator.isStarted()) {
            animator.start();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        animator.cancel();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        // Base ring is a fraction of the view so that, scaled up to MAX_SCALE,
        // the largest ring still fits inside the view bounds (Android clips onDraw).
        float baseRadius = Math.min(getWidth(), getHeight()) * 0.32f;

        for (int i = 0; i < BASE_ALPHAS.length; i++) {
            // Each ring runs the same period, phase-shifted by its stagger.
            float t = (elapsed + i * STAGGER_MS) / PERIOD_MS;
            t -= (int) t;
            float eased = easeOut.getInterpolation(t);
            float scale = MIN_SCALE + (MAX_SCALE - MIN_SCALE) * eased;
            float opacity = BASE_ALPHAS[i] * 0.9f * (1f - t);
            opacity = Math.max(0f, Math.min(1f, opacity));
            paint.setAlpha((int) (opacity * 255));
            canvas.drawCircle(cx, cy, baseRadius * scale, paint);
        }
    }
}
