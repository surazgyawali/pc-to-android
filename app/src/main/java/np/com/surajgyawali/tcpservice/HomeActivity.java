package np.com.surajgyawali.tcpservice;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;


public class HomeActivity extends AppCompatActivity {

    Switch mServerStartSwitch, mAutoStartSwitch;
    EditText mEditTextPort, mEditTextPassword;
    SharedPreferences.Editor prefsEditor;
    SharedPreferences sharedPreferences;
    public static final String USER_DATA_PREFS_NAME = "UserPrefs";
    boolean serverSwitchOn,autoStartSwitchOn;
    String mPassword;
    Integer mPort;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        prefsEditor       = getSharedPreferences(USER_DATA_PREFS_NAME, MODE_PRIVATE).edit();
        sharedPreferences = getSharedPreferences(USER_DATA_PREFS_NAME, MODE_PRIVATE);
        serverSwitchOn    = sharedPreferences.getBoolean("server_switch",false);
        autoStartSwitchOn = sharedPreferences.getBoolean("auto_start_switch",false);
        mPassword         = sharedPreferences.getString("password","mySecret");
        mPort             = sharedPreferences.getInt("port",8848);

        mServerStartSwitch = findViewById(R.id.switch_toggle_service);
        mAutoStartSwitch = findViewById(R.id.switch_toggle_auto_start);
        mEditTextPort = findViewById(R.id.et_port);
        mEditTextPassword = findViewById(R.id.et_password);
        mEditTextPassword.setText(mPassword);
        mEditTextPort.setText(mPort.toString());

        if (serverSwitchOn) {
            mServerStartSwitch.setChecked(true);
            mServerStartSwitch.setText("Service is ON");

        } else {
            mServerStartSwitch.setChecked(false);
            mServerStartSwitch.setText("Service is OFF");
        }

        if (autoStartSwitchOn) {
            mAutoStartSwitch.setChecked(true);
            mAutoStartSwitch.setText("App will start automatically on startup.");

        } else {
            mAutoStartSwitch.setChecked(false);
            mAutoStartSwitch.setText("App won't start automatically on startup.");

        }

        mServerStartSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {

                    mServerStartSwitch.setText("Service is ON");

                } else {
                    mServerStartSwitch.setText("Service is OFF");
                }

                prefsEditor.putBoolean("server_switch",isChecked);
                prefsEditor.apply();
                prefsEditor.commit();
            }
        });


        mAutoStartSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {

                    mAutoStartSwitch.setText("App will start automatically on startup.");

                } else {
                    mAutoStartSwitch.setText("App won't start automatically on startup.");
                }

                prefsEditor.putBoolean("auto_start_switch",isChecked);
                prefsEditor.apply();
                prefsEditor.commit();

            }
        });


        mEditTextPassword.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });

//        mButtonStartServer.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                if (validateSubmitData()) {
////
////                    prefsEditor.putInt("port", Integer.parseInt(mEditTextPort.getText().toString()));
////                    prefsEditor.putString("password", mEditTextPassword2.getText().toString());
////                    prefsEditor.apply();
////                        String password = sharedPreferences.getString("password", null);
////                        int port = sharedPreferences.getInt("port", 8848);
//////                        Log.d("password: " , password );
////                        Log.d("port", Integer.toString(port));
////
////                    Intent serverIntent = new Intent(HomeActivity.this, ServerActivity.class);
////                    startActivity(serverIntent);
////                }
////
//            }
//        });

//        mEditTextPassword2.addTextChangedListener(new TextWatcher() {
//            public void afterTextChanged(Editable s) {
//                String strPass1 = mEditTextPassword1.getText().toString();
//                String strPass2 = mEditTextPassword2.getText().toString();
//
//                if (!strPass1.equals(strPass2)) {
//
//                    mEditTextPassword2.setError(getResources().getString(R.string.password_not_match));
//
//                }
//            }


//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            }
//
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//            }
//        });


    }

    private boolean validateSubmitData() {
        boolean portValid = true;
        boolean passwordValid = true;
        final String strPass = mEditTextPassword.getText().toString();
        final String portData = mEditTextPort.getText().toString();

        if (!portData.trim().isEmpty()) {
            if (Integer.parseInt(portData) > 65535) {
                portValid = false;
                mEditTextPort.setError("Port must be smaller than 65535");
            }
        }

        if (strPass.trim().isEmpty()) {
            passwordValid = false;
            mEditTextPassword.setError("Passwords can't be empty");

        }


        return portValid && passwordValid;
    }
}
