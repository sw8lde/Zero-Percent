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
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final int PERMISSION_REQUEST_CONTACT = 9001;
    private static final int PERMISSION_SEND_SMS = 9002;
    private static final int PERMISSION_ACCESS_FINE_LOCATION = 9003;

    private Switch mAutoStartSwitch;
    private Switch mEnabledSwitch;
    private TextView mEnabledText;
    private CheckBox mAddLocCheckbox;

    private boolean settingsExpanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(getApplicationContext(), getString(R.string.app_ad_id));
        AdView bannerAd = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(getString(R.string.test_device))
                .build();
        bannerAd.loadAd(adRequest);

        initSettings();
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
            } else if(ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
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
            } else if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            PERMISSION_ACCESS_FINE_LOCATION);
                }
            }
        }
    }

    private void initSettings() {
        final SharedPreferences prefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);

        // enabled
        mEnabledSwitch = (Switch) findViewById(R.id.enabled_switch);
        mEnabledText = (TextView) findViewById(R.id.enabled_text);

        if (BatteryMonitorService.isRunning) {
            mEnabledSwitch.setChecked(true);
            mEnabledText.setText(R.string.enabled);
        } else {
            mEnabledSwitch.setChecked(false);
            mEnabledText.setText(R.string.disabled);
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
                        mEnabledText.setText(R.string.enabled);
                        prefs.edit().putBoolean("sent_message", false).apply();
                        Log.d(TAG, "Battery monitoring enabled");
                    }
                } else {
                    stopService(new Intent(getApplicationContext(), BatteryMonitorService.class));
                    mEnabledText.setText(R.string.disabled);
                    Log.d(TAG, "Battery monitoring disabled");
                }
            }
        });

        findViewById(R.id.enabled_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEnabledSwitch.toggle();
            }
        });

        // set message
        findViewById(R.id.set_message_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                    showEditMessageDialog(prefs);
                } else {
                    getPermissions();
                }
            }
        });

        // location
        mAddLocCheckbox = (CheckBox) findViewById(R.id.add_loc_checkbox);
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

        findViewById(R.id.add_loc_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAddLocCheckbox.toggle();
            }
        });

        // select contacts
        ((TextView) findViewById(R.id.contacts_hint)).setText(
                getResources().getQuantityString(R.plurals.selected_contacts,
                        SelectContactsActivity.getSelectedContacts(this).size(),
                        SelectContactsActivity.getSelectedContacts(this).size()));

        findViewById(R.id.contacts_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                    startActivity(new Intent(getApplicationContext(), SelectContactsActivity.class));
                } else {
                    getPermissions();
                }
            }
        });

        // settings list
        findViewById(R.id.settings_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RotateAnimation anim;

                if(settingsExpanded) {
                    findViewById(R.id.settings_list_items).setVisibility(View.GONE);
                    anim = new RotateAnimation(180f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                } else {
                    findViewById(R.id.settings_list_items).setVisibility(View.VISIBLE);
                    anim = new RotateAnimation(0f, 180f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                }

                anim.setInterpolator(new LinearInterpolator());
                anim.setDuration(200);
                anim.setFillAfter(true);
                findViewById(R.id.arrow_down).startAnimation(anim);
                settingsExpanded = !settingsExpanded;
            }
        });

        // autostart
        mAutoStartSwitch = (Switch) findViewById(R.id.autostart_switch);
        mAutoStartSwitch.setChecked(prefs.getBoolean("autostart", true));
        mAutoStartSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PackageManager pm  = getPackageManager();
                prefs.edit().putBoolean("autostart", isChecked).apply();

                if(isChecked) {
                    ComponentName componentName = new ComponentName(MainActivity.this, BootReceiver.class);
                    pm.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                            PackageManager.DONT_KILL_APP);
                } else {
                    ComponentName componentName = new ComponentName(MainActivity.this, BootReceiver.class);
                    pm.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            PackageManager.DONT_KILL_APP);
                }
            }
        });

        findViewById(R.id.autostart_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAutoStartSwitch.toggle();
            }
        });

        // crit percent
        findViewById(R.id.crit_percent_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCritPercentDialog(prefs);
            }
        });

        ((TextView) findViewById(R.id.crit_precent))
                .setText(getString(R.string.crit_percent_value, prefs.getInt("crit_percent", 5)));

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CONTACT: {
                // If request is cancelled, the result arrays are empty.
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    if(ContextCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                            mEnabledSwitch.setChecked(true);
                    } else {
                        mEnabledSwitch.setChecked(false);
                        getPermissions();
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
                        Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                        mEnabledSwitch.setChecked(true);
                    } else {
                        mEnabledSwitch.setChecked(false);
                        getPermissions();
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
        ((TextView) findViewById(R.id.contacts_hint)).setText(
                getResources().getQuantityString(R.plurals.selected_contacts,
                        SelectContactsActivity.getSelectedContacts(this).size(),
                        SelectContactsActivity.getSelectedContacts(this).size()));
    }

    private void setDividerColor(NumberPicker picker, int color) {
        java.lang.reflect.Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for(java.lang.reflect.Field field: pickerFields) {
            if(field.getName().equals("mSelectionDivider")) {
                field.setAccessible(true);
                try {
                    ColorDrawable colorDrawable = new ColorDrawable(color);
                    field.set(picker, colorDrawable);
                } catch (IllegalArgumentException | Resources.NotFoundException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    public void showEditMessageDialog(final SharedPreferences prefs) {
        final EditText msg = new EditText(this);
        msg.setText(prefs.getString("message", getString(R.string.default_message)));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.edit_message_title)
            .setView(msg)
            .setPositiveButton(R.string.set, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("message", msg.getText().toString());
                    editor.apply();
                }
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            })
            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    Log.d(TAG, "onCancel: ");
                }
            })
            .show();

        msg.requestFocus();
    }

    private void showCritPercentDialog(final SharedPreferences prefs) {
        final NumberPicker picker = new NumberPicker(this);
        picker.setMinValue(1);
        picker.setMaxValue(100);
        picker.setValue(prefs.getInt("crit_percent", 5));
        setDividerColor(picker, ContextCompat.getColor(this, R.color.colorAccent));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
            .setTitle(R.string.crit_percent)
            .setView(picker)
            .setPositiveButton(R.string.set, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    prefs.edit().putInt("crit_percent", picker.getValue()).apply();
                    ((TextView) findViewById(R.id.crit_precent))
                            .setText(getString(R.string.crit_percent_value, picker.getValue()));
                    dialog.dismiss();
                }
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            })
            .show();
    }
}