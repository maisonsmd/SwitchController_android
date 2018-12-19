package trung.switchcontroller;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ButtonsFragment.OnFragmentInteractionListener,
        SettingsFragment.OnFragmentInteractionListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 9001;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    SettingsFragment mSettingsFragment;
    ButtonsFragment mButtonsFragment;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference mUserReference;
    DatabaseReference mCurrentUserReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ViewPagerAdapter mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        mButtonsFragment = new ButtonsFragment();
        mSettingsFragment = new SettingsFragment();
        mViewPagerAdapter.addFragment(mButtonsFragment, getResources().getString(R.string.tab_control_title));
        mViewPagerAdapter.addFragment(mSettingsFragment, getResources().getString(R.string.tab_settings_text));

        ViewPager mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mViewPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.firebase_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();



        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {Manifest.permission.INTERNET};

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateUI(null);
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
        //mUserReference.child("setclock").setValue( "" + System.currentTimeMillis() / 1000);
    }

    void parseDatabaseContent(String data, int deviceIndex) {
        if (!data.contains(">")) {
            return;
        }
        Log.i(TAG, data);
        String[] strings = data.split(">");
        for (String s : strings) {
            Log.i(TAG, s);
        }
        if (strings.length == 3) {
            if (mButtonsFragment.isViewCreated) {
                mButtonsFragment.mButton[deviceIndex].setChecked(Integer.parseInt(strings[0]) == 1);
                mButtonsFragment.timeOn[deviceIndex].parseString(strings[1]);
                mButtonsFragment.timeOff[deviceIndex].parseString(strings[2]);
            }
        }
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Log.i(TAG, "username: " + user.getDisplayName());
            Log.i(TAG, "uid: " + user.getUid());
        }
        mSettingsFragment.updateUI(user);

        if (user != null) {
            mUserReference = database.getReference("users/" + user.getUid());
            mCurrentUserReference = database.getReference("current");

            mUserReference.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    String key = dataSnapshot.getKey();
                    assert key != null;
                    if (key.contains("device")) {
                        String content = dataSnapshot.getValue(String.class);
                        if (key.contains("device0"))
                            parseDatabaseContent(content, 0);
                        else if (key.contains("device1"))
                            parseDatabaseContent(content, 1);
                        else if (key.contains("device2"))
                            parseDatabaseContent(content, 2);
                        else if (key.contains("device3"))
                            parseDatabaseContent(content, 3);
                    }
                    if(key.contains("temp")){
                        float temp = dataSnapshot.getValue(Float.class);
                        if(mButtonsFragment.mTextViewTemperature != null){
                            String numberAsString = String.format ("%.1f *C", temp);
                            mButtonsFragment.mTextViewTemperature.setText(numberAsString);
                        }
                    }
                    if(key.contains("clock") && !key.contains("setclock")){
                        String clock = dataSnapshot.getValue(String.class);
                        if(mButtonsFragment.mTextViewDeviceTime != null){
                            mButtonsFragment.mTextViewDeviceTime.setText(clock);
                        }
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    String key = dataSnapshot.getKey();
                    assert key != null;
                    if (key.contains("device")) {
                        String content = dataSnapshot.getValue(String.class);
                        if (key.contains("device0"))
                            parseDatabaseContent(content, 0);
                        else if (key.contains("device1"))
                            parseDatabaseContent(content, 1);
                        else if (key.contains("device2"))
                            parseDatabaseContent(content, 2);
                        else if (key.contains("device3"))
                            parseDatabaseContent(content, 3);
                    }
                    if(key.contains("temp")){
                        float temp = dataSnapshot.getValue(Float.class);
                        if(mButtonsFragment.mTextViewTemperature != null){
                            String numberAsString = String.format ("%.1f *C", temp);
                            mButtonsFragment.mTextViewTemperature.setText(numberAsString);
                        }
                    }
                    if(key.contains("clock")){
                        String clock = dataSnapshot.getValue(String.class);
                        if(mButtonsFragment.mTextViewDeviceTime != null){
                            mButtonsFragment.mTextViewDeviceTime.setText(clock);
                        }
                    }
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        } else {
            mUserReference = null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.e(TAG, "Google sign in failed", e);
                updateUI(null);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            Toast.makeText(getApplicationContext(), "signed in", Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                            mCurrentUserReference.child("user").setValue("" + mAuth.getCurrentUser().getUid());

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "authentication failed", Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    public static boolean hasPermissions(Context context, String... permissions)
    {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null)
        {
            for (String permission : permissions)
            {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED)
                {
                    return false;
                }
            }
        }
        return true;
    }
    @Override
    public void OnSignInRequest() {
        signIn();
    }

    @Override
    public void OnSignOutRequest() {
        signOut();
    }

    @Override
    public void onDataChanged(int index) {
        if (mAuth.getCurrentUser() == null)
            return;

        String s = "";
        try {
            s += mButtonsFragment.mButton[index].isChecked() ? 1 : 0;
            s += ">";
            s += mButtonsFragment.timeOn[index].isEnabled ? 1 : 0;
            s += "-";
            s += mButtonsFragment.timeOn[index].toString();
            s += ">";
            s += mButtonsFragment.timeOff[index].isEnabled ? 1 : 0;
            s += "-";
            s += mButtonsFragment.timeOff[index].toString();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }

        String deviceName = "device" + index;
        mUserReference.child(deviceName).setValue(s);
        //mUserReference.child("updateRq").setValue(index + 1);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

}
