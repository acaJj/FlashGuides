package com.wew.azizchr.guidezprototype;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.GenericTransitionOptions;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.Blob;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

/**
 * Created by Chris
 * LoadGuide code written by Jeff
 */
public class ViewGuide extends AppCompatActivity {

    public static String DEBUG_TAG = "BORBOT";

    public LinearLayout layoutFeed;
    public LinearLayout currentStepLayout;
    public LinearLayout.LayoutParams stepLP;
    public TextView title;
    int currentStepNumber;

    //Firebase Instance Variables
    private FirebaseStorage mStorage;
    private StorageReference imgStorage;
    private FirebaseFirestore mFirestore;
    private FirebaseAuth mFirebaseAuth;

    private String guideId;
    private String userId;
    private String guideTitle;
    private String guideKey;
    private String author;

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

        //Initialize firebase variables
        mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser mCurrentUser = mFirebaseAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance();
        imgStorage = mStorage.getReference();
        mFirestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        mFirestore.setFirestoreSettings(settings);

        //Gets the guide name variable from previous activity and puts it in the title
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if(bundle != null){
            guideId = bundle.getString("GUIDEID");
            guideKey = bundle.getString("KEY");
            guideTitle = bundle.getString("GUIDE_TITLE");
            userId = bundle.getString("USERID");
            author = bundle.getString("AUTHOR");
            Log.d(DEBUG_TAG,"Loading: " +guideKey + "/"+userId);
            loadGuide(userId,guideKey);
        }else{
            Log.d(DEBUG_TAG,"Not Loading");
            Toast.makeText(ViewGuide.this, "There was a problem loading your guide", Toast.LENGTH_LONG).show();
        }

