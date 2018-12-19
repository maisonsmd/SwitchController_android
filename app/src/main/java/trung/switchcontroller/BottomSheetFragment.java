package trung.switchcontroller;

import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

class Timer {
    private static final String TAG = Timer.class.getSimpleName();

    int hour;
    int minute;
    boolean isEnabled;

    void parseArray(int[] arr) {
        hour = arr[0];
        minute = arr[1];
    }
    //format: 0-00:00
    void parseString(String s){
        s = s.trim();
        String[] s1 = s.split("-");
        if(s1.length == 2){
            isEnabled = Integer.parseInt(s1[0]) == 1;
            String[] s2 = s1[1].split(":");
            if(s2.length == 2){
                hour = Integer.parseInt(s2[0]);
                minute = Integer.parseInt(s2[1]);

                Log.i(TAG, "parsing: " + isEnabled + ", " + toString());
            }
        }
    }

    int[] getArray() {
        return new int[]{hour, minute};
    }

    @NonNull
    @Override
    public String toString() {
        return "" + (hour < 10 ? "0" : "") + hour + ":" + (minute < 10 ? "0" : "") + minute;
    }
}

public class BottomSheetFragment extends BottomSheetDialogFragment {

    private static final String TAG = BottomSheetFragment.class.getSimpleName();

    public static final String BOTTOM_SHEET_INTENT_FILTER = "BOTTOM_SHEET_INTENT_FILTER";
    Timer timeOn = new Timer(), timeOff = new Timer();

    TextView textViewTimeOn;
    TextView textViewTimeOff;
    CheckBox checkBoxOn;
    CheckBox checkBoxOff;

    public BottomSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_bottom_sheet_dialog, container, false);

        int[] timeOnArr = getArguments().getIntArray(getString(R.string.KEY_TIME_ON));
        boolean timeOnEnable = getArguments().getBoolean(getString(R.string.KEY_TIME_ON_ENABLE));
        int[] timeOffArr = getArguments().getIntArray(getString(R.string.KEY_TIME_OFF));
        boolean timeOffEnable = getArguments().getBoolean(getString(R.string.KEY_TIME_OFF_ENABLE));

        timeOn.parseArray(timeOnArr);
        timeOn.isEnabled = timeOnEnable;
        timeOff.parseArray(timeOffArr);
        timeOff.isEnabled = timeOffEnable;

        textViewTimeOn = view.findViewById(R.id.textViewTimeOn);
        textViewTimeOff = view.findViewById(R.id.textViewTimeOff);
        checkBoxOn = view.findViewById(R.id.checkBoxEnableOn);
        checkBoxOff = view.findViewById(R.id.checkBoxEnableOff);

        textViewTimeOn.setText(timeOn.toString());
        textViewTimeOff.setText(timeOff.toString());
        checkBoxOn.setChecked(timeOn.isEnabled);
        checkBoxOff.setChecked(timeOff.isEnabled);

        view.findViewById(R.id.buttonSaveTimer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(BOTTOM_SHEET_INTENT_FILTER);
                intent.putExtra(getString(R.string.KEY_TIME_ON), timeOn.getArray());
                intent.putExtra(getString(R.string.KEY_TIME_ON_ENABLE), timeOn.isEnabled);
                intent.putExtra(getString(R.string.KEY_TIME_OFF), timeOff.getArray());
                intent.putExtra(getString(R.string.KEY_TIME_OFF_ENABLE), timeOff.isEnabled);

                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);

                dismiss();
            }
        });

        checkBoxOn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                timeOn.isEnabled = isChecked;
            }
        });
        checkBoxOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                timeOff.isEnabled = isChecked;
            }
        });


        textViewTimeOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open time picker dialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),R.style.Theme_AppCompat_DayNight_Dialog,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                timeOn.hour = hourOfDay;
                                timeOn.minute = minute;
                                textViewTimeOn.setText(timeOn.toString());
                            }
                        }, timeOn.hour, timeOn.minute, true);
                timePickerDialog.show();
            }
        });

        textViewTimeOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open time picker dialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), R.style.Theme_AppCompat_DayNight_Dialog,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                timeOff.hour = hourOfDay;
                                timeOff.minute = minute;
                                textViewTimeOff.setText(timeOff.toString());
                            }
                        }, timeOff.hour, timeOff.minute, true);
                timePickerDialog.show();
            }
        });

        return view;
    }
}
