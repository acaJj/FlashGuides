package com.wew.azizchr.guidezprototype;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.List;


/**
 * Created by Jeffrey
 * Attaches the data to the adapter and sets the onClick to the appropriate guide
 */
public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchResultHolder> {

    List<Result> searchResults;

    SearchAdapter(List<Result> sr){ this.searchResults = sr; }

    public static class SearchResultHolder extends RecyclerView.ViewHolder{
        CardView cardView;
        TextView guideTitle;
        TextView guideAuthor;
        TextView guideDate;

        //Creates ViewHolder class and sets the attributes
        SearchResultHolder (final View itemView){
            super(itemView);
            cardView = (CardView)itemView.findViewById(R.id.searchResult);
            guideTitle = (TextView) itemView.findViewById(R.id.guideTitle);
            guideAuthor = (TextView) itemView.findViewById(R.id.guideAuthor);
            guideDate = (TextView) itemView.findViewById(R.id.guideDatePublished);

            //Starts the view guide activity for the selected guide
            itemView.setClickable(true);
            /*
            itemView.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("ResourceAsColor")
                @Override
                public void onClick(View v) {
                    //Get the activity in which the cardview resides in
                    Activity mContext = (Activity) v.getContext();

                    //Starts the intent with a transition
                    Intent intent = new Intent(v.getContext(), CreateNewGuide.class);
                    intent.putExtra("MODE","EDIT");

                    v.getContext().startActivity(intent);
                    mContext.overridePendingTransition(R.anim.rightslide, R.anim.leftslide);
                }
            });*/
        }

        public void bindData(final Result result){
            //set cardview listener
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Get the activity in which the cardview resides in
                    Activity mContext = (Activity) view.getContext();

                    //Starts the intent with a transition
                    //set listener based on result destination
                    if (result.getDestination().equals("Edit")){
                        Intent intent = new Intent(view.getContext(), CreateNewGuide.class);
                        intent.putExtra("MODE","EDIT");
                        intent.putExtra("GUIDEID",result.getId());
                        intent.putExtra("GUIDE_TITLE",result.getTitle());
                        intent.putExtra("KEY",result.getKey());

                        view.getContext().startActivity(intent);
                    }else if (result.getDestination().equals("View")){
                        Intent intent = new Intent(view.getContext(), ViewGuide.class);
                        intent.putExtra("GUIDEID",result.getId());
                        intent.putExtra("GUIDE_TITLE",result.getTitle());
                        intent.putExtra("KEY",result.getKey());
                        intent.putExtra("USERID",result.getUserId());
                        intent.putExtra("AUTHOR",result.getName());

                        view.getContext().startActivity(intent);
                    }

                    mContext.overridePendingTransition(R.anim.rightslide, R.anim.leftslide);
                }
            });
        }
    }

    @NonNull
    @Override
    public SearchResultHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_result, parent, false);
        SearchResultHolder srHolder = new SearchResultHolder(v);
        return srHolder;
    }

    //Binds the data to the appropriate area of the cardview
    @Override
    public void onBindViewHolder(@NonNull SearchResultHolder holder, int position) {
        holder.guideTitle.setText(searchResults.get(position).title);
        holder.guideAuthor.setText("By: " + searchResults.get(position).name);
        holder.guideDate.setText("Created: " + searchResults.get(position).date);
        //bind the listener
        holder.bindData(searchResults.get(position));
    }

    //Returns the number of search results
    @Override
    public int getItemCount() {
        return searchResults.size();
    }

    //Attaches itself to the recycler view
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView){
        super.onAttachedToRecyclerView(recyclerView);
    }
}
