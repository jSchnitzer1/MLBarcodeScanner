package com.leamra.barcode_scanner.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.leamra.barcode_scanner.R;

public class CameraViewOverlay extends View {
    private final Paint boxPaint;
    private final Paint scrimPaint;
    private final Paint eraserPaint;
    private final float boxCornerRadius;
    private RectF boxRect;

    public final void setViewFinder() {
        float overlayWidth = this.getWidth();
        float overlayHeight = this.getHeight();
        float boxWidth = (overlayWidth * 80) / 100;
        float boxHeight = (overlayHeight) * 36 / 100;
        float cx = overlayWidth / 2;
        float cy = overlayHeight / 2;

        this.boxRect = new RectF(cx - boxWidth / 2, cy - boxHeight / 2, cx + boxWidth / 2, cy + boxHeight / 2);
        this.invalidate();
    }

    public CameraViewOverlay(@Nullable Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        boxPaint = new Paint();
        boxPaint.setColor(ContextCompat.getColor(context, R.color.barcode_reticle_stroke));
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth((float)context.getResources().getDimensionPixelOffset(R.dimen.barcode_reticle_stroke_width));

        scrimPaint = new Paint();
        scrimPaint.setColor(ContextCompat.getColor(context, R.color.barcode_reticle_background));

        eraserPaint = new Paint();
        eraserPaint.setStrokeWidth(this.boxPaint.getStrokeWidth());
        eraserPaint.setXfermode((Xfermode)(new PorterDuffXfermode(PorterDuff.Mode.CLEAR)));

        this.boxCornerRadius = (float)context.getResources().getDimensionPixelOffset(R.dimen.barcode_reticle_corner_radius);
    }

    public void draw (Canvas canvas) {
        super.draw(canvas);
        if(this.boxRect != null) {
            canvas.drawRect(0.0F, 0.0F, canvas.getWidth(), canvas.getHeight(), this.scrimPaint);
            eraserPaint.setStyle(Paint.Style.FILL);
            canvas.drawRoundRect(this.boxRect, this.boxCornerRadius, this.boxCornerRadius, this.eraserPaint);
            this.eraserPaint.setStyle(Paint.Style.STROKE);
            canvas.drawRoundRect(this.boxRect, this.boxCornerRadius, this.boxCornerRadius, this.eraserPaint);
            canvas.drawRoundRect(this.boxRect, this.boxCornerRadius, this.boxCornerRadius, this.boxPaint);
        }
    }
}
