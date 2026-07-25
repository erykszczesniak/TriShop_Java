package com.trishop.app.presentation.common;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.trishop.app.R;

/**
 * Ambient mesh background mirroring iOS MeshBackgroundView: three soft radial
 * washes (accent top-left, cool slate top-right, accent bottom) over the base
 * screen background. Purely decorative, drawn as the bottom-most layer.
 */
public class MeshBackgroundView extends View {

    // center x, center y (fractions of size), radius (fraction), alpha, color
    private static final class Wash {
        final float cx;
        final float cy;
        final float r;
        final float alpha;
        final int color;

        Wash(float cx, float cy, float r, float alpha, int color) {
            this.cx = cx;
            this.cy = cy;
            this.r = r;
            this.alpha = alpha;
            this.color = color;
        }
    }

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final int baseColor;
    private final Wash[] washes;
    private final float[] stops = {0f, 0.55f};

    public MeshBackgroundView(@NonNull Context context) {
        this(context, null);
    }

    public MeshBackgroundView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MeshBackgroundView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        baseColor = ContextCompat.getColor(context, R.color.screen_background);
        int accent = ContextCompat.getColor(context, R.color.accent);
        int violet = ContextCompat.getColor(context, R.color.neutral_700);

        // Subtle washes over the dark base — iOS fades each to transparent well
        // within its radius, so the background reads as near-uniform slate with
        // a faint tint.
        washes = new Wash[]{
                new Wash(0.12f, -0.05f, 0.9f, 0.10f, accent),
                new Wash(1.0f, 0.10f, 0.75f, 0.08f, violet),
                new Wash(0.72f, 1.08f, 0.9f, 0.06f, accent)
        };

        setClickable(false);
        setFocusable(false);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        canvas.drawColor(baseColor);
        float w = getWidth();
        float h = getHeight();
        float maxDim = Math.max(w, h);
        for (Wash wash : washes) {
            float cx = wash.cx * w;
            float cy = wash.cy * h;
            float radius = wash.r * maxDim;
            int core = (wash.color & 0x00FFFFFF) | ((int) (wash.alpha * 255) << 24);
            int edge = wash.color & 0x00FFFFFF; // transparent
            paint.setShader(new RadialGradient(
                    cx, cy, radius, new int[]{core, edge}, stops, Shader.TileMode.CLAMP));
            canvas.drawCircle(cx, cy, radius, paint);
        }
        paint.setShader(null);
    }
}
