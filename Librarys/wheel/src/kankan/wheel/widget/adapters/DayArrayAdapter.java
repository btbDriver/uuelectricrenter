package kankan.wheel.widget.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Calendar;

import kankan.wheel.R;

/**
 * Day adapter
 */
public class DayArrayAdapter extends AbstractWheelTextAdapter {
    // Count of days to be shown
    private int daysCount = 20;

    // Calendar
    Calendar calendar;


    public int getDaysCount() {
        return daysCount;
    }

    public void setDaysCount(int daysCount) {
        this.daysCount = daysCount;
    }

    /**
     * Constructor
     */
    public DayArrayAdapter(Context context, Calendar calendar) {
        super(context, R.layout.time2_day, NO_RESOURCE);
        this.calendar = calendar;

//        setItemTextResource(R.id.time2_monthday);
    }

    @Override
    public View getItem(int index, View cachedView, ViewGroup parent) {
        final int day = index;
        Calendar newCalendar = (Calendar) calendar.clone();
        newCalendar.roll(Calendar.DAY_OF_YEAR, day);

        View view = super.getItem(index, cachedView, parent);
//        TextView weekday = (TextView) view.findViewById(R.id.time2_weekday);
//        if (day == 0) {
//            weekday.setText("");
//        } else {
//            DateFormat format = new SimpleDateFormat("EEE");
//            weekday.setText(format.format(newCalendar.getTime()));
//        }

        TextView monthday = (TextView) view.findViewById(R.id.time2_monthday);
        monthday.setText(formatTime(newCalendar));
//        monthday.setTextColor(0xFF111111);

        return view;
    }

    @Override
    public int getItemsCount() {
        return daysCount + 1;
    }

    @Override
    protected CharSequence getItemText(int index) {
        return "";
    }


    public static final String formatTime(final Calendar calendar) {
        StringBuilder dateBuilder = new StringBuilder();
//        dateBuilder.append(calendar.get(Calendar.YEAR));
//        dateBuilder.append("年");
        int month = calendar.get(Calendar.MONTH) + 1;
        dateBuilder.append(month < 10 ? "0" + month : month);
        dateBuilder.append("月");
        dateBuilder.append(calendar.get(Calendar.DAY_OF_MONTH) < 10 ? "0" + calendar.get(Calendar.DAY_OF_MONTH) : calendar.get(Calendar.DAY_OF_MONTH));
        dateBuilder.append("日");
        dateBuilder.append("(周");
        switch (calendar.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.SUNDAY:
                dateBuilder.append("日");
                break;
            case Calendar.MONDAY:
                dateBuilder.append("一");
                break;
            case Calendar.TUESDAY:
                dateBuilder.append("二");
                break;
            case Calendar.WEDNESDAY:
                dateBuilder.append("三");
                break;
            case Calendar.THURSDAY:
                dateBuilder.append("四");
                break;
            case Calendar.FRIDAY:
                dateBuilder.append("五");
                break;
            case Calendar.SATURDAY:
                dateBuilder.append("六");
                break;
        }
        dateBuilder.append(")");
        return dateBuilder.toString();
    }
}
