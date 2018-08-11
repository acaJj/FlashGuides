package com.wew.azizchr.guidezprototype;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kbeanie.multipicker.api.CameraImagePicker;
import com.kbeanie.multipicker.api.ImagePicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenImage;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CreateNewGuide extends AppCompatActivity {

    private static final String TEXT_TAG = "TEXT";
    private static final String IMG_TAG = "IMG";
    private static final int SELECT_FILE =0;//used when selecting an image file
    private static final int WRITE_STEP =1;//used when making a new step
    private static final int WRITE_DESC =2;//used when making a new text block
    private static final int EDIT_DESC = 3;//used when editing a text block
    public static int PESDK_RESULT = 4;

    private String outputPath;
    private String newStepTitle;
    private String newStepDesc;
    private String newDesc;
    private String mode;//used to determine whether we are making a new guide or editing an old one

    private int guideNum = 0;//the current guide index thing
    private int textBlockNum; //the current number of textBlocks
    private int imgBlockNum;//the current number of image blocks
    private int currentIndex;
    private int currentPictureSwap;

    private Boolean isSwapping;

    private CameraImagePicker camera;
    public LinearLayout layoutFeed;
    public LinearLayout selectedLayout;
    public TextView selectedTextView;

    TextView mNewGuideTitle;
    private Button mSave;

    //The title of the guide, initialized as NULL so we can easily check through string methods if it hasn't been set
    String guideTitle = "NULL";

    //Firebase Instance Variables
    private FirebaseStorage mStorage;
    private FirebaseFirestore mFirestore;
    private FirebaseAuth mFirebaseAuth;

    //Cloud Firestore Reference Variables
    private CollectionReference textData;
    private CollectionReference picData;
    private DocumentReference userRef;

    //ArrayList stores metadata for the guide text and picture components
    private ArrayList<GuideData> mGuideDataArrayList = new ArrayList<>();
    private Guide newGuide; //guide object that stores descriptive info on the guide being made

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_guide);

        //sets the status bar color
        if (android.os.Build.VERSION.SDK_INT >= 21){
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.statusbarpurple));
        }

        newGuide = new Guide();

        mNewGuideTitle = (TextView) findViewById(R.id.txtNewGuideTitle);

        //Initialize firebase variables
        mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser mCurrentUser = mFirebaseAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        mFirestore.setFirestoreSettings(settings);

        //Gets the guide name variable from previous activity and puts it in the title
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if(bundle != null){
            guideTitle = bundle.getString("GUIDE_TITLE");
            mode = bundle.getString("MODE");
        } else{
            guideTitle = "Guide (No title)";
            mode = "CREATE";
        }

        userRef = mFirestore.document("Users/" + mFirebaseAuth.getUid());

        if (mode.equals("CREATE")){
            newGuide.setId(UUID.randomUUID().toString());
        }else if (mode.equals("EDIT")){
            newGuide.setId(bundle.getString("GUIDEID"));
            //loadGuide(newGuide.getId());
            Log.i("BORBOT EDITs","" + newGuide.getId());
        }
        mNewGuideTitle.setText(guideTitle);

        newGuide.setAuthor(mCurrentUser.getEmail());
        newGuide.setTitle(guideTitle);
        //newGuide.setDateCreated();

        //gets the number of guides the user has
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot documentSnapshot = task.getResult();
                guideNum = documentSnapshot.getLong("numGuides").intValue();
                guideNum++;// increments guideNum by 1 because we are making a new guide so there is 1 more than before

                //sets these 2 collections to point to the folders for the guide data of the new guide. for later uploading
                textData = mFirestore.collection("Users/" + mFirebaseAuth.getUid() +"/guides/"+guideNum+"/textData");
                picData = mFirestore.collection("Users/" + mFirebaseAuth.getUid() +"/guides/"+guideNum+"/imageData");
            }

        });

        //Sets up the Camera Image Picker values
        camera = new CameraImagePicker(CreateNewGuide.this);
        camera.setImagePickerCallback(new ImagePickerCallback() {
            @Override
            public void onImagesChosen(List<ChosenImage> list) {
                Uri imagePath = Uri.parse(list.get(0).getQueryUri());
                addImage(imagePath);
            }

            @Override
            public void onError(String s) {

            }
        });

        currentIndex = 0;
        isSwapping = false;
        layoutFeed = (LinearLayout) findViewById(R.id.newGuideLayoutFeed);
        mSave = findViewById(R.id.btnSaveGuide);

        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveGuide();
            }
        });
    }

    private void loadGuide(String id) {
        //References to the guide we are editing and its component collections
        DocumentReference guideToEdit = mFirestore.document("Users/" + mFirebaseAuth.getUid() +"/guides/"+guideNum);
        CollectionReference guideSteps = mFirestore.collection(guideToEdit + "stepData");
        CollectionReference guideText = mFirestore.collection(guideToEdit + "textData");
        CollectionReference guideImgs = mFirestore.collection(guideToEdit + "imageData");

        guideSteps.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot doc: task.getResult()){

                    }
                }else{
                    Log.i("SOMEONE FUCKED UP: ","IT WAS JEFFREY!");
                }
            }
        });
    }

    public void onClickStep(View view) {
        Intent intent = new Intent(CreateNewGuide.this,AddStepActivity.class);
        intent.putExtra("CurrStep", layoutFeed.getChildCount());
        startActivityForResult(intent,WRITE_STEP);
    }


    public void onClickGuideTitle(View view) {
        editTitle(view);
    }

    private void saveGuide(){

        DocumentReference guideRef = mFirestore.document("Users/" + mFirebaseAuth.getUid() +"/guides/"+guideNum);
        guideRef.set(newGuide);

        //all data is stored in custom objects and added to an array list, we iterate through that to upload
        //the order the objects are stored in the array is the order they're laid out in the layout
        for (int i = 0; i < mGuideDataArrayList.size(); i++){
            //gets the next data object and uploads depending on the type of data we have
            GuideData dataToSave = mGuideDataArrayList.get(i);
            if (dataToSave.getType().equals("Text")){
                TextData textDataPkg = (TextData) dataToSave;
                //textDataPkg.setPlacement(i);//sets the placement to the current index
                uploadText(textDataPkg);
            }else if (dataToSave.getType().equals("Picture")){
                PictureData picDataPkg = (PictureData)dataToSave;
                //picDataPkg.setPlacement(i);
                String picDataPkgUri = picDataPkg.getUri();
                uploadImage(picDataPkg,picDataPkgUri);
            }
        }

        //update the users guide count so the guides can be named differently, 'guide0', 'guide1', etc
        userRef.update("numGuides", guideNum);
        Toast.makeText(CreateNewGuide.this, "Guide Saved!", Toast.LENGTH_SHORT).show();
        for (int i = 0; i< mGuideDataArrayList.size();i++){
            Log.i("GUIDEDATA OBJ: ",mGuideDataArrayList.get(i).getType());
        }
    }

    /**
     * Builds and displays a menu of options for placing a picture
     */
    private void SelectImage(){
        final CharSequence[] items = {"Camera","Gallery","Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(CreateNewGuide.this);
        builder.setTitle("Add an Image");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(items[i].equals("Camera")){
                    //outputPath = camera.pickImage();
                    Intent intent = new Intent(CreateNewGuide.this,CameraViewActivity.class);
                    startActivityForResult(intent,PESDK_RESULT);
                }else if(items[i].equals("Gallery")){
                    Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(intent.createChooser(intent,"Select File"),SELECT_FILE);
                }else if(items[i].equals("Cancel")){
                    if(isSwapping){isSwapping = false;}
                    dialogInterface.dismiss();
                }
            }
        });
        builder.show();
    }


    /**
     * Creates and adds a new ImageView to the guide's layoutFeed
     * @param imageUri the uri of the image being added
     * @return true if success, otherwise false
     */
    public boolean addImage(final Uri imageUri){
        try{
            //Creates the new imageview
            final ImageView newImgView = new ImageView(CreateNewGuide.this);
            //Glide.with(CreateNewGuide.this).load(imageUri).into(newImgView);
            Glide.with(CreateNewGuide.this)
                    .asBitmap()
                    .load(imageUri)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable com.bumptech.glide.request.transition.Transition<? super Bitmap> transition) {
                            //creates object to hold picture data
                            newImgView.setImageBitmap(resource);
                            PictureData picData = new PictureData();
                            picData.setUri(imageUri.toString());
                            picData.setType("Picture");
                            picData.setStepNumber((int)selectedLayout.getTag());
                            //if we are swapping out a picture, replace the old location with new pic, otherwise just add it to end
                            if (isSwapping){
                                picData.setPlacement(currentPictureSwap);
                                mGuideDataArrayList.add(currentPictureSwap,picData);
                                mGuideDataArrayList.remove(currentPictureSwap + 1);
                            }else{
                                picData.setPlacement(currentIndex);
                                //mGuideDataArrayList.add(picData);
                                Log.i("BORBOT MOGI: ","picture aok");
                                addObjectToDataListInOrder(picData);
                                Log.i("BORBOT MOGI: ","picture aokkkkk");
                            }
                        }
                    });

            newImgView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    displayImageOptionMenu(imageUri, v);
                }
            });

            //Adds the tag to the new imageview (the tag is the step number)
            newImgView.setTag(selectedLayout.getTag());
            Log.i("BORBOT IMG TAG", ""+newImgView.getTag());

            //fits the image to the sides, fixes the view bounds, adds padding
            newImgView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            newImgView.setAdjustViewBounds(true);
            newImgView.setPadding(3, 10, 3, 10);

            if(isSwapping){
                selectedLayout.addView(newImgView, currentPictureSwap);
                selectedLayout.removeViewAt(currentPictureSwap + 1);
                currentPictureSwap = 0;
                isSwapping = false;
            }else {
                selectedLayout.addView(newImgView, selectedLayout.getChildCount() - 2);
                currentIndex++;
            }

        }catch(Exception ex){
            Log.i("IMAGE ERROR: ", ex.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Creates and adds a new text block to the layoutFeed
     * @param title of the new step block
     * @param desc of the new step block
     * @return true if success, otherwise false
     */
    public boolean addStep(String title, String desc){
        try{
            final LinearLayout newStepBlock = new LinearLayout(CreateNewGuide.this);
            newStepBlock.generateViewId();
            final LinearLayout newTitleBlock = new LinearLayout(CreateNewGuide.this);
            newStepBlock.setOrientation(LinearLayout.VERTICAL);
            newTitleBlock.setOrientation(LinearLayout.HORIZONTAL);

            //Creates the various text views and changes their visual properties
            TextView mStepNumber = new TextView(CreateNewGuide.this);
            mStepNumber.setTypeface(null, Typeface.BOLD);
            mStepNumber.setTextSize(24);
            mStepNumber.setTextColor(Color.BLACK);
            TextView mStepTitle = new TextView(CreateNewGuide.this);
            mStepTitle.setTypeface(null, Typeface.BOLD);
            mStepTitle.setTextSize(24);
            mStepTitle.setTextColor(Color.BLACK);
//            TextView mStepDesc = new TextView(CreateNewGuide.this);
//            mStepDesc.setTextSize(17);
//            mStepDesc.setTextColor(Color.DKGRAY);
//            mStepDesc.setPadding(5, 10, 5, 10);

            newTitleBlock.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    displayStepOptionsMenu(newTitleBlock.getChildAt(1));
                }
            });
            //Creates the add image button and its on click listener
            Button addImage = new Button(CreateNewGuide.this);
            addImage.setText("Add Image to step " + layoutFeed.getChildCount());
            addImage.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    selectedLayout = newStepBlock;
                    SelectImage();
                }
            });

            //Creates the add description button and its on click listener
            Button addDesc = new Button (CreateNewGuide.this);
            addDesc.setText("Add Text to Step " + layoutFeed.getChildCount());
            addDesc.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    selectedLayout = newStepBlock;
                    Intent intent = new Intent(CreateNewGuide.this,AddDescriptionActivity.class);
                    intent.putExtra("CurrStep", layoutFeed.getChildCount());
                    intent.putExtra("isEditing", false);
                    startActivityForResult(intent,WRITE_DESC);
                }
            });

            int num = layoutFeed.getChildCount();

            //Sets the tag for the step block and step description textview
            //The tag is the step number
            newStepBlock.setTag(num);
