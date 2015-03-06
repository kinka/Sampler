package hk.amae.util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * Created by kinka on 3/6/15.
 */
public class ActivityGestureDetector implements View.OnTouchListener {
    private float downX, downY;
    private long timeDown;
    private final float MIN_DIST;
    private final int VELOCITY;
    private final float MAX_OFF_PATH;

    public ActivityGestureDetector(Context ctx, SwipeInterface act) {
        this.activity = act;
        final ViewConfiguration vc = ViewConfiguration.get(ctx);
        DisplayMetrics dm = ctx.getResources().getDisplayMetrics();
        MIN_DIST = vc.getScaledPagingTouchSlop() * dm.density;
        VELOCITY = vc.getScaledMinimumFlingVelocity();
        MAX_OFF_PATH = MIN_DIST * 2;
    }

    SwipeInterface activity;

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                timeDown = System.currentTimeMillis();
                downX = motionEvent.getX();
                downY = motionEvent.getY();
                return true;
            }
            case MotionEvent.ACTION_UP:
                long timeUp = System.currentTimeMillis();
                float upX = motionEvent.getX();
                float upY = motionEvent.getY();

                long time = timeUp - timeDown;

                float deltaX = downX - upX;
                float absDeltaX = Math.abs(deltaX);
                float deltaY = downY - upY;
                float absDeltaY = Math.abs(deltaY);

                if (absDeltaY > MAX_OFF_PATH)
                    return view.performClick();
                final long M_SEC = 1000;

//                Comm.logI(String.format("absDeltaX=%.2f, time=%d, VELOCITY=%d, time*VELOCITY/M_SEC=%d, absDeltaX > time * VELOCITY / M_SEC=%b",
//                        absDeltaX, time, VELOCITY, time * VELOCITY / M_SEC, (absDeltaX > time * VELOCITY / M_SEC)));

                if (absDeltaX > MIN_DIST && absDeltaX > time * VELOCITY / M_SEC) {
                    if (deltaX < 0) {
                        activity.onLeftWipe(view);
                        return true;
                    }
                    if (deltaX > 0) {
                        activity.onRightWipe(view);
                        return true;
                    }
                } else {

                }
                break;
        }
        return false;
    }
}
