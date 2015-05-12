package hk.amae.sampler;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import hk.amae.util.Command;


public class HistoryAct extends Activity implements View.OnClickListener {
    ListView listView;
    int PageSize = 25;
    int PageNum = 0;
    int currentPage = -1;
    String fmtCurrent = "位号%03d/%d";
    TextView txtPage;
    ArrayList<HistoryItem> HistoryData = new ArrayList<>(800);
    ArrayList<HistoryItem> historyItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_history);

        listView = (ListView) findViewById(R.id.listView);

        fetch(1, 800);
//        for (int i=0; i<PageSize; i++)
//            historyItems.add(new HistoryItem());
        HistoryArrayAdaptor adaptor = new HistoryArrayAdaptor(this, R.layout.history_item, historyItems);
        listView.setAdapter(adaptor);

        findViewById(R.id.btn_prev).setOnClickListener(this);
        findViewById(R.id.btn_next).setOnClickListener(this);
        findViewById(R.id.btn_back).setOnClickListener(this);

        txtPage = (TextView) findViewById(R.id.txt_page);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.btn_prev:
                prev();
                break;
            case R.id.btn_next:
                next();
                break;
            case R.id.btn_back:
                super.onBackPressed();
                break;
        }

    }

    private void prev() {
        if (currentPage == 0)
            return;
        currentPage--;
        int len = PageSize;
        int base = currentPage * PageSize;
        if (base + PageSize > HistoryData.size())
            len = HistoryData.size() - base;
        historyItems.clear();
        for (int i=0; i<len; i++) {
            historyItems.add(new HistoryItem(i + 1, HistoryData.get(base + i).title, false));
        }

        txtPage.setText(String.format(fmtCurrent, currentPage + 1, PageNum));
        ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
    }
    private void next() {
        if (currentPage+1 >= PageNum)
            return;
        currentPage += 1;
        int len = PageSize;
        int base = currentPage * PageSize;
        if (base + PageSize > HistoryData.size())
            len = HistoryData.size() - base;
        historyItems.clear();
        for (int i=0; i<len; i++)
            historyItems.add(new HistoryItem(i + 1, HistoryData.get(base + i).title, true));

        txtPage.setText(String.format(fmtCurrent, currentPage + 1, PageNum));
        ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
    }

    private void fetch(int start, int end) {
        new Command(new Command.Once() {
            @Override
            public void done(boolean verify, Command cmd) {
                cmd.History = new String[800];
                for (int i=0; i<cmd.History.length; i++)
                    HistoryData.add(new HistoryItem(i+1, "History " + (i+1), true));
                PageNum = HistoryData.size() / PageSize + (HistoryData.size() % PageSize == 0 ? 0 : 1);
                next();
            }
        }).reqSampleHistory(start, end);
    }

    public class HistoryArrayAdaptor extends ArrayAdapter<HistoryItem> {
        private final Context context;
        private final ArrayList<HistoryItem> values;
        private final int rowLayout;

        public HistoryArrayAdaptor(Context context, int resource, ArrayList<HistoryItem> values) {
            super(context, resource, values);
            this.context = context;
            this.values = values;
            this.rowLayout = resource;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View rowView;
            if (convertView == null) {
                rowView = inflater.inflate(rowLayout, parent, false);
            } else {
                rowView = convertView;
            }
            TextView num = (TextView) rowView.findViewById(R.id.label_num);
            TextView title = (TextView) rowView.findViewById(R.id.label_title);
            CheckBox print = (CheckBox) rowView.findViewById(R.id.chk_print);

            HistoryItem item = values.get(position);
            num.setText("" + item.rowid);
            title.setText(item.title);
            print.setChecked(item.print);

            return rowView;
        }

    }

    public class HistoryItem {
        int rowid = 0;
        String title = "201503281518";
        boolean print = false;

        public HistoryItem() {}
        public HistoryItem(int id, String title, boolean print) {
            this.rowid = id;
            this.title = title;
            this.print = print;
        }
    }
}
