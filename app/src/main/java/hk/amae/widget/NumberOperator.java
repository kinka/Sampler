package hk.amae.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

import java.util.Timer;
import java.util.TimerTask;

import hk.amae.sampler.R;
import hk.amae.util.Comm;

/**
 * Created by kinka on 2/11/15.
 */
public class NumberOperator extends FrameLayout implements View.OnClickListener, View.OnTouchListener {
    private Button btnAdder, btnSubstractor;
    private EditText txtValue;
    private int value;
    private int delta = 10;
    Timer timer = new Timer();
    Handler handler;

    public NumberOperator(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.num_operator, this, true);
        init();
        setAttrs(attrs);
    }

    public NumberOperator(Context context) {
        super(context);
    }

    private void init() {
        btnAdder = (Button) findViewById(R.id.btn_adder);
        btnSubstractor = (Button) findViewById(R.id.btn_substractor);
        txtValue = (EditText) findViewById(R.id.txt_value);

        btnAdder.setOnClickListener(this);
        btnAdder.setOnTouchListener(this);

        btnSubstractor.setOnClickListener(this);
        btnSubstractor.setOnTouchListener(this);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                    case -1:
                        txtValue.setText("" + value);
                        break;
                }
            }
        };
    }

    private void setAttrs(AttributeSet attrs) {
        if (attrs == null)
            return;

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.NumberOperator, 0, 0);
        delta = a.getInt(R.styleable.NumberOperator_delta, 0);
        a.recycle();
    }

    public int getValue() {
        return value;
    }

    @Override
    public void onClick(View view) {
        value = Integer.valueOf(txtValue.getText().toString());
        switch (view.getId()) {
            case R.id.btn_adder:
                value+=delta;
                break;
            case R.id.btn_substractor:
                if (value > 0)
                    value-=delta;
                break;
        }
        txtValue.setText("" + value);
    }

    int holding = 0;
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (view.getId() == R.id.btn_adder)
            holding = 1;
        else if(view.getId() == R.id.btn_substractor)
            holding = -1;
        else
            holding = 0;

        value = Integer.valueOf(txtValue.getText().toString());

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                quickly();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                timer.cancel();
                break;
        }
        return false;
    }
    void quickly() {
        timer.cancel();
        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (holding == 1)
                    value += delta;
                else if (holding == -1)
                    value -= delta;
                if (value < 0)
                    value = 0;
                Message msg = new Message();
                msg.what = holding;
                handler.sendMessage(msg);
            }
        };
        timer.schedule(task, 500, 50);
    }
}
