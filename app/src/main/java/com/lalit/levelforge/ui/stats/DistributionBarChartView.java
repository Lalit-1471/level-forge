package com.lalit.levelforge.ui.stats;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.lalit.levelforge.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DistributionBarChartView extends View {

    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final List<StatsPoint> points = new ArrayList<>();

    public DistributionBarChartView(Context context) {
        super(context);
        init();
    }

    public DistributionBarChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DistributionBarChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setPoints(List<StatsPoint> chartPoints) {
        points.clear();
        if (chartPoints != null) {
            points.addAll(chartPoints);
        }
        requestLayout();
        invalidate();
    }

    private void init() {
        labelPaint.setColor(ContextCompat.getColor(getContext(), R.color.slate_200));
        labelPaint.setTextSize(sp(12));
        labelPaint.setFakeBoldText(true);

        valuePaint.setColor(ContextCompat.getColor(getContext(), R.color.slate_300));
        valuePaint.setTextSize(sp(11));

        trackPaint.setColor(ContextCompat.getColor(getContext(), R.color.slate_800));
        trackPaint.setStyle(Paint.Style.FILL);

        barPaint.setColor(ContextCompat.getColor(getContext(), R.color.teal_300));
        barPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int rows = Math.max(1, points.size());
        int desiredHeight = (int) dp(24 + rows * 42);
        int height = resolveSize(desiredHeight, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (points.isEmpty()) {
            drawEmptyState(canvas);
            return;
        }

        double max = maxValue();
        float left = dp(14);
        float right = getWidth() - dp(14);
        float labelY = dp(24);
        for (StatsPoint point : points) {
            float barTop = labelY + dp(8);
            float barBottom = barTop + dp(10);
            float barWidth = (float) ((right - left) * (point.getValue() / Math.max(1, max)));

            canvas.drawText(point.getLabel(), left, labelY, labelPaint);
            String value = format(point.getValue());
            canvas.drawText(value, right - valuePaint.measureText(value), labelY, valuePaint);

            rect.set(left, barTop, right, barBottom);
            canvas.drawRoundRect(rect, dp(5), dp(5), trackPaint);
            rect.set(left, barTop, left + barWidth, barBottom);
            canvas.drawRoundRect(rect, dp(5), dp(5), barPaint);
            labelY += dp(42);
        }
    }

    private void drawEmptyState(Canvas canvas) {
        String text = getContext().getString(R.string.stats_chart_empty);
        float x = (getWidth() - valuePaint.measureText(text)) / 2f;
        canvas.drawText(text, Math.max(dp(12), x), Math.max(dp(32), getHeight() / 2f), valuePaint);
    }

    private double maxValue() {
        double max = 0;
        for (StatsPoint point : points) {
            max = Math.max(max, point.getValue());
        }
        return max;
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
