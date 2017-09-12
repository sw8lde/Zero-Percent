package com.smartworks.zeropercent;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.app.Activity;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.QuickContactBadge;
import android.widget.TextView;


import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


public class ContactsListAdapter extends BaseAdapter implements Filterable {
    private static final String TAG = "ContactsListAdapter";

    private Context context;
    private ArrayList<Contact> contactsList, filteredContactsList;
    public ArrayList<Contact> selectedContactsList;
    private String filterContactName;

    ContactsListAdapter(Context context, ArrayList<Contact> contactsList){

        super();
        this.context = context;
        this.contactsList = contactsList;
        this.filteredContactsList = new ArrayList<>();
        this.selectedContactsList = SelectContactsActivity.getSelectedContacts(context);
        this.filterContactName = "";
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String filterString = constraint.toString().toLowerCase();
                FilterResults results = new FilterResults();
                ArrayList<Contact> filtered = new ArrayList<>();

                for (int i = 0; i < contactsList.size(); i++) {
                    if (contactsList.get(i).name.toLowerCase().contains(filterString)) {
                        filtered.add(contactsList.get(i));
                    }
                }

                results.values = filtered;
                results.count = filtered.size();

                return results;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredContactsList = (ArrayList<Contact>) results.values;
                notifyDataSetChanged();
            }

        };
    }

    public void clearSelected() {
        selectedContactsList.clear();
        notifyDataSetChanged();
    }

    public void addContacts(ArrayList<Contact> contacts){
        contactsList.addAll(contacts);
        getFilter().filter(this.filterContactName);
    }

    @Override
    public int getCount() {
        return filteredContactsList.size();
    }

    @Override
    public Contact getItem(int position) {
        return filteredContactsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return Long.parseLong(this.getItem(position).id);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Contact c = filteredContactsList.get(position);
        final ViewHolder holder;

        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.contacts_list_item, parent, false);

            holder = new ViewHolder();
            holder.badge = (QuickContactBadge) convertView.findViewById(R.id.quickbadge);
            holder.check = (CheckBox) convertView.findViewById(R.id.check_contact);
            holder.name = (TextView) convertView.findViewById(R.id.contact_text);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.name.setText(filteredContactsList.get(position).toString());
        holder.check.setChecked(selectedContactsList.contains(c));
        holder.check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    selectedContactsList.add(c);
                } else if(selectedContactsList.contains(c)) {
                    selectedContactsList.remove(c);
                }
                Log.d("contact", filteredContactsList.get(position).toString());
            }
        });

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.check.toggle();
            }
        });

        holder.badge.assignContactUri(filteredContactsList.get(position).uri);
        Bitmap thumbnail = loadContactPhotoThumbnail(filteredContactsList.get(position).thumbnail);

        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float diameterPixels = 40 * (metrics.densityDpi / 160f);
        float radiusPixels = diameterPixels/2;

        Bitmap output = Bitmap.createBitmap((int) diameterPixels, (int) diameterPixels,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(getRandomColor("500"));
        canvas.drawCircle(radiusPixels, radiusPixels, radiusPixels, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        if(thumbnail == null) {
            final Paint paintT = new Paint();
            paintT.setColor(Color.WHITE);
            paintT.setAntiAlias(true);
            paintT.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    26, context.getResources().getDisplayMetrics()));
            paintT.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
            final Rect textBounds = new Rect();
            String text = filteredContactsList.get(position).name.substring(0, 1);
            paintT.getTextBounds(text, 0, text.length(), textBounds);
            canvas.drawText(text, radiusPixels - textBounds.exactCenterX(), radiusPixels - textBounds.exactCenterY(), paintT);
        } else {
            final Rect rect = new Rect(0, 0, (int) diameterPixels, (int) diameterPixels);
            canvas.drawBitmap(thumbnail, rect, rect, paint);
        }

        holder.badge.setImageBitmap(output);

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            holder.badge.setOverlay(null);
        } else {
            try {
                Field overlayField = QuickContactBadge.class.getDeclaredField("mOverlay");
                overlayField.setAccessible(true);
                overlayField.set(holder.badge, null);
            } catch (Exception e) {
                Log.d(TAG, "Badge Reflection Exception:");
                e.printStackTrace();
            }
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView name;
        CheckBox check;
        QuickContactBadge badge;
    }

    @Nullable
    private Bitmap loadContactPhotoThumbnail(String photoData) {
        AssetFileDescriptor afd = null;

        if(photoData != null) {
            try {
                Uri thumbUri = Uri.parse(photoData);

                afd = context.getContentResolver().openAssetFileDescriptor(thumbUri, "r");
                FileDescriptor fileDescriptor = afd.getFileDescriptor();

                if(fileDescriptor != null) {
                    return BitmapFactory.decodeFileDescriptor(fileDescriptor, null, null);
                }
            } catch (Exception e) {
                Log.d(TAG, "loadContactPhotoThumbnail:");
                e.printStackTrace();
            }
        }
        if(afd != null) {
            try {
                afd.close();
            } catch(IOException e) {}
        }

        return null;
    }

    private int getRandomColor(String typeColor) {
        int returnColor = Color.BLACK;
        int arrayId = context.getResources().getIdentifier("mdcolor_" + typeColor, "array",
                context.getApplicationContext().getPackageName());

        if(arrayId != 0) {
            TypedArray colors = context.getResources().obtainTypedArray(arrayId);
            int index = (int) (Math.random() * colors.length());
            returnColor = colors.getColor(index, Color.BLACK);
            colors.recycle();
        }
        return returnColor;
    }
}
