package hk.amae.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import hk.amae.sampler.R;

/**
 * Created by kinka on 2/14/15.
 */
public class Battery extends FrameLayout {
    private ImageView imgPlug;
    private ImageView imgBattery;
    private TextView txtRemain;

    private int[] status = {
            R.drawable.plug,
            R.drawable.battery_0,
            R.drawable.battery_25,
            R.drawable.battery_50,
            R.drawable.battery_75,
            R.drawable.battery_100};
    private int cap = 0; // 电池容量
    private boolean isCharging = false;

    public Battery(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
        setAttrs(attrs);
    }

    public Battery(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
        setAttrs(attrs);
    }

    public Battery(Context context) {
        super(context);
        init(context);
    }

    void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.battery_status, this, true);

        imgPlug = (ImageView) findViewById(R.id.img_plug);
        imgBattery = (ImageView) findViewById(R.id.img_battery);
        txtRemain = (TextView) findViewById(R.id.txt_remain);
    }

    private void setAttrs(AttributeSet attrs) {
        if (attrs == null)
            return;

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.Battery, 0, 0);
        setCapacity(a.getInt(R.styleable.Battery_cap, 0));
        setCharging(a.getBoolean(R.styleable.Battery_charging, false));
        a.recycle();
    }

    private void adjustStatus() {
        int res;
        if (cap <= 12)
            res = status[1];
        else if (cap <= 25 + 12)
            res = status[2];
        else if (cap <= 50 + 12)
            res = status[3];
        else if (cap <= 75 + 12)
            res = status[4];
        else
            res = status[5];

        txtRemain.setText(this.cap + "%");
        imgBattery.setImageResource(res);
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

    public void setCharging(boolean isCharging) {
        this.isCharging = isCharging;
        imgPlug.setVisibility(isCharging ? VISIBLE : INVISIBLE);
    }

    public boolean getCharging() {
        return this.isCharging;
    }
}
