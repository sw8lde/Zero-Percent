package com.smartworks.zeropercent;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ComponentName;
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
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_CONTACT = 9001;
    private View mEnabled;
    private Switch mEnabledSwitch;
    private View mSetMessage;
    private TextView mSetMessageTextView;
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
        SharedPreferences prefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefs.edit();

        ViewGroup body = (ViewGroup) findViewById(R.id.activity_main);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        mEnabled = inflater.inflate(R.layout.settings_list_child, null);
        ((ImageView) mEnabled.findViewById(R.id.icon)).setImageResource(R.drawable.ic_battery_alert);
        body.addView(mEnabled, 0);

        mEnabledSwitch = (Switch) mEnabled.findViewById(R.id.setting_switch);
        mEnabledSwitch.setVisibility(View.VISIBLE);
        mEnabledSwitch.setChecked(prefs.getBoolean("enabled", false));

        mEnabled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEnabledSwitch.toggle();
                if (mEnabledSwitch.isChecked()) {
                    editor.putBoolean("enabled", true);
                    mEnabledSwitch.setText(R.string.enabled);
                    mEnabledSwitch.setTextColor(ContextCompat.getColor(v.getContext(), R.color.textPrimary));

                    PackageManager pm  = getApplicationContext().getPackageManager();
                    ComponentName componentName = new ComponentName(getApplicationContext(), BatteryMonitorReceiver.class);
                    pm.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                            PackageManager.DONT_KILL_APP);
                    Log.d(TAG, "Battery monitoring enabled");
                } else {
                    editor.putBoolean("false", true);
                    mEnabledSwitch.setText(R.string.disabled);
                    mEnabledSwitch.setTextColor(ContextCompat.getColor(v.getContext(), R.color.textDisabled));

                    PackageManager pm  = getApplicationContext().getPackageManager();
                    ComponentName componentName = new ComponentName(getApplicationContext(), BatteryMonitorReceiver.class);
                    pm.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            PackageManager.DONT_KILL_APP);
                    Log.d(TAG, "Battery monitoring disabled");
                }
                editor.apply();
            }
        });

        mSetMessage = inflater.inflate(R.layout.settings_list_child, null);
        ((ImageView) mSetMessage.findViewById(R.id.icon)).setImageResource(R.drawable.ic_message);
        body.addView(mSetMessage, 1);

        mSetMessageTextView = (TextView) mSetMessage.findViewById(R.id.setting_textview);
        mSetMessageTextView.setVisibility(View.VISIBLE);
        mSetMessageTextView.setText(R.string.set_message);
        mSetMessageTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        mSelectContacts = inflater.inflate(R.layout.settings_list_child, null);
        ((ImageView) mSelectContacts.findViewById(R.id.icon)).setImageResource(R.drawable.ic_contact);
        mSelectContacts.findViewById(R.id.hint).setVisibility(View.VISIBLE);
        ((TextView) mSelectContacts.findViewById(R.id.hint)).setText(
                getResources().getQuantityString(R.plurals.selected_contacts,
                        SelectContactsActivity.getSelectedContacts(this).size(),
                        SelectContactsActivity.getSelectedContacts(this).size()));
        body.addView(mSelectContacts, 2);

        mSelectContactsTextView = (TextView) mSelectContacts.findViewById(R.id.setting_textview);
        mSelectContactsTextView.setVisibility(View.VISIBLE);
        mSelectContactsTextView.setText(R.string.select_contacts);
        mSelectContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestContactPermission();
                if(ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                    startActivity(new Intent(getApplicationContext(), SelectContactsActivity.class));
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.perm_denied), Toast.LENGTH_SHORT).show();
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

    private void requestContactPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_CONTACTS)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(getString(R.string.req_perm_title));
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setMessage(getString(R.string.req_perm_message));
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @TargetApi(Build.VERSION_CODES.M)
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            requestPermissions(
                                    new String[]
                                            {Manifest.permission.READ_CONTACTS}
                                    , PERMISSION_REQUEST_CONTACT);
                        }
                    });
                    builder.show();
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_CONTACTS},
                            PERMISSION_REQUEST_CONTACT);
                }
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