package com.smartworks.zeropercent;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SettingsListAdapter extends BaseExpandableListAdapter {
    private static final String TAG = "SettingsListAdapter";
    private Context context;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public SettingsListAdapter(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public int getGroupCount() {
        return 1;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 4;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return null;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return null;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
                             final ViewGroup parent) {
        /*
         * Settings:
         * 0) Autostart
         * 1) Crit %
         * 2) Normal Freq
         * 3) Crit Freq
         */
        final ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.settings_list_child, parent, false);
            holder = new ViewHolder();
            convertView.setTag(holder);

            switch(childPosition) {
                case 0:
                    holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                    holder.toggle = (Switch) convertView.findViewById(R.id.setting_switch);
                    break;
                case 1:
                    holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                    holder.textView = (TextView) convertView.findViewById(R.id.setting_textview_numpicker);
                    holder.picker = (TextView) convertView.findViewById(R.id.setting_numpicker);
                    holder.hint = (TextView) convertView.findViewById(R.id.hint);
                    break;
                case 2:
                    holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                    holder.textView = (TextView) convertView.findViewById(R.id.setting_textview_numpicker);
                    holder.picker = (TextView) convertView.findViewById(R.id.setting_numpicker);
                    holder.hint = (TextView) convertView.findViewById(R.id.hint);
                    break;
                case 3:
                    holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                    holder.textView = (TextView) convertView.findViewById(R.id.setting_textview_numpicker);
                    holder.picker = (TextView) convertView.findViewById(R.id.setting_numpicker);
                    holder.hint = (TextView) convertView.findViewById(R.id.hint);
                    break;
            }
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        switch(childPosition) {
            case 0:
                holder.icon.setImageResource(R.drawable.ic_autostart);
                holder.toggle.setVisibility(View.VISIBLE);
                holder.toggle.setText(R.string.autostart);
                holder.toggle.setChecked(prefs.getBoolean("autostart", false));
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        holder.toggle.toggle();
                        if (holder.toggle.isChecked()) {
                            editor.putBoolean("autostart", true);

                            PackageManager pm  = context.getPackageManager();
                            ComponentName componentName = new ComponentName(context, BootReceiver.class);
                            pm.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                                    PackageManager.DONT_KILL_APP);
                            Log.d(TAG, "Autostart enabled");
                        } else {
                            editor.putBoolean("autostart", false);

                            PackageManager pm  = context.getPackageManager();
                            ComponentName componentName = new ComponentName(context, BootReceiver.class);
                            pm.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                                    PackageManager.DONT_KILL_APP);
                            Log.d(TAG, "Autostart disabled");
                        }
                        editor.apply();
                    }
                });
                break;
            case 1:
                holder.icon.setImageResource(R.drawable.ic_battery_alert);
                holder.textView.setVisibility(View.VISIBLE);
                holder.textView.setText(R.string.crit_percent);
                holder.picker.setVisibility(View.VISIBLE);
                holder.picker.setText(context.getString(R.string.crit_percent_text, prefs.getInt("crit_percent", 5)));
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        initNumPicker(holder, R.string.crit_percent, R.string.crit_percent_text, "crit_percent", 1, 100);
                    }
                });
                break;
            case 2:
                holder.icon.setImageResource(R.drawable.ic_timer);
                holder.textView.setVisibility(View.VISIBLE);
                holder.textView.setText(R.string.normal_freq);
                holder.picker.setVisibility(View.VISIBLE);
                holder.picker.setText(context.getString(R.string.freq_text, prefs.getInt("normal_freq", 5)));
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        initNumPicker(holder, R.string.normal_freq, R.string.freq_text, "normal_freq", 1, 100);
                    }
                });
                holder.hint.setVisibility(View.VISIBLE);
                holder.hint.setText(R.string.normal_freq_hint);
                break;
            case 3:
                holder.icon.setImageResource(R.drawable.ic_timer);
                holder.textView.setVisibility(View.VISIBLE);
                holder.textView.setText(R.string.crit_freq);
                holder.picker.setVisibility(View.VISIBLE);
                holder.picker.setText(context.getString(R.string.freq_text, prefs.getInt("crit_freq", 5)));
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        initNumPicker(holder, R.string.crit_freq, R.string.freq_text, "crit_freq", 1, 100);
                    }
                });
                holder.hint.setVisibility(View.VISIBLE);
                holder.hint.setText(R.string.crit_freq_hint);
                break;
        }

        return convertView;
    }

    private void initNumPicker(final ViewHolder holder, int nameResId, final int textResId, final String key, int minValue, int maxValue) {
        final NumberPicker picker = new NumberPicker(context);
        picker.setMinValue(minValue);
        picker.setMaxValue(maxValue);
        picker.setValue(prefs.getInt(key, 5));
        setDividerColor(picker, ContextCompat.getColor(context, R.color.colorAccent));

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder
                .setTitle(nameResId)
                .setView(picker)
                .setPositiveButton(R.string.set, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        editor.putInt(key, picker.getValue());
                        holder.picker.setText(context.getString(textResId, picker.getValue()));
                        editor.apply();
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

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.settings_list_parent, parent, false);
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }


    private static class ViewHolder {
        ImageView icon;
        Switch toggle;
        TextView textView;
        TextView picker;
        TextView hint;
    }
}

