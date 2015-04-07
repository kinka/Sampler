package hk.amae.widget;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.ProgressBar;
import android.widget.TextView;

import hk.amae.sampler.R;

/**
 * Created by kinka on 2/9/15.
 * from http://weavora.com/blog/2012/02/23/android-progressbar-with-text/
 */
public class TextProgressBar extends ProgressBar {
    Paint textPaint;
    String text = "";
    int textColor = Color.BLACK;
    float textSize = 16;
    boolean textBelow = false;

    String textFormat = "%d%%";

    Rect bounds = new Rect();

    void init() {
        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(textColor);
        textPaint.setTextSize(textSize);
    }

    public TextProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setAttrs(attrs);
        init();
    }

    public TextProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        setAttrs(attrs);
        init();
    }

    public TextProgressBar(Context context) {
        super(context);
        init();
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredHeight = measureHeight(heightMeasureSpec);
        int measureWidth = measureWidth(widthMeasureSpec);

        setMeasuredDimension(measureWidth, measuredHeight);
    }

    private int measureHeight(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        return specSize;
    }

    private int measureWidth(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        return specSize;
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        canvas.save();
        if (textBelow)
            canvas.scale(1.0f, 0.5f);

        super.onDraw(canvas);
        canvas.restore();

        text = String.format(textFormat, getProgress());

        textPaint.getTextBounds(text, 0, text.length(), bounds);
        int x = getWidth() / 2 - bounds.centerX();
        int y = getHeight() / 2 - bounds.centerY();
        if (textBelow)
            y = getHeight() + bounds.centerY();

        canvas.drawText(text, x, y, textPaint);
    }

    private void setAttrs(AttributeSet attrs) {
        if (attrs == null)
            return;

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.TextProgressBar, 0, 0);
        setTextSize(a.getDimension(R.styleable.TextProgressBar_textSize, 16));
        setTextColor(a.getColor(R.styleable.TextProgressBar_textColor, Color.BLACK));
        setFormat(a.getString(R.styleable.TextProgressBar_textFormat));
        setTextBelow(a.getBoolean(R.styleable.TextProgressBar_textBelow, false));
        a.recycle();
    }

    public synchronized void setTextBelow(boolean below) {
        this.textBelow = below;
        postInvalidate();
    }

    public synchronized void setFormat(String format) {
        this.textFormat = format == null ? "%d%%" : format;
    }

    public synchronized void setTextColor(int textColor) {
        this.textColor = textColor;
        postInvalidate();
    }

    public synchronized void setTextSize(float textSize) {
        this.textSize = textSize;
        postInvalidate();
    }
}
