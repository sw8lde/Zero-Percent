package com.smartworks.zeropercent;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SelectContactsActivity extends AppCompatActivity {
    private static final String TAG = "SelectContactsActivity";
    private static final String SELECTED_CONTACTS_KEY = "selectedContacs";

    private ListView contactsChooser;
    private TextView txtLoadInfo;
    private ContactsListAdapter contactsListAdapter;
    private ContactsLoader contactsLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_contacts);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        contactsChooser = (ListView) findViewById(R.id.contacts_list);
        txtLoadInfo = (TextView) findViewById(R.id.txt_load_progress);
        contactsListAdapter = new ContactsListAdapter(this, new ArrayList<Contact>());
        contactsChooser.setAdapter(contactsListAdapter);

        loadContacts("");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(contactsListAdapter.selectedContactsList.isEmpty()) {
                    setResult(RESULT_CANCELED);
                } else {
                    Intent resultIntent = new Intent();

                    resultIntent.putParcelableArrayListExtra("SelectedContacts", contactsListAdapter.selectedContactsList);
                    setResult(RESULT_OK,resultIntent);
                }
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
            contactsLoader.txtProgress = txtLoadInfo;
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
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);
        MenuItemCompat.setOnActionExpandListener(menu.findItem(R.id.search), new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                contactsListAdapter.filter("");
                //hideEmpty();
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
                if (TextUtils.isEmpty(query)) {
                    contactsListAdapter.filter("");
                } else {
                    contactsListAdapter.filter(query);
                }
                return true;
            }
        });

        return true;
    }

    public static ArrayList<Contact> getSelectedContacts(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);

        Gson gson = new Gson();
        ArrayList<Contact> contacts = gson.fromJson(
                prefs.getString(SELECTED_CONTACTS_KEY, null),
                new TypeToken<ArrayList<Contact>>() {}.getType());
        if(contacts == null) {
            contacts = new ArrayList<>();
        }

        return contacts;
    }

    public static void setSelectedContacts(Context context, ArrayList<Contact> contacts) {
        SharedPreferences prefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Gson gson = new Gson();
        editor.putString(SELECTED_CONTACTS_KEY, gson.toJson(
                contacts,
                new TypeToken<ArrayList<Contact>>() {
                }.getType()));

        editor.apply();
    }

}
