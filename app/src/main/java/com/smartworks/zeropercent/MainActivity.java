package com.smartworks.zeropercent;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_CONTACT = 9001;
    private static final int PERMISSION_SEND_SMS = 9002;
    private static final int PERMISSION_ACCESS_FINE_LOCATION = 9003;
    private View mEnabled;
    private Switch mEnabledSwitch;
    private View mEditMessage;
    private TextView mEditMessageTextView;
    private View mAddLoc;
    private TextView mAddLocTextView;
    private CheckBox mAddLocCheckbox;
    private View mSelectContacts;
    private TextView mSelectContactsTextView;
    private SettingsListAdapter mSettingsListAdapter;
    private ExpandableListView mSettingsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initSettings();
    }

    private void initSettings() {
        final SharedPreferences prefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);

        ViewGroup body = (ViewGroup) findViewById(R.id.activity_main);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        mEnabled = inflater.inflate(R.layout.settings_list_child, null);
        ((ImageView) mEnabled.findViewById(R.id.icon)).setImageResource(R.drawable.ic_battery_alert);
        body.addView(mEnabled, 0);

        mEnabledSwitch = (Switch) mEnabled.findViewById(R.id.setting_switch);
        mEnabledSwitch.setVisibility(View.VISIBLE);
        if (BatteryMonitorService.isRunning) {
            mEnabledSwitch.setChecked(true);
            mEnabledSwitch.setText(R.string.enabled);
        } else {
            mEnabledSwitch.setChecked(false);
            mEnabledSwitch.setText(R.string.disabled);
        }
        mEnabledSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(mEnabledSwitch.isChecked()) {
                    if(ContextCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(getApplicationContext(),
                                    Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                        getPermissions();
                    } else {
                        startService(new Intent(getApplicationContext(), BatteryMonitorService.class));
                        mEnabledSwitch.setText(R.string.enabled);
                        prefs.edit().putBoolean("sent_message", false).apply();
                        Log.d(TAG, "Battery monitoring enabled");
                    }
                } else {
                    stopService(new Intent(getApplicationContext(), BatteryMonitorService.class));
                    mEnabledSwitch.setText(R.string.disabled);
                    Log.d(TAG, "Battery monitoring disabled");
                }
            }
        });

        mEnabled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEnabledSwitch.toggle();
            }
        });

        mEditMessage = inflater.inflate(R.layout.settings_list_child, null);
        ((ImageView) mEditMessage.findViewById(R.id.icon)).setImageResource(R.drawable.ic_message);
        body.addView(mEditMessage, 1);

        mEditMessageTextView = (TextView) mEditMessage.findViewById(R.id.setting_textview);
        mEditMessageTextView.setVisibility(View.VISIBLE);
        mEditMessageTextView.setText(R.string.set_message);
        mEditMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                    startActivity(new Intent(getApplicationContext(), EditMessageActivity.class));
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.sms_perm_denied), Toast.LENGTH_SHORT).show();
                }
            }
        });

        mAddLoc = inflater.inflate(R.layout.settings_list_child, null);
        ((ImageView) mAddLoc.findViewById(R.id.icon)).setImageResource(R.drawable.ic_battery_alert);
        body.addView(mAddLoc, 2);

        mAddLocTextView = (TextView) mAddLoc.findViewById(R.id.setting_textview);
        mAddLocTextView.setVisibility(View.VISIBLE);
        mAddLocTextView.setText(R.string.add_loc);

        mAddLocCheckbox = (CheckBox) mAddLoc.findViewById(R.id.setting_check);
        mAddLocCheckbox.setVisibility(View.VISIBLE);
        mAddLocCheckbox.setChecked(prefs.getBoolean("add_loc", false));
        mAddLocCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    if(ContextCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        mAddLocCheckbox.setChecked(false);
                        getPermissions();
                    }
                    prefs.edit().putBoolean("add_loc", mAddLocCheckbox.isChecked()).apply();
                }
            }
        });

        mAddLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAddLocCheckbox.toggle();
            }
        });

        mSelectContacts = inflater.inflate(R.layout.settings_list_child, null);
        ((ImageView) mSelectContacts.findViewById(R.id.icon)).setImageResource(R.drawable.ic_contact);
        mSelectContacts.findViewById(R.id.hint).setVisibility(View.VISIBLE);
        ((TextView) mSelectContacts.findViewById(R.id.hint)).setText(
                getResources().getQuantityString(R.plurals.selected_contacts,
                        SelectContactsActivity.getSelectedContacts(this).size(),
                        SelectContactsActivity.getSelectedContacts(this).size()));
        body.addView(mSelectContacts, 3);

        mSelectContactsTextView = (TextView) mSelectContacts.findViewById(R.id.setting_textview);
        mSelectContactsTextView.setVisibility(View.VISIBLE);
        mSelectContactsTextView.setText(R.string.select_contacts);
        mSelectContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                    startActivity(new Intent(getApplicationContext(), SelectContactsActivity.class));
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.contacts_perm_denied), Toast.LENGTH_SHORT).show();
                }
            }
        });

        mSettingsListAdapter = new SettingsListAdapter(this);
        mSettingsList = (ExpandableListView) findViewById(R.id.settings_list);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;
        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mSettingsList.setIndicatorBounds(width - GetPixelFromDips(56), width - GetPixelFromDips(8));
        } else {
            mSettingsList.setIndicatorBoundsRelative(width - GetPixelFromDips(56), width - GetPixelFromDips(8));
        }

        mSettingsList.setAdapter(mSettingsListAdapter);
    }

    private int GetPixelFromDips(float pixels) {
        // Get the screen'toggle density scale
        final float scale = getResources().getDisplayMetrics().density;
        // Convert the dps to pixels, based on density scale
        return (int) (pixels * scale + 0.5f);
    }

    private void getPermissions(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(getString(R.string.req_contacts_perm_title));
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setMessage(getString(R.string.req_contacts_perm_message));
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @TargetApi(Build.VERSION_CODES.M)
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            requestPermissions(new String[] {Manifest.permission.READ_CONTACTS}, PERMISSION_REQUEST_CONTACT);
                        }
                    });
                    builder.show();
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_CONTACTS},
                            PERMISSION_REQUEST_CONTACT);
                }
            }
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(getString(R.string.req_sms_perm_title));
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setMessage(getString(R.string.req_sms_perm_message));
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @TargetApi(Build.VERSION_CODES.M)
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            requestPermissions(new String[] {Manifest.permission.SEND_SMS}, PERMISSION_SEND_SMS);
                        }
                    });
                    builder.show();
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.SEND_SMS},
                            PERMISSION_SEND_SMS);
                }
            }
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(getString(R.string.req_loc_perm_title));
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setMessage(getString(R.string.req_loc_perm_message));
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @TargetApi(Build.VERSION_CODES.M)
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ACCESS_FINE_LOCATION);
                        }
                    });
                    builder.show();
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.SEND_SMS},
                            PERMISSION_SEND_SMS);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CONTACT: {
                // If request is cancelled, the result arrays are empty.
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    if(ContextCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                            mEnabledSwitch.setChecked(true);
                    } else {
                        mEnabledSwitch.setChecked(false);
                        Toast.makeText(getApplicationContext(), getString(R.string.sms_perm_denied), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // permission denied
                    mEnabledSwitch.setChecked(false);
                }
                break;
            } case PERMISSION_SEND_SMS: {
                // If request is cancelled, the result arrays are empty.
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    if(ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                        mEnabledSwitch.setChecked(true);
                    } else {
                        mEnabledSwitch.setChecked(false);
                        Toast.makeText(getApplicationContext(), getString(R.string.contacts_perm_denied), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // permission denied
                    mEnabledSwitch.setChecked(false);
                }
                break;
            } case PERMISSION_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    mAddLocCheckbox.setChecked(true);
                } else {
                    mAddLocCheckbox.setChecked(false);
                }
                break;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((TextView) mSelectContacts.findViewById(R.id.hint)).setText(
                getResources().getQuantityString(R.plurals.selected_contacts,
                        SelectContactsActivity.getSelectedContacts(this).size(),
                        SelectContactsActivity.getSelectedContacts(this).size()));
    }
}