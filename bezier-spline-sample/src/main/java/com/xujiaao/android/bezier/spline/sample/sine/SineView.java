package com.xujiaao.android.bezier.spline.sample.sine;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.xujiaao.android.bezier.spline.BezierSpline;
import com.xujiaao.android.bezier.spline.sample.R;

class SineView extends View {

    private final Path mPath = new Path();
    private final Paint mPaint = new Paint();
    private final BezierSpline mBezierSpline = new BezierSpline(20);

    SineView(Context context) {
        super(context);

        final Paint paint = mPaint;
        paint.setAntiAlias(true);
        paint.setColor(ContextCompat.getColor(context, R.color.colorAccent));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(context.getResources().getDisplayMetrics().density * 2F);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.translate(0F, getHeight() / 2F);

        final float width = getWidth();
        final float density = getResources().getDisplayMetrics().density;
        for (int knot = 0, knots = mBezierSpline.knots(); knot < knots; knot++) {
            final float x = knot * (width / (knots - 1F));
            final float y = (float) (Math.toDegrees(Math.sin(Math.toRadians(x / density))) * density);
            mBezierSpline.set(knot, x, y);

            canvas.drawCircle(x, y, density * 4F, mPaint);
        }

        mBezierSpline.applyToPath(mPath);
        canvas.drawPath(mPath, mPaint);
    }
}
