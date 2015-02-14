package hk.amae.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ProgressBar;

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
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Rect bounds = new Rect();
        textPaint.getTextBounds(text, 0, text.length(), bounds);
        int x = getWidth() / 2 - bounds.centerX();
        int y = getHeight() / 2 - bounds.centerY();
        canvas.drawText(text, x, y, textPaint);
    }

    private void setAttrs(AttributeSet attrs) {
        if (attrs == null)
            return;

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.TextProgressBar, 0, 0);
        setText(a.getString(R.styleable.TextProgressBar_text));
        setTextSize(a.getDimension(R.styleable.TextProgressBar_textSize, 16));
        setTextColor(a.getColor(R.styleable.TextProgressBar_textColor, Color.BLACK));
        a.recycle();
    }

    public synchronized void setText(String text) {
        this.text = text == null ? "" : text;
        postInvalidate();
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
