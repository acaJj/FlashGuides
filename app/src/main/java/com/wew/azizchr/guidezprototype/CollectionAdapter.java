package com.wew.azizchr.guidezprototype;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Jeffrey on 2018-06-24.
 * This Adapter is necessary to model the data objects in the recyclerview
 * This code is from the example from the android documentation, which models strings
 * Ours would be more complex with strings and maybe a view for a preview pic
 */

public class CollectionAdapter extends RecyclerView.Adapter<CollectionAdapter.ViewHolder> {
    public interface OnItemClickListener{
        void OnItemClick(Guide item);
    }

    private List<Guide> mGuideList;
    private OnItemClickListener mOnItemClickListener;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextView;
        public ViewHolder(View v) {
            super(v);
            mTextView = v.findViewById(R.id.item_description);
        }

        //sets the data for each list item into their viewholder
        public void bindData(final Guide guide,final OnItemClickListener listener){
            mTextView.setTag(guide.getId());
            mTextView.setText(guide.getTitle());
            mTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.OnItemClick(guide);
                }
            });
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public CollectionAdapter(List<Guide> guides, OnItemClickListener listener) {
        this.mGuideList = guides;
        this.mOnItemClickListener = listener;
    }

    public CollectionAdapter(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public void setItems(List<Guide> guides){
        this.mGuideList = guides;
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

    // Pass our data into the view holder (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.bindData(mGuideList.get(position),mOnItemClickListener);


    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mGuideList.size();
    }

    @Override
    public int getItemViewType(final int position){
        return R.layout.collection_item;
    }

    public void clear(){
        int size = mGuideList.size();
        if (size > 0){
            for (int i = 0; i < size; i++){
                mGuideList.remove(0);
            }
        }
        this.notifyItemRangeRemoved(0,size);
    }
}