        /*addTitle("Sample Guide - Recycling");

        //Run these to simulate adding steps + text + pictures
        addStep("Empty the cup", "The cup needs to be empty because it will mess up the"
                + " recycling process if the cup still contains liquid. Down the drain is fine");
        addImage("fullcup","fullcup", currentStepLayout);


        addStep("Take off the lid", "The lid comes off the body of the cup. It goes in the"
                + " blue recycling bin. The lid can be used for other purposes");
        addImage("lid","lid", currentStepLayout);

        addStep("Throw the rest", "The rest of the cup, including the part to keep your "
        + "hand from burning, can go in the organics bin. They're used for compost.");
        addImage("emptycup","emptycup", currentStepLayout);*/
    }

    /**
     * Loads guide for view
     * @param userId of the user whos guide we are viewing
     * @param key the name of the guide in the firestore database
     */
    private void loadGuide(String userId,String key) {
        //References to the guide we are editing and its component collections
        DocumentReference guideToEdit = mFirestore.document("Users/" + userId +"/guides/"+key);
        CollectionReference guideSteps = mFirestore.collection("Users/" + userId +"/guides/"+key + "/stepData");
        final CollectionReference guideText = mFirestore.collection("Users/" + userId +"/guides/"+key + "/textData");
        final CollectionReference guideImgs = mFirestore.collection("Users/" + userId +"/guides/"+key + "/imageData");

        //get the title
        guideToEdit.get().addOnCompleteListener(ViewGuide.this,new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()){

                        String title = doc.get("title").toString();
                        addTitle(title);
                    }
                }
            }
        });

        //get the stored guide data
        guideSteps.get().addOnCompleteListener(ViewGuide.this,new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                int stepNum;
                String stepTitle;
                if (task.isSuccessful()){
                    //for each step in the guide, get its info and recreate the step
                    //int totalSteps = task.getResult().size();
                    for (QueryDocumentSnapshot doc: task.getResult()){
                        stepNum = doc.getLong("stepNumber").intValue();
                        stepTitle = doc.get("stepTitle").toString();
                        addStep(stepTitle,"");

                        Query stepText = guideText.whereEqualTo("stepNumber",stepNum);
                        Query stepImgs = guideImgs.whereEqualTo("stepNumber", stepNum);
                        stepText.get().addOnCompleteListener(ViewGuide.this, new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()){
                                    for (QueryDocumentSnapshot snap: task.getResult()){
                                        Blob text = (Blob)snap.get("text");
                                        String id = snap.get("id").toString();
                                        String guideId =  snap.get("guideId").toString();
                                        String stepTitle = snap.get("stepTitle").toString();
                                        String type = snap.get("type").toString();
                                        String textType = snap.get("textType").toString();
                                        int num = snap.getLong("stepNumber").intValue();
                                        int placement = snap.getLong("placement").intValue();
                                        TextData data = new TextData(id,type,placement,guideId);
                                        data.setText(text);
                                        data.setTextType(textType);
                                        data.setStep(num,stepTitle);
                                        //data.setId(id);
                                        //Log.i(DEBUG_TAG+"-TEXT EXECUTE","in chained task, data added / " + data.getId() );

                                        addElement(data);
                                    }
                                }
                            }
                        });

                        stepImgs.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()){
                                    for (QueryDocumentSnapshot snap: task.getResult()){
                                        //get the data from fire store and add a data object to the data list
                                        String id = snap.get("id").toString();
                                        String guideId =  snap.get("guideId").toString();
                                        String stepTitle = snap.get("stepTitle").toString();
                                        String type = (String)snap.get("type");
                                        int num = snap.getLong("stepNumber").intValue();
                                        int placement = snap.getLong("placement").intValue();
                                        String imgPath = snap.get("imgPath").toString();
                                        String uri = snap.get("uri").toString();
                                        PictureData data = new PictureData(id,type,placement,guideId);
                                        data.setStep(num,stepTitle);
                                        data.setUri(uri);
                                        data.setImgPath(imgPath);

                                        //Log.i(DEBUG_TAG+"IMG EXECUTE","in chained task, data added / " + data.getId() );
                                        addElement(data);
                                    }
                                }
                            }
                        });

                        //Log.i(DEBUG_TAG+"EXECUTE","in Guide steps / " + doc.getId());
                    }
                }

            }
        });
    }

    public void addElement(GuideData data){
        int stepNumber = data.getStepNumber();

        View elementView = new View(ViewGuide.this);
        //depending on the data's type, load the proper view for the data
        if (data.getType().equals("Text")){
            TextData textData = (TextData) data;
            elementView = loadText(textData);
        }else if (data.getType().equals("Picture")){
            PictureData pictureData = (PictureData)data;
            StorageReference imageToLoad = imgStorage.child(pictureData.getImgPath());
            elementView = loadImage(pictureData,imageToLoad);
        }
        LinearLayout stepLayout = (LinearLayout)layoutFeed.getChildAt(stepNumber-1);
        //Log.i(DEBUG_TAG+"-Elements",""+data.getStepNumber()+"/"+data.getPlacement());

        //goes through the step to find the right location for the element and place it in
        for (int i =0; i< stepLayout.getChildCount()-1;i++){
            //if there is no data object in the step (only the title and buttons), place the element and get out
            if (stepLayout.getChildCount() == 2){
                stepLayout.addView(elementView,stepLayout.getChildCount()-1);
                break;
            }

            int currentViewIndex = i+1;//add 1 because the first element in step is the title layout

            View currentView = stepLayout.getChildAt(currentViewIndex);
            //Log.i("Placements:",""+(String)currentView.getTag(R.id.index)+"/"+(String)elementView.getTag(R.id.index));
            int currentViewPlacement = 0;
            try{
                currentViewPlacement = Integer.parseInt((String)currentView.getTag(R.id.index));
            }catch(NumberFormatException ex){
                Log.i("EXCEPTION!",ex.getMessage());
                //if we end up here then the element goes at the end of the step
                stepLayout.addView(elementView,currentViewIndex);
                break;
            }

            int elementViewPlacement = Integer.parseInt((String)elementView.getTag(R.id.index));

            //compare the placement of the elementView and currentView
            if (elementViewPlacement<currentViewPlacement){//if less, then it goes before it in the step
                stepLayout.addView(elementView,currentViewIndex);
                break;
            }

            //Log.i(DEBUG_TAG,data.getId()+"/"+elementView.getParent());
        }
    }

    public WebView loadText(TextData text){
        try{
            //Creates a new textview and sets the tag (the tag is the current step number)
            WebView mDescription = new WebView(ViewGuide.this);
            String description = text.getStringFromBlob();

            //set tag to the text so we can easily get it when uploading, haven't yet figured out how to get from webview
            String viewTag = "TEXT--" +description;
            mDescription.setTag(R.id.viewId,viewTag);
            Log.i("Placement",""+text.getPlacement());
            mDescription.setTag(R.id.index,""+text.getPlacement());

            mDescription.setPadding(5, 10, 5, 10);
            mDescription.loadData(description,"text/html","UTF-8");
            mDescription.setBackgroundColor(Color.TRANSPARENT);

            mDescription.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    switch (motionEvent.getAction()){
                        case MotionEvent.ACTION_UP:
                            view.performClick();
                            //displayTextOptionsMenu(view);
                    }
                    return true;
                }
            });

            //adds the new text block with the text to the selected step
            //selectedLayout.addView(mDescription, selectedLayout.getChildCount() - 1);

            return mDescription;
        }catch (Exception ex){
            ex.getMessage();
            return null;
        }
    }

    /**
     * Loads an image from Firebase Storage into a new image view
     * @param ref of the image we are getting from storage
     */
    public ImageView loadImage(PictureData pictureData,StorageReference ref){
        try{
            //Creates the new imageview
            final ImageView newImgView = new ImageView(ViewGuide.this);
            Glide.with(ViewGuide.this).asBitmap().load(ref).into(newImgView);
            Glide.with(ViewGuide.this)
                    .asBitmap()
                    .load(ref)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            //gets the mediapath of the image and parses it into a uri we can work with
                            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                            resource.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                            final String path = MediaStore.Images.Media.insertImage(ViewGuide.this.getContentResolver(), resource, UUID.randomUUID().toString() + ".png", "drawing");

                            newImgView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Uri imageUri = Uri.parse(path);
                                    Intent intent = new Intent(ViewGuide.this, ViewPhoto.class);
                                    intent.putExtra("imageUri", imageUri);
                                    startActivity(intent);
                                }
                            });
                        }
                    });

            //fits the image to the sides, fixes the view bounds, adds padding
            newImgView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            newImgView.setAdjustViewBounds(true);
            newImgView.setPadding(3, 10, 3, 10);

            //add it to the layout
            newImgView.setTag(R.id.index,""+pictureData.getPlacement());
            return newImgView;

        }catch(Exception ex){
            Log.i("IMAGE ERROR: ", ex.getMessage());
            return null;
        }
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