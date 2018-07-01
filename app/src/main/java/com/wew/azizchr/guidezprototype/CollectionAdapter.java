package com.wew.azizchr.guidezprototype;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Jeffrey on 2018-06-24.
 * This Adapter is necessary to model the data objects in the recyclerview
 * This code is from the example from the android documentation, which models strings
 * Ours would be more complex with strings and maybe a view for a preview pic
 */

//TODO: Modify the adapter code so that it uses the proper data we use, not string arrays

public class CollectionAdapter extends RecyclerView.Adapter<CollectionAdapter.ViewHolder> {
    private String[] mDataset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextView;
        public ViewHolder(View v) {
            super(v);
            mTextView = (TextView)v.findViewById(R.id.item_description);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public CollectionAdapter(String[] myDataset) {
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public CollectionAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // Create a new view. collection_item will be the template layout for all items in list
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.collection_item, parent, false);
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.mTextView.setText(mDataset[position]);

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.length;
    }
}
