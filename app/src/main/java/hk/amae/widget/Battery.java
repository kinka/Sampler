package hk.amae.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.ImageView;

import hk.amae.sampler.R;

/**
 * Created by kinka on 2/14/15.
 */
public class Battery extends ImageView {
    private int[] status = {
            R.drawable.plug,
            R.drawable.battery_0,
            R.drawable.battery_25,
            R.drawable.battery_50,
            R.drawable.battery_75,
            R.drawable.battery_100};
    private int cap = 0; // 电池容量

    public Battery(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setAttrs(attrs);
    }

    public Battery(Context context, AttributeSet attrs) {
        super(context, attrs);
        setAttrs(attrs);
    }

    public Battery(Context context) {
        super(context);
    }

    private void setAttrs(AttributeSet attrs) {
        if (attrs == null)
            return;

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.Battery, 0, 0);
        setCapacity(a.getInt(R.styleable.Battery_cap, 0));
        a.recycle();
    }

    private void adjustStatus() {
        int res = status[cap / 25 + 1];
        setImageResource(res);
        postInvalidate();
    }

    public void setCapacity(int cap) {
        if (cap > 100)
            cap = 100;
        if (cap < 0)
            cap = 0;
        this.cap = cap;
        adjustStatus();
    }

    public int getCapacity() {
        return this.cap;
    }
}
