package hk.amae.sampler;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.ScrollView;

import hk.amae.util.Comm;


public class MainActivity extends Activity
       implements MainFrag.OnMainFragListerer {

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
        Comm.logI("X " + ev.getX() + ", " + ev.getY());
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onLockToggled(boolean locked) {
        ScrollView scrollContainer = (ScrollView) findViewById(R.id.scroll_container);
        if (locked)
            scrollContainer.setForeground(new ColorDrawable(0x40727272));
        else
            scrollContainer.setForeground(new ColorDrawable(0x00000000));
    }
}
