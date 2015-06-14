package hk.amae.sampler;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import hk.amae.util.Comm;
import hk.amae.util.Command;


public class HistoryAct extends Activity implements View.OnClickListener {
    ListView listView;
    int PageSize = 25;
    int PageNum = 0;
    int currentPage = -1;
    String fmtCurrent = "位号%03d/%d";
    TextView txtPage;
    static ArrayList<HistoryItem> HistoryData = new ArrayList<>(800);
    ArrayList<HistoryItem> historyItems = new ArrayList<>();
    ArrayList<HistoryItem> filterItems = new ArrayList<>();
    boolean isFiltering = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_history);

        listView = (ListView) findViewById(R.id.listView);

        fetch(1, 800);

        HistoryArrayAdaptor adaptor = new HistoryArrayAdaptor(this, R.layout.history_item, historyItems);
        listView.setAdapter(adaptor);

        findViewById(R.id.btn_prev).setOnClickListener(this);
        findViewById(R.id.btn_next).setOnClickListener(this);
        findViewById(R.id.btn_back).setOnClickListener(this);

        txtPage = (TextView) findViewById(R.id.txt_page);
        findViewById(R.id.btn_print).setOnClickListener(this);

        final EditText editText = (EditText) findViewById(R.id.edit_query);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                doSearch(editText.getText().toString());
            }
        });
    }

    private void doSearch(String query) {
        if (query.length() > 0)
            isFiltering = true;
        else
            isFiltering = false;

        filterItems.clear();
        if (isFiltering) {
            for (HistoryItem item: HistoryData) {
                if (item.title.contains(query))
                    filterItems.add(item);
            }
            PageNum = filterItems.size() / PageSize + (filterItems.size() % PageSize == 0 ? 0 : 1);
        } else {
            PageNum = HistoryData.size() / PageSize + (HistoryData.size() % PageSize == 0 ? 0 : 1);
        }

        currentPage = -1;
        next();
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
            case R.id.btn_print:
                for (HistoryItem item: historyItems)
                    if (item.print)
                        doPrint(item.title);
                break;
            case R.id.label_title:{
                String id = ((TextView) view).getText().toString();
                showDetail(id);
            }
                break;
        }

    }

    private void doPrint(final String sampleId) {
        if (sampleId == null) return;

        new Command(new Command.Once() {
            @Override
            public void done(boolean verify, Command cmd) {
                Toast.makeText(HistoryAct.this, "正在打印"+sampleId+"中。。。", Toast.LENGTH_SHORT).show();
            }
        }).printSample(sampleId);
    }

    private void showDetail(String id) {
        Toast.makeText(this, "id: " + id, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(HistoryAct.this, QueryAct.class);
        intent.putExtra(QueryAct.KEY_ITEM, id);
        startActivity(intent);
    }

    private void prev() {
        if (currentPage == 0)
            return;
        currentPage--;

        flip();
    }
    private void next() {
        if (currentPage+1 >= PageNum)
            return;
        currentPage += 1;
        flip();
    }
    private void flip() {
        int len = PageSize;
        int base = currentPage * PageSize;

        List<HistoryItem> data = HistoryData;
        if (isFiltering)
            data = filterItems;

        if (base + PageSize > data.size())
            len = data.size() - base;
        historyItems.clear();
        for (int i=0; i<len; i++)
            historyItems.add(new HistoryItem(i + 1, data.get(base + i).title, false));

        txtPage.setText(String.format(fmtCurrent, currentPage + 1, PageNum));
        ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
    }

    private void fetch(int start, int end) {
        new Command(new Command.Once() {
            @Override
            public void done(boolean verify, Command cmd) {
//                cmd.History = new String[(int) Math.round(Math.random()*800)];
                if (cmd.History == null)
                    return;
                HistoryData.clear();
                for (int i=0; i<cmd.History.length; i++)
                    HistoryData.add(new HistoryItem(i+1, cmd.History[i], true));
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

            print.setOnCheckedChangeListener(item);

            title.setOnClickListener(HistoryAct.this);

            return rowView;
        }
    }

    public class HistoryItem implements CheckBox.OnCheckedChangeListener {
        int rowid = 0;
        String title = "201503281518";
        boolean print = false;

        public HistoryItem(int id, String title, boolean print) {
            this.rowid = id;
            this.title = title;
            this.print = print;
        }
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
            print = checked;
        }
    }
}
