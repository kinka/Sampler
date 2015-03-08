package hk.amae.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;

import hk.amae.sampler.R;

/**
 * Created by kinka on 3/8/15.
 */
public class ActionSheet extends Dialog implements View.OnClickListener {

    private OnASItemClickListener mListener;
    private Window window;
    private LinearLayout panel;
    private View bg;
    private ArrayList<Button> items = new ArrayList<>();
    private boolean mCancelableTouchOutside = true;

    final int DURATION = 300;

    public ActionSheet(Context context) {
        super(context, android.R.style.Theme_Light_NoTitleBar);

        window = getWindow();

        window.setContentView(R.layout.action_sheet);

        window.setGravity(Gravity.BOTTOM);
        Drawable d = new ColorDrawable();
        d.setAlpha(0);
        window.setBackgroundDrawable(d);

        panel = (LinearLayout) window.findViewById(R.id.as_panel);
        bg = window.findViewById(R.id.as_bg);
        bg.setOnClickListener(this);

        window.findViewById(R.id.btn_as_cancel).setOnClickListener(this);
    }

    public void setCancelableTouchOutside(boolean cancelable) {
        mCancelableTouchOutside = cancelable;
    }
    public void setOnASItemClickListener(OnASItemClickListener listener) {
        mListener = listener;
    }

    public void addItems(String... titles) {
        int size = titles.length > 3 ? 3 : titles.length;
        items.add((Button) window.findViewById(R.id.btn_as_item1));
        items.add((Button) window.findViewById(R.id.btn_as_item2));
        items.add((Button) window.findViewById(R.id.btn_as_item3));
        for (int i=0; i<items.size(); i++) {
            Button item = items.get(i);
            if (i < size) {
                item.setText(titles[i]);
                item.setOnClickListener(this);
            } else {
                item.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onClick(View view) {
        int pos = -1;
        int id = view.getId();
        switch (id) {
            case R.id.as_bg:
                if (!mCancelableTouchOutside)
                    break;
            case R.id.btn_as_cancel:
                dismissMenu();
                break;

            default:
            if (id == R.id.btn_as_item1)
                pos = 0;
            else if (id == R.id.btn_as_item2)
                pos = 1;
            else if (id == R.id.btn_as_item3)
                pos = 2;
            if (mListener != null)
                mListener.onASItemClick(pos);
        }
    }

    public void showMenu() {
        show();
        bg.startAnimation(createAlphaIn());
        panel.startAnimation(createTranslationIn());
    }

    public void dismissMenu() {
        bg.startAnimation(createAlphaOut());
        panel.startAnimation(createTranslationOut());
    }

    Animation createTranslationIn() {
        int type = TranslateAnimation.RELATIVE_TO_SELF;
        TranslateAnimation an = new TranslateAnimation(type, 0, type, 0, type, 1, type, 0);
        an.setDuration(DURATION);
        return an;
    }

    Animation createAlphaIn() {
        AlphaAnimation an = new AlphaAnimation(0, 1);
        an.setDuration(DURATION);
        return an;
    }

    @Override
    public void onBackPressed() {
        dismissMenu();
    }

    Animation createTranslationOut() {
        int type = TranslateAnimation.RELATIVE_TO_SELF;
        TranslateAnimation an = new TranslateAnimation(type, 0, type, 0, type, 0, type, 1);
        an.setDuration(DURATION);
        an.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                dismiss();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        return an;
    }

    Animation createAlphaOut() {
        AlphaAnimation an = new AlphaAnimation(1, 0);
        an.setDuration(DURATION);
        an.setFillAfter(true);
        return an;
    }

    public static interface OnASItemClickListener {
        public void onASItemClick(int position);
    }
}
