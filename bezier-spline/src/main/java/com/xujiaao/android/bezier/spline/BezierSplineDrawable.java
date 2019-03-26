package com.xujiaao.android.bezier.spline;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.view.animation.LinearInterpolator;

public class BezierSplineDrawable extends Drawable implements Animatable {

    private static final int DEFAULT_DURATION = 1000;

    private final Path mPath;
    private final Paint mPaint;
    private final BezierSpline mBezierSpline;
    private final Interpolator mInterpolator;

    private int mDuration;
    private int[] mStokeColors;
    private float mProgressOffset;
    private Animator mAnimator;

    @SuppressWarnings("WeakerAccess")
    protected BezierSplineDrawable(int knots) {
        this(knots, null);
    }

    @SuppressWarnings("WeakerAccess")
    public BezierSplineDrawable(int knots, Interpolator interpolator) {
        mPath = new Path();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);

        mBezierSpline = new BezierSpline(knots);
        mInterpolator = interpolator;

        setDuration(DEFAULT_DURATION);
    }

    // ---------------------------------------------------------------------------------------------
    // Settings
    // ---------------------------------------------------------------------------------------------

    @SuppressWarnings("WeakerAccess")
    public void setDuration(int duration) {
        if (mDuration != duration) {
            mDuration = duration;
        }
    }

    public void setStrokeWidth(float width) {
        mPaint.setStrokeWidth(width);
        invalidateSelf();
    }

    @SuppressWarnings("unused")
    public void setStrokeColors(int... colors) {
        mStokeColors = colors;
        mPaint.setShader(null);
        invalidateSelf();
    }

    // ---------------------------------------------------------------------------------------------
    // Drawable
    // ---------------------------------------------------------------------------------------------

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
        invalidateSelf();
    }

    @Override
    public int getAlpha() {
        return mPaint.getAlpha();
    }

    @Override
    public void setColorFilter(ColorFilter filter) {
        mPaint.setColorFilter(filter);
    }

    @Override
    public ColorFilter getColorFilter() {
        return mPaint.getColorFilter();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void setBounds(Rect bounds) {
        final Rect rect = getBounds();
        if (bounds.width() != rect.width() || bounds.height() != rect.height()) {
            mPaint.setShader(null);
        }

        super.setBounds(bounds);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void draw(Canvas canvas) {
        final Rect bounds = getBounds();
        final float sx = bounds.width();
        final float sy = bounds.height() / 2F;

        // prepare paint.
        final Paint paint = mPaint;
        final int[] colors = mStokeColors;
        if (colors == null || colors.length == 0) {
            paint.setColor(Color.TRANSPARENT);
        } else if (colors.length == 1) {
            paint.setColor(colors[0]);
        } else if (paint.getShader() == null) {
            paint.setShader(generateShader(bounds, colors));
        }

        // prepare path.
        final BezierSpline spline = mBezierSpline;
        final float segments = spline.segments();
        final float offset = mProgressOffset;
        for (int knot = 0, knots = spline.knots(); knot < knots; knot++) {
            final float progress = knot / segments;
            spline.set(knot, sx * progress, sy * getInterpolation(progress, offset));
        }

        final Path path = mPath;
        spline.applyToPath(path);

        final int saveCount = canvas.save();
        canvas.translate(bounds.left, bounds.top + sy);

        draw(canvas, path, paint);
        canvas.restoreToCount(saveCount);
    }

    @SuppressWarnings("WeakerAccess")
    protected Shader generateShader(Rect bounds, int[] colors) {
        final float delta = 1F / colors.length;
        final float[] positions = new float[colors.length];
        for (int i = 0; i < positions.length; i++) {
            positions[i] = delta * i;
        }

        final int width = bounds.width();
        return new LinearGradient(0, 0, width, 0, colors, positions, Shader.TileMode.REPEAT);
    }

    @SuppressWarnings("WeakerAccess")
    protected void draw(Canvas canvas, Path path, Paint paint) {
        canvas.drawPath(path, paint);
    }

    @SuppressWarnings("WeakerAccess")
    protected float getInterpolation(float progress, float offset) {
        float interpolation = 0F;
        if (mInterpolator != null) {
            interpolation = mInterpolator.getInterpolation(progress, offset);
        }

        return interpolation;
    }

    // ---------------------------------------------------------------------------------------------
    // Animatable
    // ---------------------------------------------------------------------------------------------

    @Override
    public void start() {
        if (mAnimator == null) {
            mAnimator = createAnimator();
        } else {
            mAnimator.cancel();
        }

        mAnimator.setDuration(mDuration);
        mAnimator.start();
    }

    @Override
    public void stop() {
        if (mAnimator != null) {
            mAnimator.cancel();
        }
    }

    @Override
    public boolean isRunning() {
        return mAnimator != null && mAnimator.isRunning();
    }

    private Animator createAnimator() {
        final ValueAnimator animator = ValueAnimator.ofFloat(0F, 1F);
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mProgressOffset = (Float) animation.getAnimatedValue();
                invalidateSelf();
            }
        });

        animator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationCancel(Animator animation) {
                mProgressOffset = 0F;
                invalidateSelf();
            }
        });

        return animator;
    }

    // ---------------------------------------------------------------------------------------------
    // Interpolator
    // ---------------------------------------------------------------------------------------------

    public interface Interpolator {

        float getInterpolation(float progress, float offset);
    }
}
