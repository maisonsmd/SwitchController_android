package trung.switchcontroller;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import com.google.firebase.auth.FirebaseUser;

public class SettingsFragment extends Fragment {
    private static final String TAG = SettingsFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;
    Button mButtonSignInOut;
    EditText mEditTextUID;
    TextView mTextViewUserName;
    boolean mIsUserSignedIn = false;
    boolean mIsViewCreated = false;
    ImageView mAvatar;
    TextView mEmail;
    FirebaseUser mUser;
    View mView;
    String uidString = "";

    public SettingsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_settings, container, false);

        mButtonSignInOut = mView.findViewById(R.id.btnSignInOut);
        mTextViewUserName = mView.findViewById(R.id.textViewUserName);
        mEditTextUID = mView.findViewById(R.id.editTextUID);
        mAvatar = mView.findViewById(R.id.imageViewUserAvatar);
        mEmail = mView.findViewById(R.id.textViewEmail);

        Bitmap avatar = BitmapFactory.decodeResource(getResources(), R.raw.user);
        mAvatar.setImageBitmap(avatar);
        mEditTextUID.setInputType(InputType.TYPE_NULL);

        mButtonSignInOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsUserSignedIn)
                    mListener.OnSignOutRequest();
                else
                    mListener.OnSignInRequest();
            }
        });

        //re-print UID just user did cut it
        mEditTextUID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "click on UID");
                mEditTextUID.setText(uidString);
            }
        });
        //force update UI
        mIsViewCreated = true;
        updateUI(mUser);

        return mView;
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

    public void updateUI(FirebaseUser user) {
        Log.d(TAG, "requesting to update UI with user == null: " + (user == null));
        mUser = user;
        mIsViewCreated = (mView != null);
        if (!mIsViewCreated)
            Log.e(TAG, "view is not created!");

        if (user == null) {
            mIsUserSignedIn = false;

            if (mIsViewCreated) {
                mEditTextUID.setText("");
                mTextViewUserName.setText("NOT");
                mEmail.setText("signed in");
                mButtonSignInOut.setText("sign in");
                Bitmap avatar = BitmapFactory.decodeResource(getResources(), R.raw.user);
                mAvatar.setImageBitmap(avatar);
            }
        } else {
            mIsUserSignedIn = true;
            uidString = user.getUid();

            if (mIsViewCreated) {
                mEditTextUID.setText(user.getUid());
                mTextViewUserName.setText(user.getDisplayName());
                mButtonSignInOut.setText("sign out");
                mEmail.setText(user.getEmail());

                try {
                    Glide.with(requireContext())
                            .load(user.getPhotoUrl())
                            .into(mAvatar);
                } catch (Exception e) {
                    Log.e("images", e.toString());
                }

            }
        }
    }


    interface OnFragmentInteractionListener {
        void OnSignInRequest();

        void OnSignOutRequest();
    }
}
