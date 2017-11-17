package kankan.wheel.widget.adapters;

import android.content.Context;

/**
 * Created by taurusxi on 14/10/22.
 */
public class SpeedAdapter extends NumericWheelAdapter {

    // Items step value
    private int step;

    /**
     * Constructor
     */
    public SpeedAdapter(Context context, int maxValue, int step) {
        super(context, 0, maxValue / step);
        this.step = step;

//        setItemResource(R.layout.wheel_text_item);
//        setItemTextResource(R.id.text);
    }

    /**
     * Sets units
     */
    public void setUnits(String units) {
        //this.units = units;
    }

    @Override
    public CharSequence getItemText(int index) {
        if (index >= 0 && index < getItemsCount()) {
            int value = index * step;
            if (value < 10) {
                return "0" + Integer.toString(value);
            }
            return Integer.toString(value);
        }
        return null;
    }

}
