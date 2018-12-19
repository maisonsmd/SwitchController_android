package trung.switchcontroller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ToggleButton;


public class ButtonsFragment extends Fragment {

    private static final String TAG = ButtonsFragment.class.getSimpleName();
    private OnFragmentInteractionListener mListener;
    public static final String BOTTOM_SHEET_INTENT_FILTER = "BOTTOM_SHEET_INTENT_FILTER";
    public Timer[] timeOn = new Timer[4], timeOff = new Timer[4];
    //current device that user just click and hold button
    int currentDevice = 0;
    boolean isViewCreated = false;
    ToggleButton[] mButton = new ToggleButton[4];
    TextView mTextViewTemperature;
    TextView mTextViewDeviceTime;

    public ButtonsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        for (int i = 0; i < 4; i++) {
            timeOn[i] = new Timer();
            timeOff[i] = new Timer();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_buttons, container, false);
        mButton[0] = view.findViewById(R.id.btnToggle1);
        mButton[1] = view.findViewById(R.id.btnToggle2);
        mButton[2] = view.findViewById(R.id.btnToggle3);
        mButton[3] = view.findViewById(R.id.btnToggle4);
        mTextViewTemperature = view.findViewById(R.id.textViewTemperature);
        mTextViewDeviceTime = view.findViewById(R.id.textViewDeviceTime);

        mButton[0].setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                currentDevice = 0;
                openBottomSheetDialog();
                return true;
            }
        });
        mButton[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onDataChanged(0);
            }
        });

        mButton[1].setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                currentDevice = 1;
                openBottomSheetDialog();
                return true;
            }
        });
        mButton[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onDataChanged(1);
            }
        });

        mButton[2].setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                currentDevice = 2;
                openBottomSheetDialog();
                return true;
            }
        });
        mButton[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onDataChanged(2);
            }
        });

        mButton[3].setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                currentDevice = 3;
                openBottomSheetDialog();
                return true;
            }
        });
        mButton[3].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onDataChanged(3);
            }
        });

        isViewCreated = true;
        return view;
    }

    void openBottomSheetDialog() {
        BottomSheetFragment bottomSheetFragment = new BottomSheetFragment();
        Bundle data = new Bundle();
        data.putIntArray(getString(R.string.KEY_TIME_ON), timeOn[currentDevice].getArray());
        data.putBoolean(getString(R.string.KEY_TIME_ON_ENABLE), timeOn[currentDevice].isEnabled);
        data.putIntArray(getString(R.string.KEY_TIME_OFF), timeOff[currentDevice].getArray());
        data.putBoolean(getString(R.string.KEY_TIME_OFF_ENABLE), timeOff[currentDevice].isEnabled);
        bottomSheetFragment.setArguments(data);
        bottomSheetFragment.show(getActivity().getSupportFragmentManager(), bottomSheetFragment.getTag());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    interface OnFragmentInteractionListener {
        //void onFragmentInteraction(Uri uri);
        void onDataChanged(int timerIndex);
    }

    private BroadcastReceiver bottomSheetBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int[] timeOnArr = intent.getExtras().getIntArray(getString(R.string.KEY_TIME_ON));
            int[] timeOffArr = intent.getExtras().getIntArray(getString(R.string.KEY_TIME_OFF));
            boolean timeOnEnable = intent.getExtras().getBoolean(getString(R.string.KEY_TIME_ON_ENABLE));
            boolean timeOffEnable = intent.getExtras().getBoolean(getString(R.string.KEY_TIME_OFF_ENABLE));

            timeOn[currentDevice].parseArray(timeOnArr);
            timeOn[currentDevice].isEnabled = timeOnEnable;
            timeOff[currentDevice].parseArray(timeOffArr);
            timeOff[currentDevice].isEnabled = timeOffEnable;

            mListener.onDataChanged(currentDevice);
        }
    };

    @Override
    public void onResume() {
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(bottomSheetBroadcastReceiver, new IntentFilter(BOTTOM_SHEET_INTENT_FILTER));
        super.onResume();
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(bottomSheetBroadcastReceiver);
        super.onPause();
    }
}
