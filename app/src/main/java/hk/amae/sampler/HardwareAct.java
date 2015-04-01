package hk.amae.sampler;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import hk.amae.frag.BacklitFrag;
import hk.amae.frag.HardwareFrag;


public class HardwareAct extends Activity implements View.OnClickListener {
    String[] FragTitles = {"硬件管理器", "背光亮度设置", "时间设置", "时间设置", "恢复出厂设置", "清空采样数据", "定时关机设置"};
    TextView labelTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_hardware);

        labelTitle = (TextView) findViewById(R.id.label_title);

        switchPanel(0);
    }

    private void switchPanel(int id) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        switch (id) {
            case R.id.btn_backlit:
                labelTitle.setText(FragTitles[1]);
                ft.replace(R.id.container, new BacklitFrag());
                break;

            default:
                labelTitle.setText(FragTitles[0]);
                ft.replace(R.id.container, new HardwareFrag());
        }
        if (id != 0)
            ft.addToBackStack("xxx");
//        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.btn_backlit:
                switchPanel(id);
                break;
        }
    }
}
