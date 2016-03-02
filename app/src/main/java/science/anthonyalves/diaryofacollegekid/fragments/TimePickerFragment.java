package science.anthonyalves.diaryofacollegekid.fragments;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import java.util.Calendar;

/**
 * DialogFragment that displays a clock to pick a time
 * Retrieved help from http://developer.android.com/guide/topics/ui/controls/pickers.html
 */
public class TimePickerFragment extends DialogFragment {

    TimePickerDialog.OnTimeSetListener callback;

    /**
     * Constructor
     * @param callback - callback function when a time has been selected
     */
    public TimePickerFragment(TimePickerDialog.OnTimeSetListener callback) {
        this.callback = callback;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), callback, hour, minute, DateFormat.is24HourFormat(getActivity()));
    }
}