package hk.amae.sampler;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.lang.reflect.Field;

import hk.amae.util.Comm;


public class MainActivity extends Activity
       implements MainFrag.OnMainFragListerer {
    private boolean isLocked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);

        Comm.initLogger(getPackageName());

        Comm.logI("entered main...");

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.container, new MainFrag());
        ft.commit();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
}
