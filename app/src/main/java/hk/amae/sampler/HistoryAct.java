package hk.amae.sampler;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;


public class HistoryAct extends Activity implements View.OnClickListener {
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_history);

        listView = (ListView) findViewById(R.id.listView);
        ArrayList<HistoryItem> values = new ArrayList<>();
        for (int i=0; i<25; i++)
            values.add(new HistoryItem(i+1, "234234", true));
        HistoryArrayAdaptor adaptor = new HistoryArrayAdaptor(this, R.layout.history_item, values);
        listView.setAdapter(adaptor);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

        }

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

            return rowView;
        }

    }

    public class HistoryItem {
        int rowid = 0;
        String title = "201503281518";
        boolean print = false;

        public HistoryItem(int id, String title, boolean print) {
            this.rowid = id;
            this.title = title;
            this.print = print;
        }
    }
}
