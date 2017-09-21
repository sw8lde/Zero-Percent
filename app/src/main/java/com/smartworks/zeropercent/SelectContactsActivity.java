package com.smartworks.zeropercent;

import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.turingtechnologies.materialscrollbar.AlphabetIndicator;
import com.turingtechnologies.materialscrollbar.DragScrollBar;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class SelectContactsActivity extends AppCompatActivity {
    private static final String TAG = "SelectContactsActivity";
    private static final String SELECTED_CONTACTS_KEY = "selectedContacs";

    private ContactsAdapter contactsAdapter;
    private RecyclerView contactsChooser;
    private TextView noResults;
    private MaterialSearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_contacts);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        searchView = (MaterialSearchView) findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(TextUtils.isEmpty(newText)) {
                    contactsAdapter.clearFilter();
                } else {
                    contactsAdapter.getFilter().filter(newText);
                }
                return true;
            }
        });

        noResults = (TextView) findViewById(R.id.no_results);
        contactsChooser = (RecyclerView) findViewById(R.id.contacts_list);

        contactsAdapter = new ContactsAdapter(SelectContactsActivity.this, new ArrayList<Contact>());
        contactsChooser.setHasFixedSize(true);
        contactsChooser.setLayoutManager(new LinearLayoutManager(SelectContactsActivity.this));
        contactsChooser.setAdapter(contactsAdapter);

        ((DragScrollBar) findViewById(R.id.drag_scroll_bar)).setIndicator(new AlphabetIndicator(this), true);

        getAsyncContacts().execute((Void[]) null);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectedContacts(getApplicationContext(), contactsAdapter.selectedContactsList);
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    private AsyncTask<Void, Void, Void> getAsyncContacts() {
        return new AsyncTask<Void, Void, Void>() {
            private ArrayList<Contact> contacts;

            @Override
            protected Void doInBackground(Void... params) {
                contacts = getContacts();
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                findViewById(R.id.loading_spinner).setVisibility(View.GONE);

                contactsAdapter = new ContactsAdapter(SelectContactsActivity.this, contacts);
                contactsChooser.setAdapter(contactsAdapter);
            }
        };
    }

    private ArrayList<Contact> getContacts() {
        ArrayList<Contact> contacts = new ArrayList<>();

        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                if(Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                    Cursor phoneCursor = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id},
                            null);
                    if(phoneCursor.moveToNext()) {
                        String phoneId = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID));
                        String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                        Uri contactUri = ContactsContract.Contacts.getLookupUri(
                                phoneCursor.getLong(phoneCursor.getColumnIndex(ContactsContract.Contacts._ID)),
                                phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY)));

                        String thumbnail = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));

                        contacts.add(new Contact(phoneId, name, phoneNumber, thumbnail, contactUri));
                    }

                    phoneCursor.close();
                }
            }

            cursor.close();
            return contacts;
        }

        cursor.close();
        return null;
    }

    public static ArrayList<Contact> getSelectedContacts(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Uri.class, new UriDeserializer())
                .create();
        ArrayList<Contact> contacts = gson.fromJson(
                prefs.getString(SELECTED_CONTACTS_KEY, null),
                new TypeToken<ArrayList<Contact>>() {}.getType());
        if(contacts == null) {
            contacts = new ArrayList<>();
        }

        return contacts;
    }

    @Override
    public void onBackPressed() {
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        } else {
            super.onBackPressed();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_clear_all:
                if(contactsAdapter != null) {
                    contactsAdapter.selectedContactsList.clear();
                }
                return true;
        }
        return false;
    }

    public static void setSelectedContacts(Context context, ArrayList<Contact> contacts) {
        SharedPreferences prefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Uri.class, new UriSerializer())
                .create();
        editor.putString(SELECTED_CONTACTS_KEY, gson.toJson(
                contacts,
                new TypeToken<ArrayList<Contact>>() {}.getType()));

        editor.apply();
    }

    public void updateResults(int count) {
        if(count == 0) {
            noResults.setVisibility(View.VISIBLE);
        } else {
            noResults.setVisibility(View.GONE);
        }
    }
}

class UriSerializer implements JsonSerializer<Uri> {
    public JsonElement serialize(Uri src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.toString());
    }
}

class UriDeserializer implements JsonDeserializer<Uri> {
    @Override
    public Uri deserialize(final JsonElement src, final Type srcType,
                           final JsonDeserializationContext context) throws JsonParseException {
        return Uri.parse(src.getAsString());
    }
}
