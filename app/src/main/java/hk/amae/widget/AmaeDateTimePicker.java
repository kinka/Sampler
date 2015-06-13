package hk.amae.widget;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

import hk.amae.util.Comm;

/**
 * Created by kinka on 4/4/15.
 */
public class AmaeDateTimePicker {

    public interface Picker {
        void onPick(String value);
    }

    public static void showDateDialog(Context context, final TextView textView, final String format, String style) {
        showDateDialog(context, textView, format, style, null);
    }

    public static void showDateDialog(Context context, final TextView textView, final String format, String style, final Picker picker) {
        int year = 0;
        int month = 0;
        int day = 1;
        Calendar calendar = Calendar.getInstance();

        try {
            String oldDate = textView.getText().toString();
            int posY = 0, posM = 0, posD = 0;
            if (style == null || style.equals("年月日")) {
                posY = oldDate.indexOf("年");
                posM = oldDate.indexOf("月");
                posD = oldDate.indexOf("日");
            } else {
                posY = oldDate.indexOf("-");
                posM = oldDate.lastIndexOf("-");
                posD = oldDate.length();
            }
            year = Integer.valueOf(oldDate.substring(0, posY).trim());
            month = Integer.valueOf(oldDate.substring(posY + 1, posM).trim()) - 1;
            day = Integer.valueOf(oldDate.substring(posM + 1, posD).trim());
        } catch (Exception e) {
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
            day = calendar.get(Calendar.DAY_OF_MONTH);
        }

        new DatePickerDialog(context,
            new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int y, int m, int d) {
                    String value = String.format(format, y, m + 1, d);
                    textView.setText(value);
                    Comm.hideSoftInput();
                    if (picker != null)
                        picker.onPick(value);
                }
            }, year, month, day).show();
    }

    public static void showTimeDialog(Context context, final TextView textView, final String format) {
        showTimeDialog(context, textView, format, null);
    }
    public static void showTimeDialog(Context context, final TextView textView, final String format, final Picker picker) {
        int hour = 0;
        int minute = 0;
        int second = 0;
        Calendar calendar = Calendar.getInstance();

        try {
            String oldTime = textView.getText().toString();
            int pos1 = oldTime.indexOf(":");
            int pos2 = oldTime.lastIndexOf(":");
            hour = Integer.valueOf(oldTime.substring(0, pos1).trim());
            minute = Integer.valueOf(oldTime.substring(pos1+1, pos2).trim());
            second = Integer.valueOf(oldTime.substring(pos2+1).trim());
        } catch (Exception e) {
            hour = calendar.get(Calendar.HOUR_OF_DAY);
            minute = calendar.get(Calendar.MINUTE);
        }
        final int s = second;

        new TimePickerDialog(context,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int h, int m) {
                        String value = String.format(format, h, m, s);
                        textView.setText(value);
                        Comm.hideSoftInput();
                        if (picker != null)
                            picker.onPick(value);
                    }
                }, hour, minute, true).show();
    }

}
