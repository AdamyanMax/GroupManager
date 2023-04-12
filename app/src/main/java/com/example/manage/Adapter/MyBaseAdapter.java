package com.example.manage.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.manage.Contacts.Contacts;
import com.example.manage.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyBaseAdapter extends BaseAdapter {
    private final Context context;
    private final ArrayList<Contacts> contactsList;

    public MyBaseAdapter(Context context, ArrayList<Contacts> contactsList) {
        this.context = context;
        this.contactsList = contactsList;
    }

    @Override
    public int getCount() {
        return contactsList.size();
    }

    @Override
    public Object getItem(int position) {
        return contactsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.app_bar_layout, parent, false);
        }

        Contacts contacts = (Contacts) getItem(position);

        TextView tvName = convertView.findViewById(R.id.tv_display_username);
        TextView tvStatus = convertView.findViewById(R.id.tv_display_user_status);
        CircleImageView ivProfileImage = convertView.findViewById(R.id.civ_display_profile_image);

        tvName.setText(contacts.getName());
        tvStatus.setText(contacts.getStatus());
        Picasso.get().load(contacts.getImage()).into(ivProfileImage);

        return convertView;
    }
}
