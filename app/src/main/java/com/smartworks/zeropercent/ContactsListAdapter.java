package com.smartworks.zeropercent;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.app.Activity;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;


import java.util.ArrayList;


public class ContactsListAdapter extends BaseAdapter {

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

    public void filter(String filterContactName){
        filteredContactsList.clear();

        if(filterContactName.isEmpty() || filterContactName.length()<1){
            filteredContactsList.addAll(contactsList);
            this.filterContactName = "";

        }
        else {
            this.filterContactName = filterContactName.toLowerCase().trim();
            for (int i = 0; i < contactsList.size(); i++) {

                if (contactsList.get(i).name.toLowerCase().contains(filterContactName))
                    filteredContactsList.add(contactsList.get(i));
            }
        }
        notifyDataSetChanged();
    }

    public void addContacts(ArrayList<Contact> contacts){
        this.contactsList.addAll(contacts);
        this.filter(this.filterContactName);
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
        final CheckBox checkBox;
        final Contact c = filteredContactsList.get(position);

        if(convertView == null){
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            convertView = inflater.inflate(R.layout.contacts_list_item, parent, false);
        }

        TextView name = (TextView) convertView.findViewById(R.id.contact_text);
        name.setText(filteredContactsList.get(position).toString());

        checkBox = (CheckBox) convertView.findViewById(R.id.check_contact);
        checkBox.setChecked(selectedContactsList.contains(c));
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkBox.toggle();
                if(checkBox.isChecked()) {
                    selectedContactsList.add(c);
                } else if(selectedContactsList.contains(c)) {
                    selectedContactsList.remove(c);
                }
                Log.d("contact", filteredContactsList.get(position).toString());
                SelectContactsActivity.setSelectedContacts(context, selectedContactsList);
            }
        });

        return convertView;
    }
}
