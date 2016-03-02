package science.anthonyalves.diaryofacollegekid.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import java.util.Calendar;

/**
 * DialogFragment that displays a calender to pick a date
 * Retrieved help from http://developer.android.com/guide/topics/ui/controls/pickers.html
 */
public class DatePickerFragment extends DialogFragment {


    DatePickerDialog.OnDateSetListener callback;

    /**
     * Constructor
     * @param callback - callback function when a date has been selected
     */
    public DatePickerFragment(DatePickerDialog.OnDateSetListener callback) {
        this.callback = callback;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), callback, year, month, day);
    }
}