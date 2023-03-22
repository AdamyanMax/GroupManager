package com.example.manage;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.GroupViewHolder> {

    private ArrayList<String> listOfGroups;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public static class GroupViewHolder extends RecyclerView.ViewHolder {
        public TextView groupNameTextView;

        public GroupViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);
            groupNameTextView = itemView.findViewById(R.id.tv_group_name);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position);
                    }
                }
            });
        }
    }
    public void updateData(ArrayList<String> newData) {
        listOfGroups.clear();
        listOfGroups.addAll(newData);
        notifyDataSetChanged();
    }

    public GroupsAdapter(ArrayList<String> listOfGroups) {
        this.listOfGroups = listOfGroups;
    }

    @Override
    public GroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_item, parent, false);
        return new GroupViewHolder(v, listener);
    }

    @Override
    public void onBindViewHolder(GroupViewHolder holder, int position) {
        String groupName = listOfGroups.get(position);
        holder.groupNameTextView.setText(groupName);
    }

    @Override
    public int getItemCount() {
        return listOfGroups.size();
    }
}