//            mStepDesc.setTag(num);

            mStepNumber.setText("Step " +num + " : ");
            mStepTitle.setText(title);
//            mStepDesc.setText(desc);
//            mStepDesc.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    displayTextOptionsMenu(v);
//                }
//            });

            //Set up the views to show everything
            newTitleBlock.addView(mStepNumber);
            newTitleBlock.addView(mStepTitle);
            newStepBlock.addView(newTitleBlock);
//            newStepBlock.addView(mStepDesc);
            newStepBlock.addView(addImage);
            newStepBlock.addView(addDesc);

            //Adds the new step block to the end of the main layout, before the button
            layoutFeed.addView(newStepBlock, layoutFeed.getChildCount()-1);

            //creates an object which holds all the data for the text in the step
//            TextData mTextData = new TextData();
//            mTextData.setStepNumber(num);
//            mTextData.setStepTitle(title);
//            mTextData.setText(desc);
//            mTextData.setPlacement(currentIndex);
//            mTextData.setType("Text");
            //adds the textdata object to our arraylist of data objects for firebase upload
//            mGuideDataArrayList.add(mTextData);

            //adds the starting description to the step block if not null/empty
            if (!desc.equals("")){
                selectedLayout = newStepBlock;
                addDescription(desc);
            }

            currentIndex++;

        }catch (Exception ex){
            ex.getMessage();
            return false;
        }
        return true;
    }

    /**
     * Edits the text of a chosen text block
     * @param newStepDesc of the text block that we are editing
     * @return true if success, otherwise false
     */
    private boolean editDescription(String newStepDesc){
        try {
            selectedTextView.setText(newStepDesc);
            int num = (int)selectedTextView.getTag();//get the tag of the textview, which is the view's id
            //iterate through the data list and update the view in it with the new text
            for (int i = 0; i < mGuideDataArrayList.size();i++){
                //we are only updating text data here, if the type is picture then go to next iteration
                if (mGuideDataArrayList.get(i).getType() == "Picture"){
                    continue;
                }
                TextData data = (TextData)mGuideDataArrayList.get(i);
                if (data.getStepNumber() == num){
                    data.setText(newStepDesc);
                    Log.i("TEXT CHANGED FOR : ",data.getId());
                    break;
                }
            }
        }catch (Exception ex){
            return false;
        }

        return true;
    }

    private boolean addDescription(String newStepDesc) {
        try{
            //Creates a new textview and sets the tag (the tag is the current step number)
            TextView mDescription = new TextView(CreateNewGuide.this);
            mDescription.setTag(selectedLayout.getTag());

            mDescription.setTextSize(17);
            mDescription.setTextColor(Color.DKGRAY);
            mDescription.setPadding(5, 10, 5, 10);
            mDescription.setText(newStepDesc);
            mDescription.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    displayTextOptionsMenu(v);
                }
            });

            //adds the new textblock with the text to the selected step
            selectedLayout.addView(mDescription, selectedLayout.getChildCount() - 2);
            LinearLayout titleBlock = (LinearLayout) selectedLayout.getChildAt(0);
            TextView title = (TextView)titleBlock.getChildAt(1);
            TextData dataToAdd = new TextData();
            dataToAdd.setType("Text");
            //dataToAdd.setPlacement(mGuideDataArrayList.size());
            dataToAdd.setStepNumber((int) selectedLayout.getTag());
            dataToAdd.setStepTitle(title.getText().toString());
            dataToAdd.setText(newStepDesc);
            dataToAdd.setTextStyle(false, false, Color.DKGRAY, 17);
            dataToAdd.setId(dataToAdd.getGuideId() + "TEXT" + mGuideDataArrayList.size());
            Log.i("BORBOT AddDesc: ","text aok");
            addObjectToDataListInOrder(dataToAdd);

        }catch (Exception ex){
            ex.getMessage();
            return false;
        }

        return true;
    }

    /**
     * this function takes a GuideData object and adds it to our overall guide data list
     * in the correct index according to the object's position in the actual guide
     * @param data the object we are adding to the data list
     * @return true if successfully added
     */
    private boolean addObjectToDataListInOrder(GuideData data){
        Log.i("BORBOT MOGI ","add this "+data.getType() + " to data list");
        try{
            int num = (int)selectedLayout.getTag();//get the tag of the stepLayout, which is the view's id
            boolean foundCorrectStep = false;//when we found the step we want to add text to, set to true
            //add the new data object to the data list in the appropriate spot
            for (int i = 0; i <= mGuideDataArrayList.size();i++){
                //if the list is empty
                if (mGuideDataArrayList.isEmpty()){
                    data.setPlacement(0);
                    mGuideDataArrayList.add(data);
                    Log.i("BORBOT MOGI EMPTY",data.getType());
                    break;
                }

                GuideData currentObj = mGuideDataArrayList.get(i);

                //set to true when we have found the step we are adding text to
                if (currentObj.getStepNumber() == num){
                    foundCorrectStep = true;
                }

                //new text is always added to the end of the step by default, we can only know when we reached it when
                //we iterate onto the first object of the next step.
                Log.i("BORBOT COMPARE ",currentObj.getStepNumber() + ", " + num);
                if (foundCorrectStep && (currentObj.getStepNumber() > num)){//first object that isn't equal to id of the new view, which is the first element THIS NEEDS TO BE FIXED
                    String newDataId = "";
                    if (data.getType() == "Text")newDataId=data.getGuideId()+"TEXT"+mGuideDataArrayList.size();
                    else if (data.getType() == "Picture")newDataId=data.getGuideId()+"IMG"+mGuideDataArrayList.size();
                    data.setId(newDataId);
                    data.setPlacement(i-1);
                    mGuideDataArrayList.add(i-1,data);
                    Log.i("BORBOT MOGI MIDDLE",data.getType());
                    break;
                }
            }
            //if we didn't find the next step we haven't added the data to the list
            //that means the step we are adding to is the last step, put the data object at the end of the data list
            if (!mGuideDataArrayList.contains(data)){
                GuideData lastDataObj = mGuideDataArrayList.get(mGuideDataArrayList.size()-1);
                data.setStepNumber(lastDataObj.getStepNumber());
                data.setPlacement(mGuideDataArrayList.size());
                String newDataId = "";
                if (data.getType() == "Text")newDataId=data.getGuideId()+"TEXT"+mGuideDataArrayList.size();
                else if (data.getType() == "Picture")newDataId=data.getGuideId()+"IMG"+mGuideDataArrayList.size();
                data.setId(newDataId);
                mGuideDataArrayList.add(data);
                Log.i("BORBOT MOGI END",data.getType());
                Log.i("END OF DATA LIST","OBJECT ADDED!!!!:)!!!");
            }
        }catch (Exception ex){
            return false;
        }
        return true;
    }

    /**
     * Builds and displays a menu of options for selecting a photo in the guide
     */
    private void displayImageOptionMenu(final Uri imageUri, final View v){
        final CharSequence[] items = {"Delete Picture","View Picture","Edit Picture", "Swap Picture", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(CreateNewGuide.this);
        builder.setTitle("What would you like to do?");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(items[i].equals("Delete Picture")){
                    //Remove picture, indexes shift up
                    ((LinearLayout) v.getParent()).removeView(v);
                }else if(items[i].equals("View Picture")){
                    //calls the activty to view the picture and passes the URI
                    Intent intent = new Intent(CreateNewGuide.this, ViewPhoto.class);
                    intent.putExtra("imageUri", imageUri);
                    startActivity(intent);
                }else if(items[i].equals("Edit Picture")){
//                    Intent intent = new Intent(CreateNewGuide.this, EditPhoto.class);
//                    intent.putExtra("imageUri", imageUri);
//                    startActivity(intent);
                }else if(items[i].equals("Swap Picture")){
                    //sets isSwapping check to true and then calls SelectImage to replace the image we want in the layout hierarchy
                    isSwapping = true;
                    selectedLayout = ((LinearLayout) v.getParent());
                    currentPictureSwap = ((LinearLayout) v.getParent()).indexOfChild(v);
                    SelectImage();
                }else if(items[i].equals("Cancel")){
                    dialogInterface.dismiss();
                }
            }
        });
        builder.show();
    }

    /**
     * Builds and displays a menu of options for selecting a text block in the guide
     */
    private void displayTextOptionsMenu(final View v){
        final CharSequence[] items = {"Delete Text","Edit Text", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(CreateNewGuide.this);
        builder.setTitle("What would you like to do?");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(items[i].equals("Delete Text")) {
                    //Removes the selected Textview
                    ((LinearLayout) v.getParent()).removeView(v);
                }else if(items[i].equals("Edit Text")){
                    //Stores the selected text view to edit later
                    selectedTextView = (TextView)v;
                    //Starts a new activity
                    String currText = selectedTextView.getText().toString();
                    Intent intent = new Intent(CreateNewGuide.this,AddDescriptionActivity.class);
                    intent.putExtra("CurrText", currText);
                    intent.putExtra("isEditing", true);
                    startActivityForResult(intent,EDIT_DESC);
                }else if(items[i].equals("Cancel")){
                    dialogInterface.dismiss();
                }
            }
        });
        builder.show();
    }

    /**
     * Builds and displays a menu of options for selecting the step title
     */
    private void displayStepOptionsMenu(final View v){
        final CharSequence[] items = {"Edit step title","Delete entire step", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(CreateNewGuide.this);
        builder.setTitle("What would you like to do?");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(items[i].equals("Edit step title")) {
                    editTitle(v);
                }else if(items[i].equals("Delete entire step")){
                    deleteStep(v);
                }else if(items[i].equals("Cancel")){
                    dialogInterface.dismiss();
                }
            }
        });
        builder.show();
    }

    /**
     * Builds and displays a edittext to allow the user to edit the title
     */
    public void editTitle(View v){
        final TextView textview = (TextView) v;
        String currentText = textview.getText().toString();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rename Title");
        final EditText input = new EditText(this);
        input.setText(currentText);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        builder.setView(input);

        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                textview.setText(input.getText().toString());
                newGuide.setTitle(input.getText().toString());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    /**
     * Uploads a text block to the firestore database
     * @param text object to be uploaded
     */
    public void uploadText(TextData text){
        Log.i("UPLOADTEXT: ", "STARTING UPLOAD");
        if (text.getText().isEmpty()){return;}

        //Check to see if the current step has an object saved in the db
        uploadStep(text);

        //if the text object does not have an id, that means that its new and hasn't been saved into the db
        //give it an id so that in future saves we won't create a new doc in db and will instead overwrite old save
        if (text.getId() == null || text.getId() == ""){
            text.setId(UUID.randomUUID().toString());
        }
        DocumentReference textBlockRef = textData.document("textBlock-" + text.getId());
        textBlockRef.set(text);
        textBlockNum++;
    }

    private void uploadStep(final GuideData data) {
        //if the current step has not had an object representing it stored in the db
        //then we will make one here before saving the text
        final DocumentReference step = mFirestore.document(
                "Users/" + mFirebaseAuth.getUid() +"/guides/"+guideNum+"/stepData/step" + data.getStepNumber());
        step.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()){
                        //step exists so we don't have to do anything more
                        return;
                    }else{
                        //step doesn't exist so we create it
                        Map<String,Object> dataToSave = new HashMap<String,Object>();
                        dataToSave.put("stepNumber",data.getStepNumber());
                        dataToSave.put("stepTitle",data.getStepTitle());
                        step.set(dataToSave);
                    }
                }
            }
        });
    }

    /**
     * Uploads an image reference to firebase storage and the firestore database
     * @param img object to be uploaded
     * @param picUri the uri string of the picture being uploaded to storage
     */
    public void uploadImage(final PictureData img, String picUri){
        Log.i("UPLOADIMAGE: ","Starting upload");

        //Check to see if the current step has an object saved in the db
        uploadStep(img);

        //Uri newUri = Uri.fromFile(new File(picUri));
        Uri newUri = Uri.parse(picUri);
        /*
        TODO: Uploading images should be given its own background thread so that the app does not freeze for a long time
        TODO: Look up what threads are and how to use them
        TODO: When uploading images, only the last image has the imgPath saved to the object, dont know why (all images are still uploaded to storage)
         */

        Glide.with(CreateNewGuide.this)
                .asBitmap()
                .load(newUri)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        //create a byte array output stream to prepare the image bitmap for upload
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        //resource bitmap is compressed and stored into baos
                        resource.compress(Bitmap.CompressFormat.PNG,100,baos);
                        byte[] data = baos.toByteArray();//outstream is converted into byte array for upload

                        //the path of the image in firebase storage, is set as the imgPath for our PictureData obj for retrieval purposes
                        String path = "guideimages/users/" + mFirebaseAuth.getUid() + "/guide"+guideNum+"/" + UUID.randomUUID() + ".png";
                        img.setImgPath(path);
                        StorageReference imgRef = mStorage.getReference(path);

                        //metadata is set for the image to be uploaded
                        StorageMetadata metadata = new StorageMetadata.Builder()
                                .setCustomMetadata(mFirebaseAuth.getUid(),"guide"+guideNum+"/imgBlock" + imgBlockNum)
                                .build();

                        //image byte array is uploaded with our metadata
                        UploadTask uploadTask = imgRef.putBytes(data,metadata);
                        Log.i("UPLOADIMAGE: ","uploadtask");

                        //on success, do something, otherwise go to the error page and tell us what went wrong
                        uploadTask.addOnSuccessListener(CreateNewGuide.this,new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                //Can create a hashmap to upload but instead we use custom objects
                                DocumentReference imgBlockRef = picData.document("imgBlock" + imgBlockNum);
                                imgBlockRef.set(img);
                                Log.i("UPLOADIMAGE: ","please be done");
                                imgBlockNum++;
                            }
                        }).addOnFailureListener(CreateNewGuide.this, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Intent intent = new Intent(CreateNewGuide.this, ErrorActivity.class);
                                intent.putExtra("ERRORS", e.getLocalizedMessage());
                                startActivity(intent);
                                return;
                            }
                        });
                    }
                });

    }

    /**
     * Fixes the step numbers when a step is deleted
     * When the steps are reordered that means we have to update the step numbers for each item
     * in the data list and the tags for each data view so those edits are done too
     */
    private void reorderSteps(){
        int dataListPointer = 0;//used to iterate through the datalist by stepCount instead of one by one

        //Loops through the layout Feed and its children to set the step number to the correct step
        for(int i = 0; i < layoutFeed.getChildCount() - 1; i++){
            LinearLayout stepLayout = (LinearLayout) layoutFeed.getChildAt(i);
            LinearLayout titleLayout = (LinearLayout) stepLayout.getChildAt(0);
            TextView stepTitle = (TextView) titleLayout.getChildAt(0);
            stepTitle.setText("Step " + (i + 1) + " : ");
            reorderTags(stepLayout,(i+1));//reorders the tags for each view in each step's linear layout

            //get the first object of the current step
            GuideData data = mGuideDataArrayList.get(i+dataListPointer);

            //if data's number is more than the number we are changing to, then correct it
            //ex. removed step 1, step 2 is now the new step 1, therefore we must decrement all blocks from step 2,
            //otherwise its already correct so leave it
            int currStepNum = data.getStepNumber();//the step num of the current data block we are on
            int stepToCheck = currStepNum;//the current step whose data blocks we are working on
            while (currStepNum > (i+1)){
                data.setStepNumber(currStepNum-1);//lower the step number by 1 to its proper number
                //get the next data object in the list so we can see if its in the same step as our current object
                dataListPointer++;//we are moving to the next data object in the list
                //if we have reached the last element, break the loop
                if ((i+dataListPointer) >= mGuideDataArrayList.size()){
                    break;
                }
                data = mGuideDataArrayList.get(i+dataListPointer);
                currStepNum = data.getStepNumber();
                //if this is not true, then we have reached a data object of the next step, break and move on
                if (currStepNum != stepToCheck){
                    dataListPointer--;
                    break;
                }
            }

            //Changes the buttons text to the correct step number
            Button addStep = (Button) stepLayout.getChildAt(stepLayout.getChildCount() - 2);
            addStep.setText("Add Image to step " + (i + 1));
            Button addDesc = (Button) stepLayout.getChildAt(stepLayout.getChildCount() - 1);
            addDesc.setText("Add Text to Step " + (i + 1));
        }
    }

    /**
     * Called whenever we have to re-order the views in the guide layout
     * Re-sets the tags of the guide data views to their new proper step values.
     * @param stepLayout that we are editing the view tags of
     * @param stepNum the new step number that we are changing the tags to
     */
    private void reorderTags(LinearLayout stepLayout, int stepNum){
        //we start at 1 because the view at index 0 is the title of the step
        for (int i = 1; i < stepLayout.getChildCount();i++){
            View view = stepLayout.getChildAt(i);
            view.setTag(stepNum);//set the tag of the view to the new stepNum
        }
    }


    /**
     * Asks the user if they want to delete the selected step, then does it.
     */
    public void deleteStep(final View v){
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to delete this step? All unsaved changes will be lost.")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //IT WORKS NOW TRUST ME - CA (July 23. 2018)
                        
                        //Gets the parent of the parent layout and deletes the whole parent (step layout)
                        LinearLayout titleLayout = ((LinearLayout) v.getParent());
                        LinearLayout stepLayout = ((LinearLayout) titleLayout.getParent());

                        //iterates through the data list and removes all elements of the deleted step
                        Iterator<GuideData> iterator = mGuideDataArrayList.iterator();
                        while (iterator.hasNext()){
                            GuideData data = iterator.next();
                            if (data.getStepNumber() == (int)stepLayout.getTag()){
                                iterator.remove();
                            }
                        }
                        ((LinearLayout) stepLayout.getParent()).removeView(stepLayout);

                        reorderSteps();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    /**
     * Adds an image or some text
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK) {
            if(requestCode == Picker.PICK_IMAGE_CAMERA){
                if (camera == null){
                    camera = new CameraImagePicker(CreateNewGuide.this,outputPath);
                    camera.setImagePickerCallback(new ImagePickerCallback() {
                        @Override
                        public void onImagesChosen(List<ChosenImage> list) {
                            Uri imagePath = Uri.parse(list.get(0).getQueryUri());
                            addImage(imagePath);
                        }

                        @Override
                        public void onError(String s) {

                        }
                    });
                }
                camera.submit(data);
            }
            else if (requestCode == SELECT_FILE){
                Uri selectedImageUri = data.getData();
                addImage(selectedImageUri);
            } else if (requestCode == WRITE_STEP){
                if (data == null){
                    return;
                }
                newStepTitle = AddStepActivity.getTitle(data);
                newStepDesc = AddStepActivity.getDesc(data);
                addStep(newStepTitle, newStepDesc);
            } else if (requestCode == WRITE_DESC){
                if (data == null){
                    return;
                }
                newDesc = AddDescriptionActivity.getNewDesc(data);
                addDescription(newDesc);
            }else if (requestCode == EDIT_DESC){
                if (data == null){
                    return;
                }
                newDesc = AddDescriptionActivity.getNewDesc(data);
                editDescription(newDesc);
            }else if (requestCode == PESDK_RESULT){
                if (data == null)return;
                Uri imageUri = CameraViewActivity.getSourceUri(data);
                addImage(imageUri);
            }
        }
    }

    /**
     * Asks the user if they really want to go back and erease all unsaved data
     */
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to go back? Any unsaved changes will be lost.")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        CreateNewGuide.this.finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outstate){
        //need a save path in case the activity is killed
        //will need to re-initialize cameraimagepicker
        outstate.putString("ImagePath",outputPath);
        outstate.putInt("guideNum",guideNum);
        //outstate.putInt("TEXTBLOCKNUM",textBlockNum);
       // outstate.putInt("IMGBLOCKNUM",imgBlockNum);
        super.onSaveInstanceState(outstate);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState){
        //after activity is restored, need to re-initialize these 2 values
        // in order to re-initialize CameraImagePicker
        if(savedInstanceState != null){
            outputPath = savedInstanceState.getString("ImagePath", null);
            guideNum = savedInstanceState.getInt("guideNum",0);
        }
        super.onRestoreInstanceState(savedInstanceState);
    }
}
