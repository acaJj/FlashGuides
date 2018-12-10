package com.wew.azizchr.guidezprototype;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.algolia.search.saas.Client;
import com.algolia.search.saas.Index;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.util.List;


/**
 * Created by Jeffrey
 * Attaches the data to the adapter and sets the onClick to the appropriate guide
 */
public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchResultHolder> {

    private List<Result> searchResults;
    private FirebaseFirestore mFirestore;
    //Algolia id and api keys for search
    private static String updateApiKey = "132c50036e3241722083caa0a25393e2";//do not use the admin key
    private static String applicationId = "031024FLJM";
    private static Client client = new Client(applicationId, updateApiKey);
    private static Index algoliaIndex = client.getIndex("guides");

    SearchAdapter( List<Result> sr,FirebaseFirestore firestore){
        this.searchResults = sr;
        mFirestore = firestore;
    }

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
        }

        public void bindData(final Result result,final FirebaseFirestore mFirestore){
            //set cardview listener
            cardView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(final View view) {
                    //Get the activity in which the cardview resides in
                    final Activity mContext = (Activity) view.getContext();

                    if (result.getDestination().equals("View")){
                        Intent intent = new Intent(view.getContext(), ViewGuide.class);
                        intent.putExtra("GUIDEID",result.getId());
                        intent.putExtra("GUIDE_TITLE",result.getTitle());
                        intent.putExtra("KEY",result.getKey());
                        intent.putExtra("USERID",result.getUserId());
                        intent.putExtra("AUTHOR",result.getName());

                        view.getContext().startActivity(intent);

                        mContext.overridePendingTransition(R.anim.rightslide, R.anim.leftslide);
                        return;
                    }

                    final CharSequence[] items = {"Continue Editing","Publish","Unpublish","Delete Guide", "Cancel"};
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle("What would you like to do?");
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //get firestore reference to the guide in this view so we can do stuff with it
                            DocumentReference guideRef = mFirestore.document("Users/" + result.getUserId() +"/guides/"+result.getKey());
                            if (items[i].equals("Continue Editing")){
                                //Starts the intent with a transition
                                //set listener based on result destination
                                if (result.getDestination().equals("Edit")){
                                    Intent intent = new Intent(view.getContext(), CreateNewGuide.class);
                                    intent.putExtra("MODE","EDIT");
                                    intent.putExtra("GUIDEID",result.getId());
                                    intent.putExtra("GUIDE_TITLE",result.getTitle());
                                    intent.putExtra("KEY",result.getKey());
                                    intent.putExtra("DATE",result.getDate());

                                    view.getContext().startActivity(intent);

                                    mContext.overridePendingTransition(R.anim.rightslide, R.anim.leftslide);
                                }
                            }else if (items[i].equals("Publish")){
                                guideRef.update("publishedStatus",true);
                                Toast.makeText(mContext, "'"+result.getTitle() + "' has been published!",Toast.LENGTH_SHORT).show();

                                //index the guide title with Algolia so we can search it later
                                try{

                                    JSONObject guideObject = new JSONObject().//guide object
                                            put("title",result.getTitle()).put("author",result.getName())
                                            .put("userId",result.getUserId()).put("key",result.getKey())
                                            .put("date",result.getDate()).put("publishedStatus",true);

                                    algoliaIndex.addObjectAsync(guideObject, result.getId(),null);
                                }catch(Exception ex){
                                    Log.d("BORBOT", ex.getMessage());
                                }
                            }else if (items[i].equals("Unpublish")){
                                guideRef.update("publishedStatus",false);
                                Toast.makeText(mContext,"Your guide is no longer published, others can not see it",Toast.LENGTH_SHORT).show();

                                //index the guide title with Algolia so we can search it later
                                try{
                                    JSONObject guideObject = new JSONObject().//guide object
                                            put("title",result.getTitle()).put("author",result.getName())
                                            .put("userId",result.getUserId()).put("key",result.getKey())
                                            .put("date",result.getDate()).put("publishedStatus",false);
                                    algoliaIndex.addObjectAsync(guideObject, result.getId(),null);
                                }catch(Exception ex){
                                    Log.d("BORBOT", ex.getMessage());
                                }
                            }else if (items[i].equals("Delete Guide")){
                                FirebaseConnection mFirebaseConnection = new FirebaseConnection();
                                mFirebaseConnection.deleteGuide(mContext,guideRef);
                                mContext.recreate();
                            }else if (items[i].equals("Cancel")){
                                dialogInterface.dismiss();
                            }
                        }
                    });

                    builder.show();

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
        holder.guideTitle.setText(searchResults.get(position).getTitle());
        holder.guideAuthor.setText("By: " + searchResults.get(position).getName());
        holder.guideDate.setText("Created: " + searchResults.get(position).getDate());
        //bind the listener
        holder.bindData(searchResults.get(position),mFirestore );
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
