package com.lalit.levelforge.ui.stats;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.lalit.levelforge.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProgressLineChartView extends View {

    private final Paint axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path linePath = new Path();
    private final List<StatsPoint> points = new ArrayList<>();

    public ProgressLineChartView(Context context) {
        super(context);
        init();
    }

    public ProgressLineChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ProgressLineChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setPoints(List<StatsPoint> chartPoints) {
        points.clear();
        if (chartPoints != null) {
            points.addAll(chartPoints);
        }
        invalidate();
    }

    private void init() {
        axisPaint.setColor(ContextCompat.getColor(getContext(), R.color.slate_700));
        axisPaint.setStrokeWidth(dp(1));

        gridPaint.setColor(ContextCompat.getColor(getContext(), R.color.slate_700));
        gridPaint.setStrokeWidth(dp(1));
        gridPaint.setAlpha(90);

        linePaint.setColor(ContextCompat.getColor(getContext(), R.color.orange_400));
        linePaint.setStrokeWidth(dp(3));
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeJoin(Paint.Join.ROUND);

        pointPaint.setColor(ContextCompat.getColor(getContext(), R.color.teal_300));
        pointPaint.setStyle(Paint.Style.FILL);

        textPaint.setColor(ContextCompat.getColor(getContext(), R.color.slate_300));
        textPaint.setTextSize(sp(11));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }

        float left = dp(38);
        float top = dp(16);
        float right = width - dp(10);
        float bottom = height - dp(28);
        drawGrid(canvas, left, top, right, bottom);

        if (points.isEmpty()) {
            drawEmptyState(canvas, width, height);
            return;
        }

        double max = maxValue();
        double min = minValue();
        if (max == min) {
            min = 0;
        }

        linePath.reset();
        for (int i = 0; i < points.size(); i++) {
            float x = xForIndex(i, left, right);
            float y = yForValue(points.get(i).getValue(), min, max, top, bottom);
            if (i == 0) {
                linePath.moveTo(x, y);
            } else {
                linePath.lineTo(x, y);
            }
        }
        canvas.drawPath(linePath, linePaint);

        for (int i = 0; i < points.size(); i++) {
            float x = xForIndex(i, left, right);
            float y = yForValue(points.get(i).getValue(), min, max, top, bottom);
            canvas.drawCircle(x, y, dp(4), pointPaint);
            if (i == 0 || i == points.size() - 1 || points.size() <= 4) {
                canvas.drawText(points.get(i).getLabel(), x - dp(12), height - dp(8), textPaint);
            }
        }

        canvas.drawText(format(max), dp(2), top + dp(5), textPaint);
        canvas.drawText(format(min), dp(2), bottom, textPaint);
    }

    private void drawGrid(Canvas canvas, float left, float top, float right, float bottom) {
        canvas.drawLine(left, top, left, bottom, axisPaint);
        canvas.drawLine(left, bottom, right, bottom, axisPaint);
        for (int i = 1; i <= 3; i++) {
            float y = top + ((bottom - top) * i / 4f);
            canvas.drawLine(left, y, right, y, gridPaint);
        }
    }

    private void drawEmptyState(Canvas canvas, int width, int height) {
        String text = getContext().getString(R.string.stats_chart_empty);
        float textWidth = textPaint.measureText(text);
        canvas.drawText(text, (width - textWidth) / 2f, height / 2f, textPaint);
    }

    private float xForIndex(int index, float left, float right) {
        if (points.size() == 1) {
            return (left + right) / 2f;
        }
        return left + ((right - left) * index / (points.size() - 1f));
    }

    private float yForValue(double value, double min, double max, float top, float bottom) {
        double range = Math.max(1, max - min);
        double normalized = (value - min) / range;
        return (float) (bottom - ((bottom - top) * normalized));
    }

    private double maxValue() {
        double max = 0;
        for (StatsPoint point : points) {
            max = Math.max(max, point.getValue());
        }
        return max;
    }

    private double minValue() {
        double min = Double.MAX_VALUE;
        for (StatsPoint point : points) {
            min = Math.min(min, point.getValue());
        }
        return min == Double.MAX_VALUE ? 0 : min;
    }

    private String format(double value) {
        if (value >= 1000) {
            return String.format(Locale.US, "%.1fk", value / 1000.0);
        }
        if (value == Math.rint(value)) {
            return String.valueOf((int) value);
        }
        return String.format(Locale.US, "%.1f", value);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    private float sp(float value) {
        return value * getResources().getDisplayMetrics().scaledDensity;
    }
}
