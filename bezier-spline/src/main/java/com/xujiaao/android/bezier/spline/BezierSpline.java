package com.xujiaao.android.bezier.spline;

import android.graphics.Path;

/**
 * Smooth Bézier Spline Through Prescribed Points.
 *
 * @see <a href="https://www.particleincell.com/2012/bezier-splines">Bezier Splines</a>
 */
@SuppressWarnings("WeakerAccess")
public class BezierSpline {

    private final int mKnots;

    private final float[] mX;
    private final float[] mY;

    private final float[] mPX1;
    private final float[] mPY1;
    private final float[] mPX2;
    private final float[] mPY2;

    private boolean mResolved;
    private ControlPointsResolver mResolver;

    public BezierSpline(int knots) throws IllegalArgumentException {
        if (knots <= 1) {
            throw new IllegalArgumentException("At least two knot points required");
        }

        mKnots = knots;
        mX = new float[knots];
        mY = new float[knots];

        final int segments = knots - 1;
        mPX1 = new float[segments];
        mPY1 = new float[segments];
        mPX2 = new float[segments];
        mPY2 = new float[segments];
    }

    /**
     * Gets knots count.
     */
    public int knots() {
        return mKnots;
    }

    /**
     * Gets segments count.
     */
    public int segments() {
        return mKnots - 1;
    }

    /**
     * Sets coordinates of knot.
     */
    public void set(int knot, float x, float y) {
        mX[knot] = x;
        mY[knot] = y;
        mResolved = false;
    }

    /**
     * Sets x coordinate of knot.
     */
    public void x(int knot, float x) {
        mX[knot] = x;
        mResolved = false;
    }

    /**
     * Sets y coordinate of knot.
     */
    public void y(int knot, float y) {
        mY[knot] = y;
        mResolved = false;
    }

    /**
     * Gets x coordinate of knot.
     */
    public float x(int knot) {
        return mX[knot];
    }

    /**
     * Gets y coordinate of knot.
     */
    public float y(int knot) {
        return mY[knot];
    }

    /**
     * Gets resolved x coordinate of first control point.
     */
    @SuppressWarnings("unused")
    public float px1(int segment) {
        ensureResolved();
        return mPX1[segment];
    }

    /**
     * Gets resolved y coordinate of first control point.
     */
    @SuppressWarnings("unused")
    public float py1(int segment) {
        ensureResolved();
        return mPY1[segment];
    }

    /**
     * Gets resolved x coordinate of second control point.
     */
    @SuppressWarnings("unused")
    public float px2(int segment) {
        ensureResolved();
        return mPX2[segment];
    }

    /**
     * Gets resolved y coordinate of second control point.
     */
    @SuppressWarnings("unused")
    public float py2(int segment) {
        ensureResolved();
        return mPY2[segment];
    }

    /**
     * Applies resolved control points to the specified Path.
     */
    public void applyToPath(Path path) {
        ensureResolved();

        path.reset();
        path.moveTo(mX[0], mY[0]);

        final int segments = mKnots - 1;
        if (segments == 1) {
            path.lineTo(mX[1], mY[1]);
        } else {
            for (int segment = 0; segment < segments; segment++) {
                final int knot = segment + 1;
                path.cubicTo(
                        mPX1[segment],
                        mPY1[segment],
                        mPX2[segment],
                        mPY2[segment],
                        mX[knot],
                        mY[knot]);
            }
        }
    }

    private void ensureResolved() {
        if (!mResolved) {
            final int segments = mKnots - 1;
            if (segments == 1) {
                mPX1[0] = mX[0];
                mPY1[0] = mY[0];
                mPX2[0] = mX[1];
                mPY2[0] = mY[1];
            } else {
                if (mResolver == null) {
                    mResolver = new ControlPointsResolver(segments);
                }

                mResolver.resolve(mX, mPX1, mPX2);
                mResolver.resolve(mY, mPY1, mPY2);
            }

            mResolved = true;
        }
    }

    /**
     * Copied from https://www.particleincell.com/wp-content/uploads/2012/06/bezier-spline.js
     */
    private static class ControlPointsResolver {

        private final int mSegments;

        private final float[] mA;
        private final float[] mB;
        private final float[] mC;
        private final float[] mR;

        ControlPointsResolver(int segments) {
            mSegments = segments;

            mA = new float[segments];
            mB = new float[segments];
            mC = new float[segments];
            mR = new float[segments];
        }

        void resolve(float[] K, float[] P1, float[] P2) {
            final int segments = mSegments;
            final int last = segments - 1;

            final float[] A = mA;
            final float[] B = mB;
            final float[] C = mC;
            final float[] R = mR;

            // prepare left most segment.
            A[0] = 0F;
            B[0] = 2F;
            C[0] = 1F;
            R[0] = K[0] + 2F * K[1];

            // prepare internal segments.
            for (int i = 1; i < last; i++) {
                A[i] = 1F;
                B[i] = 4F;
                C[i] = 1F;
                R[i] = 4F * K[i] + 2F * K[i + 1];
            }

            // prepare right most segment.
            A[last] = 2F;
            B[last] = 7F;
            C[last] = 0F;
            R[last] = 8F * K[last] + K[segments];

            // solves Ax=b with the Thomas algorithm (from Wikipedia).
            for (int i = 1; i < segments; i++) {
                final float m = A[i] / B[i - 1];
                B[i] = B[i] - m * C[i - 1];
                R[i] = R[i] - m * R[i - 1];
            }

            P1[last] = R[last] / B[last];

            for (int i = segments - 2; i >= 0; i--) {
                P1[i] = (R[i] - C[i] * P1[i + 1]) / B[i];
            }

            // we have p1, now compute p2.
            for (int i = 0; i < last; i++) {
                P2[i] = 2F * K[i + 1] - P1[i + 1];
            }

            P2[last] = (K[segments] + P1[segments - 1]) / 2F;
        }
    }
}
