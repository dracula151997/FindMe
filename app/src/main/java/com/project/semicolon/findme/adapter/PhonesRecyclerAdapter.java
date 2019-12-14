package com.project.semicolon.findme.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.semicolon.findme.R;
import com.project.semicolon.findme.database.ContactEntity;

import java.util.List;

public class PhonesRecyclerAdapter extends RecyclerView.Adapter<PhonesRecyclerAdapter.ViewHolder> {
    private List<ContactEntity> items;

    public void setItems(List<ContactEntity> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public void delete(int position) {
        if (items != null) {
            items.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, getItemCount());
        }

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.phone_number_list_item,
                parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.phoneText.setText(items.get(position).getPhoneNumber());
        holder.nameText.setText(items.get(position).getContactName());

    }

    @Override
    public int getItemCount() {
        if (items == null) return 0;
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView phoneText;
        public TextView nameText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            phoneText = itemView.findViewById(R.id.phone_number);
            nameText = itemView.findViewById(R.id.display_name);
        }
    }
}
