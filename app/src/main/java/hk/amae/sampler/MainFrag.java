package hk.amae.sampler;


import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;

import hk.amae.util.Comm;
import hk.amae.widget.TextProgressBar;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFrag extends Fragment implements View.OnClickListener {
    OnMainFragListerer mCallback;

    Spinner spinChannel;
    TextProgressBar progSampling;
    Activity parent;
    ImageButton btnLock;

    private boolean isLocked = false;
    private boolean isRunning = false;
    private boolean isCharging = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_main, container, false);
        spinChannel = (Spinner) v.findViewById(R.id.spin_channel);
        ArrayAdapter<CharSequence> spinAdapter =
                ArrayAdapter.createFromResource(parent, R.array.channels_array, android.R.layout.simple_spinner_item);
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinChannel.setAdapter(spinAdapter);

        progSampling = (TextProgressBar) v.findViewById(R.id.prog_sampling);
        btnLock = (ImageButton) v.findViewById(R.id.toggle_lock);
        btnLock.setOnClickListener(this);
        v.findViewById(R.id.toggle_run).setOnClickListener(this);
        v.findViewById(R.id.btn_setting).setOnClickListener(this);
        v.findViewById(R.id.btn_connect).setOnClickListener(this);
        v.findViewById(R.id.btn_query).setOnClickListener(this);
        v.findViewById(R.id.btn_clean).setOnClickListener(this);
        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        parent = activity;
        mCallback = (OnMainFragListerer) parent;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.toggle_lock:
                isLocked = !isLocked;
                if (isLocked)
                    ((ImageButton) view).setImageResource(R.drawable.lock);
                else
                    ((ImageButton) view).setImageResource(R.drawable.unlock);
                Rect rect = new Rect();
                btnLock.getDrawingRect(rect);
                mCallback.onLockToggled(isLocked);
                break;

            case R.id.toggle_run:
                isRunning = !isRunning;
                if (isRunning)
                    ((ImageButton) view).setImageResource(R.drawable.pause);
                else
                    ((ImageButton) view).setImageResource(R.drawable.play);
                break;

            case R.id.btn_setting:
            case R.id.btn_connect:
            case R.id.btn_query:
            case R.id.btn_clean:
                mCallback.onButtonClick(view.getId());
                break;
        }
    }

    public interface OnMainFragListerer {
        public void onLockToggled(boolean locked);
        public void onButtonClick(int id);
    }
}
