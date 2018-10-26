package com.wew.azizchr.guidezprototype;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.GenericTransitionOptions;
import com.bumptech.glide.Glide;

public class ViewGuide extends AppCompatActivity {

    public LinearLayout layoutFeed;
    public LinearLayout currentStepLayout;
    public LinearLayout.LayoutParams stepLP;
    public TextView title;
    int currentStepNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_guide);

        //sets the status bar color
        if (android.os.Build.VERSION.SDK_INT >= 21){
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.statusbarpurple));
        }

        layoutFeed = findViewById(R.id.newGuideLayoutFeed);
        stepLP = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        stepLP.setMargins(0,0,0, 75);
        title = findViewById(R.id.txtGuideTitle);

        addTitle("Sample Guide - Recycling");

        //Run these to simulate adding steps + text + pictures
        addStep("Empty the cup", "The cup needs to be empty because it will mess up the"
                + " recycling process if the cup still contains liquid. Down the drain is fine");
        addImage("fullcup","fullcup", currentStepLayout);


        addStep("Take off the lid", "The lid comes off the body of the cup. It goes in the"
                + " blue recycling bin. The lid can be used for other purposes");
        addImage("lid","lid", currentStepLayout);

        addStep("Throw the rest", "The rest of the cup, including the part to keep your "
        + "hand from burning, can go in the organics bin. They're used for compost.");
        addImage("emptycup","emptycup", currentStepLayout);
    }

    public boolean addTitle(String titleText){
        try{
            title.setText(titleText);
        } catch(Exception ex){
            Log.i("Error getting title: ", ex.getMessage());
            return false;
        }
        return true;
    }

    public boolean addStep(String title, String desc){
        try{
            //Calculates the current step number
            currentStepNumber = layoutFeed.getChildCount() + 1;

            //Creates the new step block and title block layouts
            final LinearLayout newStepBlock = new LinearLayout(ViewGuide.this);
            newStepBlock.generateViewId();
            final LinearLayout newTitleBlock = new LinearLayout(ViewGuide.this);
            newStepBlock.setOrientation(LinearLayout.VERTICAL);
            newTitleBlock.setOrientation(LinearLayout.HORIZONTAL);

            //Creates the various text views and changes their visual properties
            TextView mStepNumber = new TextView(ViewGuide.this);
            mStepNumber.setTypeface(null, Typeface.BOLD);
            mStepNumber.setTextSize(24);
            mStepNumber.setTextColor(Color.BLACK);
            TextView mStepTitle = new TextView(ViewGuide.this);
            mStepTitle.setTypeface(null, Typeface.BOLD);
            mStepTitle.setTextSize(24);
            mStepTitle.setTextColor(Color.BLACK);

            //Creates the linear layout to separate the two steps
            LinearLayout stepDivider = new LinearLayout(ViewGuide.this);
            stepDivider.setBackgroundResource(R.drawable.border_new_content);
            stepDivider.setOrientation(LinearLayout.HORIZONTAL);
            stepDivider.setLayoutParams(stepLP);
            stepDivider.setPadding(0,0,0,75);

            //Creates and assigns the proper text to the step title
            String newStepNum = "Step " + currentStepNumber + " : ";
            mStepNumber.setText(newStepNum);
            mStepTitle.setText(title);

            //Adds the views to the title and step blocks
            newTitleBlock.addView(mStepNumber);
            newTitleBlock.addView(mStepTitle);
            newStepBlock.addView(newTitleBlock);
            newStepBlock.addView(stepDivider);
            currentStepLayout = newStepBlock;

            //Adds the whole step block to the bottom of the main layout
            layoutFeed.addView(newStepBlock, layoutFeed.getChildCount());

            //adds the starting description to the step block if not null/empty
            if (!desc.equals("")){
                addDescription(desc, newStepBlock);
            }

        }catch (Exception ex){
            ex.getMessage();
            return false;
        }
        return true;
    }


    private boolean addDescription(String newStepDesc, LinearLayout stepLayout) {
        try{
            //Creates a new textview and sets the tag (the tag is the current step number)
            WebView mDescription = new WebView(ViewGuide.this);

            //String viewTag = "TEXT--" +newStepDesc;
            //mDescription.setTag(viewTag);

            //Formats the webview
            mDescription.setPadding(0, 10, 0, 10);
            mDescription.loadData(newStepDesc,"text/html","UTF-8");
            mDescription.setBackgroundColor(Color.TRANSPARENT);

            //adds the new text block with the text to the selected step
            stepLayout.addView(mDescription, stepLayout.getChildCount()-1);

        }catch (Exception ex){
            ex.getMessage();
            return false;
        }

        return true;
    }

    public boolean addImage(final String imageName,String tag, LinearLayout selectedLayout){
        try{
            //Creates the new imageview
            final ImageView newImgView = new ImageView(ViewGuide.this);

            Glide.with(ViewGuide.this)
                    .load(this.getResources().getIdentifier(imageName, "drawable", this.getPackageName()))
                    .transition(GenericTransitionOptions.with(R.anim.fui_slide_in_right))
                    .into(newImgView);

            newImgView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri uri = Uri.parse("android.resource://com.wew.azizchr.guidezprototype/drawable/" + imageName);
                    Intent intent = new Intent(ViewGuide.this, ViewPhoto.class);
                    intent.putExtra("imageUri", uri);
                    startActivity(intent);
                }
            });

            //fits the image to the sides, fixes the view bounds, adds padding
            newImgView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            newImgView.setAdjustViewBounds(true);
            newImgView.setPadding(3, 10, 3, 10);
            //String viewTag = "PICTURE--" + tag;
            //newImgView.setTag(viewTag);
            selectedLayout.addView(newImgView, selectedLayout.getChildCount()-1);

        }catch(Exception ex){
            Log.i("Error uploading image: ", ex.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to go back?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ViewGuide.this.finish();
                        overridePendingTransition(R.anim.leftslidebackward, R.anim.rightslidebackward);
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}