package np.com.surajgyawali.tcpservice;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import java.net.ServerSocket;

public class HomeActivity extends AppCompatActivity {

    Switch mServerStartSwitch, mAutoStartSwitch;
    TextView mIpTextView, mEditTextPort, mEditTextPassword;

    SharedPreferences.Editor prefsEditor;
    SharedPreferences sharedPreferences;

    public static final String USER_DATA_PREFS_NAME = "UserPrefs";
    boolean serverSwitchOn, autoStartSwitchOn;

    String mPassword;
    Utilities utilities = new Utilities();
//    Integer mPort;
    ServerSocket serverSocket;
    String message = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        prefsEditor = getSharedPreferences(USER_DATA_PREFS_NAME, MODE_PRIVATE).edit();
        sharedPreferences = getSharedPreferences(USER_DATA_PREFS_NAME, MODE_PRIVATE);
        serverSwitchOn = sharedPreferences.getBoolean("server_switch", false);
        autoStartSwitchOn = sharedPreferences.getBoolean("auto_start_switch", false);
        mPassword = utilities.getUserPassword(sharedPreferences);

        mServerStartSwitch = findViewById(R.id.switch_toggle_service);
        mAutoStartSwitch = findViewById(R.id.switch_toggle_auto_start);
        mEditTextPort = findViewById(R.id.tv_port);
        mEditTextPassword = findViewById(R.id.tv_password);
        mIpTextView = findViewById(R.id.tv_ip);

        mEditTextPassword.setText(mPassword);
        mEditTextPort.setText(utilities.getUserPort(sharedPreferences).toString());
        mIpTextView.setText(utilities.getIPAddress(true));
//        socketServerThread = new Thread(new SocketServerThread());

        if (utilities.checkWifiOnAndConnected(this)) {

            mIpTextView.setText(utilities.getIPAddress(true));

        } else {

            mIpTextView.setText("Not connected to a network.");

        }


        if (serverSwitchOn) {
            mServerStartSwitch.setChecked(true);
            mServerStartSwitch.setText("Service is ON");
            startService();

        } else {
            mServerStartSwitch.setChecked(false);
            mServerStartSwitch.setText("Service is OFF");
//            closeServerThread();
            stopService();
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

                        startService();
                    mServerStartSwitch.setText("Service is ON");

                } else {

                     stopService();
                    mServerStartSwitch.setText("Service is OFF");
                }

                prefsEditor.putBoolean("server_switch", isChecked);
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

                prefsEditor.putBoolean("auto_start_switch", isChecked);
                prefsEditor.apply();
                prefsEditor.commit();

            }
        });


        mEditTextPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditPasswordDialog();

            }
        });

        mEditTextPort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditPortDialog();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



    private void showEditPasswordDialog() {

        final AlertDialog.Builder passwordDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(HomeActivity.this, android.R.style.Theme_DeviceDefault_Dialog));

        final EditText editPassword = new EditText(HomeActivity.this);
        editPassword.setTextColor(getResources().getColor(R.color.colorAccent));
        editPassword.setHighlightColor(getResources().getColor(R.color.dark));
        passwordDialogBuilder.setTitle("Edit Password");
        passwordDialogBuilder.setMessage("Enter new password:");
        passwordDialogBuilder.setView(editPassword);
        passwordDialogBuilder.setCancelable(false);


        passwordDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                String password = editPassword.getText().toString();

                if (password.trim().isEmpty()) {
                    Toast.makeText(HomeActivity.this, "Passwords can't be empty.", Toast.LENGTH_SHORT).show();
                    return;
                }

                prefsEditor.putString("password", password);
                mEditTextPassword.setText(password);
                prefsEditor.commit();
            }

        });


        passwordDialogBuilder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

        passwordDialogBuilder.create().show();

    }


    private void showEditPortDialog() {

        final AlertDialog.Builder portDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(HomeActivity.this, android.R.style.Theme_DeviceDefault_Dialog));

        final EditText editPort = new EditText(HomeActivity.this);
        editPort.setTextColor(getResources().getColor(R.color.colorAccent));
        editPort.setHighlightColor(getResources().getColor(R.color.dark));
        editPort.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);
        editPort.setHint("0-65535");
        editPort.setHintTextColor(getResources().getColor(R.color.colorDimWhite));
        portDialogBuilder.setTitle("Edit Port");
        portDialogBuilder.setMessage("Enter new port:");
        portDialogBuilder.setView(editPort);
        portDialogBuilder.setCancelable(false);


        portDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                String port = editPort.getText().toString();

                if (!port.trim().isEmpty()) {

                    if (Integer.parseInt(port) > 65535) {
                        Toast.makeText(HomeActivity.this, "Port must be smaller than 65535", Toast.LENGTH_SHORT).show();
                        return;
                    }

                }

                if (port.trim().isEmpty()) {
                    Toast.makeText(HomeActivity.this, "Port can't be empty.", Toast.LENGTH_SHORT).show();
                    return;
                }

                prefsEditor.putInt("port", Integer.parseInt(port));
                mEditTextPort.setText(port);
                prefsEditor.commit();
                stopService();
                startService();
            }

        });


        portDialogBuilder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

        portDialogBuilder.create().show();

    }

    public void startService() {
        startService(new Intent(getBaseContext(), NetworkService.class));
    }

    // Method to stop the service
    public void stopService() {
        stopService(new Intent(getBaseContext(), NetworkService.class));
    }
}
