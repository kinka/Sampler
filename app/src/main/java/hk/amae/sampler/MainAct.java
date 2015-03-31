package hk.amae.sampler;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.lang.reflect.Field;

import hk.amae.frag.MainFrag;
import hk.amae.frag.SettingFrag;
import hk.amae.util.Comm;


public class MainAct extends Activity
       implements MainFrag.OnMainFragListerer {
    private boolean isLocked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.act_main);

        Comm.initLogger(getPackageName());

        Comm.logI("entered main...");

        switchPanel(0);
    }

    private void switchPanel(int id) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        switch (id) {
            case R.id.btn_setting:
                ft.replace(R.id.container, new SettingFrag());
                break;
            case R.id.btn_connect:
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                return;
            default:
                ft.replace(R.id.container, new MainFrag());
        }
        if (id != 0)
            ft.addToBackStack("xxx");
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (isLocked) { // think about scrolling
            int statusHeight = 75;
            try {
                Class c = Class.forName("com.android.internal.R$dimen");
                Object obj = c.newInstance();
                Field field = c.getField("status_bar_height");
                int res = (Integer) field.get(obj);
                statusHeight = getResources().getDimensionPixelSize(res);
            } catch (Exception e) {

            }

            FrameLayout container = (FrameLayout) findViewById(R.id.container);
            float offsetX = container.getX();
            float offsetY = container.getY();

            LinearLayout wrapper = (LinearLayout) findViewById(R.id.layout_lockwrapper);
            offsetX += wrapper.getX();
            offsetY += wrapper.getY();

            ImageButton lock = (ImageButton) findViewById(R.id.toggle_lock);
            offsetX += lock.getX();
            offsetY += lock.getY();

            Rect rectSrc = new Rect((int) offsetX, (int) offsetY, (int) offsetX + lock.getWidth(), (int) offsetY + lock.getHeight());
            int eX = (int) ev.getX();
            int eY = (int) ev.getY() - statusHeight;
//            Comm.logI("x=" + eX + " y=" + eY + " rect=" + rectSrc.toShortString());
            boolean isInLock = eX < rectSrc.right && eX > rectSrc.left && eY < rectSrc.bottom && eY > rectSrc.top;
            if (!isInLock)
                return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onLockToggled(boolean locked) {
        isLocked = locked;

        ScrollView scrollContainer = (ScrollView) findViewById(R.id.scroll_container);
        if (locked)
            scrollContainer.setForeground(new ColorDrawable(0x60727272));
        else
            scrollContainer.setForeground(new ColorDrawable(0x00000000));

    }

    @Override
    public void onButtonClick(int id) {
        switchPanel(id);
    }
}
