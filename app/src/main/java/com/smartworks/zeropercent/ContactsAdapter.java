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
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.QuickContactBadge;
import android.widget.TextView;

import com.turingtechnologies.materialscrollbar.INameableAdapter;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;

class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactViewHolder>  implements Filterable, INameableAdapter {
    private static final String TAG = "ContactsAdapter";

    private Context context;
    private ArrayList<Contact> contactsList, filteredContactsList;
    ArrayList<Contact> selectedContactsList;

    ContactsAdapter(Context context, ArrayList<Contact> contactsList){
        super();

        this.context = context;
        this.contactsList = contactsList;
        this.filteredContactsList = contactsList;
        this.selectedContactsList = SelectContactsActivity.getSelectedContacts(context);
    }

    @Override
    public Character getCharacterForElement(int pos) {
        return filteredContactsList.get(pos).name.charAt(0);
    }

    class ContactViewHolder extends RecyclerView.ViewHolder{
        CheckBox check;
        QuickContactBadge badge;
        TextView name;

        ContactViewHolder(View v) {
            super(v);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    check.toggle();
                }
            });

            check = (CheckBox) v.findViewById(R.id.check_contact);
            badge = (QuickContactBadge) v.findViewById(R.id.quickbadge);
            name = (TextView) v.findViewById(R.id.contact_text);
        }

        private void getThumbnail(int pos) {
            Bitmap thumbnail = loadContactPhotoThumbnail(filteredContactsList.get(pos).thumbnail);

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
                String text = filteredContactsList.get(pos).name.substring(0, 1);
                paintT.getTextBounds(text, 0, text.length(), textBounds);
                canvas.drawText(text, radiusPixels - textBounds.exactCenterX(), radiusPixels - textBounds.exactCenterY(), paintT);
            } else {
                final Rect rect = new Rect(0, 0, (int) diameterPixels, (int) diameterPixels);
                canvas.drawBitmap(thumbnail, rect, rect, paint);
            }

            badge.setImageBitmap(output);

            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                badge.setOverlay(null);
            } else {
                try {
                    Field overlayField = QuickContactBadge.class.getDeclaredField("mOverlay");
                    overlayField.setAccessible(true);
                    overlayField.set(badge, null);
                } catch (Exception e) {
                    Log.d(TAG, "Badge Reflection Exception:");
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String filterString = constraint.toString().toLowerCase();
                FilterResults results = new FilterResults();
                ArrayList<Contact> filtered = new ArrayList<>();

                for(int i = 0; i < contactsList.size(); i++) {
                    if(contactsList.get(i).name.toLowerCase().contains(filterString)) {
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
                ((SelectContactsActivity) context).updateResults(results.count);
                notifyDataSetChanged();
            }

        };
    }

    void clearFilter() {
        filteredContactsList = contactsList;
    }

    @Override
    public int getItemCount() {
        return filteredContactsList.size();
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

    @Override
    public ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return new ContactViewHolder(inflater.inflate(R.layout.contacts_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ContactViewHolder holder, int position) {
        final Contact c = filteredContactsList.get(position);

        holder.badge.assignContactUri(filteredContactsList.get(0).uri);
        holder.getThumbnail(position);
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
            }
        });
    }
}
