package hk.amae.sampler;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import hk.amae.frag.BacklitFrag;
import hk.amae.frag.ClearSampleFrag;
import hk.amae.frag.HardwareFrag;
import hk.amae.frag.RestoreFrag;
import hk.amae.frag.SetDateTimeFrag;
import hk.amae.frag.SetPowerOffFrag;


public class HardwareAct extends Activity implements View.OnClickListener {
    String[] FragTitles = {"硬件管理器", "背光亮度设置", "时间设置", "恢复出厂设置", "清空采样数据", "定时关机设置"};
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

            case R.id.btn_time:
                labelTitle.setText(FragTitles[2]);
                ft.replace(R.id.container, new SetDateTimeFrag());
                break;

            case R.id.btn_restore:
                labelTitle.setText(FragTitles[3]);
                ft.replace(R.id.container, new RestoreFrag());
                break;

            case R.id.btn_clear:
                labelTitle.setText(FragTitles[4]);
                ft.replace(R.id.container, new ClearSampleFrag());
                break;

            case R.id.btn_poweroff:
                labelTitle.setText(FragTitles[5]);
                ft.replace(R.id.container, new SetPowerOffFrag());
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
            case R.id.btn_time:
            case R.id.btn_restore:
            case R.id.btn_clear:
            case R.id.btn_poweroff:
                switchPanel(id);
                break;
        }
    }
}
