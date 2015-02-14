package hk.amae.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

import hk.amae.sampler.R;

/**
 * Created by kinka on 2/11/15.
 */
public class NumberOperator extends FrameLayout implements View.OnClickListener, View.OnLongClickListener {
    private Button btnAdder, btnSubstractor;
    private EditText txtValue;
    private int value;

    public NumberOperator(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.num_operator, this, true);
        init();
    }

    public NumberOperator(Context context) {
        super(context);
    }

    private void init() {
        btnAdder = (Button) findViewById(R.id.btn_adder);
        btnSubstractor = (Button) findViewById(R.id.btn_substractor);
        txtValue = (EditText) findViewById(R.id.txt_value);

        btnAdder.setOnClickListener(this);
        btnAdder.setOnLongClickListener(this);
        btnSubstractor.setOnClickListener(this);
        btnSubstractor.setOnLongClickListener(this);
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public void onClick(View view) {
        value = Integer.valueOf(txtValue.getText().toString());
        switch (view.getId()) {
            case R.id.btn_adder:
                value++;
                break;
            case R.id.btn_substractor:
                if (value > 0)
                    value--;
                break;
        }
        txtValue.setText("" + value);
    }

    @Override
    public boolean onLongClick(View view) {

        value = Integer.valueOf(txtValue.getText().toString());
        switch (view.getId()) {
            case R.id.btn_adder:
                value++;
                break;
            case R.id.btn_substractor:
                if (value > 0)
                    value--;
                break;
        }
        txtValue.setText("" + value);

        return false;
    }
}
