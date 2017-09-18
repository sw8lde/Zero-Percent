package com.smartworks.zeropercent;

import android.app.SearchManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
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

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class SelectContactsActivity extends AppCompatActivity {
    private static final String TAG = "SelectContactsActivity";
    private static final String SELECTED_CONTACTS_KEY = "selectedContacs";

    private ListView contactsChooser;
    private TextView loading;
    private TextView noResults;
    private ContactsListAdapter contactsListAdapter;
    private ContactsLoader contactsLoader;

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

        contactsChooser = (ListView) findViewById(R.id.contacts_list);
        loading = (TextView) findViewById(R.id.txt_load_progress);
        noResults = (TextView) findViewById(R.id.no_results);
        contactsListAdapter = new ContactsListAdapter(this, new ArrayList<Contact>());
        contactsChooser.setAdapter(contactsListAdapter);

        loadContacts("");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectedContacts(getApplicationContext(), contactsListAdapter.selectedContactsList);
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    private void loadContacts(String filter) {
        if(contactsLoader != null && contactsLoader.getStatus() != AsyncTask.Status.FINISHED) {
            try {
                contactsLoader.cancel(true);
            } catch (Exception e){

            }
        }
        if(filter == null) filter="";

        try {
            contactsLoader = new ContactsLoader(this,contactsListAdapter);
            contactsLoader.txtProgress = loading;
            contactsLoader.execute(filter);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setQueryHint(getString(R.string.search));
        searchView.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);
        MenuItemCompat.setOnActionExpandListener(menu.findItem(R.id.search), new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                searchView.requestFocus();
                ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE))
                        .toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                searchView.setQuery("", true);
                ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(searchView.getWindowToken(), 0);
                return true;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if(TextUtils.isEmpty(query)) {
                    contactsListAdapter.getFilter().filter("");
                } else {
                    contactsListAdapter.getFilter().filter(query);
                }
//                if(contactsListAdapter.getCount() == 0) {
//                    noResults.setVisibility(View.VISIBLE);
//                } else {
//                    noResults.setVisibility(View.GONE);
//                }
                return true;
            }
        });

        try {
            Field cursor = TextView.class.getDeclaredField("mCursorDrawableRes");
            cursor.setAccessible(true);
            cursor.set(searchView.findViewById(
                    searchView.getContext().getResources().getIdentifier(
                            "android:id/search_src_text", null, null)), R.drawable.cursor);

            Field plate = SearchView.class.getDeclaredField("mSearchPlate");
            plate.setAccessible(true);
            searchView.findViewById(searchView.getContext().getResources()
                    .getIdentifier("android:id/search_plate", null, null))
                    .setBackgroundColor(ContextCompat.getColor(searchView.getContext(), R.color.colorPrimary));
        } catch(Exception e) {}

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.clear_all:
                if(contactsListAdapter != null) {
                    contactsListAdapter.clearSelected();
                }
                return true;
        }
        return false;
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

    public void updateResults(int count) {
        if(count == 0) {
            noResults.setVisibility(View.VISIBLE);
        } else {
            noResults.setVisibility(View.GONE);
        }
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
