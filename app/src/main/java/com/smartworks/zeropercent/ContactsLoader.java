package com.smartworks.zeropercent;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;


public class ContactsLoader extends AsyncTask<String,Void,Void> {

    private ContactsListAdapter contactsListAdapter;
    private Context context;
    private ArrayList<Contact> tempContactHolder;
    public TextView txtProgress;
    private int totalContactsCount,loadedContactsCount;


    ContactsLoader(Context context,ContactsListAdapter contactsListAdapter) {
        this.context = context;
        this.contactsListAdapter = contactsListAdapter;
        this.tempContactHolder= new ArrayList<>();
        loadedContactsCount=0;
        totalContactsCount=0;
        txtProgress=null;
    }

    @Override
    protected Void doInBackground(String[] filters) {
        String filter = filters[0];
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = Contacts.CONTENT_URI;

        String[] projection = new String[]{
                Contacts._ID,
                Contacts.DISPLAY_NAME,
                Contacts.HAS_PHONE_NUMBER
        };
        Cursor cursor;
        if(filter.length() > 0) {
            cursor = contentResolver.query(
                    uri,
                    projection,
                    Contacts.DISPLAY_NAME + " LIKE ?",
                    new String[]{filter},
                    Contacts.DISPLAY_NAME + " ASC"
            );
        } else {
            cursor = contentResolver.query(
                    uri,
                    projection,
                    null,
                    null,
                    Contacts.DISPLAY_NAME + " ASC"
            );
        }

        totalContactsCount = cursor.getCount();
        if(cursor != null && cursor.getCount()>0){
            while(cursor.moveToNext()) {
                if(Integer.parseInt(cursor.getString(cursor.getColumnIndex(Contacts.HAS_PHONE_NUMBER))) > 0) {

                    String id = cursor.getString(cursor.getColumnIndex(Contacts._ID));
                    String name = cursor.getString(cursor.getColumnIndex(Contacts.DISPLAY_NAME));


                    Cursor phoneCursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?",
                            new String[]{id},
                            null
                    );

                    if(phoneCursor != null && phoneCursor.getCount() > 0) {
                        while (phoneCursor.moveToNext()) {
                            String phId = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID));

                            String customLabel = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL));

                            String label = (String) ContactsContract.CommonDataKinds.Phone.getTypeLabel(context.getResources(),
                                    phoneCursor.getInt(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)),
                                    customLabel
                            );
                            String phNo = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                            Uri contactUri = Contacts.getLookupUri(
                                    phoneCursor.getLong(phoneCursor.getColumnIndex(Contacts._ID)),
                                    phoneCursor.getString(phoneCursor.getColumnIndex(Contacts.LOOKUP_KEY)));

                            String thumbnail = phoneCursor.getString(phoneCursor.getColumnIndex(Contacts.PHOTO_THUMBNAIL_URI));

                            tempContactHolder.add(new Contact(phId, name, phNo, label, thumbnail, contactUri));
                        }
                        phoneCursor.close();
                    }
                }
                loadedContactsCount++;
                publishProgress();
            }
            cursor.close();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Void[] v) {
        if(this.tempContactHolder.size() >= 100) {
            contactsListAdapter.addContacts(tempContactHolder);

            this.tempContactHolder.clear();

            if(txtProgress != null){
                txtProgress.setVisibility(View.VISIBLE);
                String progressMessage = "Loading...(" + loadedContactsCount + "/" + totalContactsCount + ")";
                txtProgress.setText(progressMessage);
            }
        }
    }

    @Override
    protected void onPostExecute(Void v) {
        contactsListAdapter.addContacts(tempContactHolder);
        tempContactHolder.clear();

        if(txtProgress != null) {
            txtProgress.setText("");
            txtProgress.setVisibility(View.GONE);
        }
    }
}
