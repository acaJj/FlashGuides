package com.wew.azizchr.guidezprototype;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kbeanie.multipicker.api.CameraImagePicker;
import com.kbeanie.multipicker.api.ImagePicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CreateNewGuide extends AppCompatActivity {

    private static final String TEXT_TAG = "TEXT";
    private static final String IMG_TAG = "IMG";
    private static final int SELECT_FILE =0;
    private static final int WRITE_STEP =1;
    private static final int WRITE_DESC =2;

    private String outputPath;
    private String newStepTitle;
    private String newStepDesc;

    private int guideNum = 0;//the current guide index thing
    private int textBlockNum; //the current number of textBlocks
    private int imgBlockNum;//the current number of image blocks
    private int currentIndex;
    private int currentStep;
    private int totalEntries;
    private int currentPictureSwap;
    private Boolean isSwapping;

    private CameraImagePicker camera;
    public LinearLayout layoutFeed;
    private LayoutInflater inflater = new LayoutInflater(CreateNewGuide.this) {
        @Override
        public LayoutInflater cloneInContext(Context context) {
            return null;
        }
    };

    TextView mNewGuideTitle;
    Button mAddImage;
    private Button mSave;

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
    private Guide newGuide;

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

        //Gets the guide name variable from previous activity and puts it in the title
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if(bundle != null){
            guideTitle = bundle.getString("GUIDE_TITLE");
        } else{
            guideTitle = "Guide (No title)";
        }
        mNewGuideTitle.setText(guideTitle);

        newGuide.setAuthor(mCurrentUser.getEmail());
        newGuide.setTitle(mCurrentUser.getEmail()+" / "+guideTitle);

        userRef = mFirestore.document("Users/" + mFirebaseAuth.getUid());
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot documentSnapshot = task.getResult();
                guideNum = documentSnapshot.getLong("numGuides").intValue();
                guideNum++;
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
        currentStep = 1;
        totalEntries = 0;
        isSwapping = false;
        layoutFeed = (LinearLayout) findViewById(R.id.newGuideLayoutFeed);
        //mAddImage = (Button) findViewById(R.id.btnAddImage);
        mSave = findViewById(R.id.btnSaveGuide);
        //mAddImage.setVisibility(View.GONE);

        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveGuide();
            }
        });
    }

    public void onClickGallery(View view) {
        SelectImage();
    }

    public void onClickStep(View view) {
        Intent intent = new Intent(CreateNewGuide.this,AddStepActivity.class);
        intent.putExtra("CurrStep", currentStep);
        startActivityForResult(intent,WRITE_STEP);
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
                textDataPkg.setPlacement(i);//sets the placement to the current index
                uploadText(textDataPkg);
            }else if (dataToSave.getType().equals("Picture")){
                PictureData picDataPkg = (PictureData)dataToSave;
                picDataPkg.setPlacement(i);
                uploadImage(picDataPkg,picDataPkg.getUri());
            }
        }

        //update the users guide count so the guides can be named differently, 'guide0', 'guide1', etc
        userRef.update("numGuides", guideNum);
        Toast.makeText(CreateNewGuide.this, "Guide Saved!", Toast.LENGTH_SHORT).show();
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
                    outputPath = camera.pickImage();
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
            ImageView newImgView = new ImageView(CreateNewGuide.this);
            Glide.with(this).load(imageUri).into(newImgView);
            Glide.with(CreateNewGuide.this)
                    .asBitmap()
                    .load(imageUri)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable com.bumptech.glide.request.transition.Transition<? super Bitmap> transition) {
                            //creates object to hold picture data
                            PictureData picData = new PictureData();
                            picData.setUri(imageUri.toString());
                            picData.setPlacement(currentIndex);
                            picData.setType("Picture");
                            mGuideDataArrayList.add(picData);
                        }
                    });

            newImgView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DecideImage(imageUri, v);
                }
            });
            newImgView.setTag(IMG_TAG);

            //fits the image to the sides, fixes the view bounds, adds padding
            newImgView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            newImgView.setAdjustViewBounds(true);
            newImgView.setPadding(3, 10, 3, 10);

            if(isSwapping){
                layoutFeed.addView(newImgView, currentPictureSwap);
                layoutFeed.removeViewAt(currentPictureSwap + 1);
                currentPictureSwap = 0;
                isSwapping = false;
            }else {
                layoutFeed.addView(newImgView, currentIndex);
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
            LinearLayout newStepBlock = new LinearLayout(CreateNewGuide.this);
            LinearLayout newTitleBlock = new LinearLayout(CreateNewGuide.this);
            newStepBlock.setOrientation(LinearLayout.VERTICAL);
            newTitleBlock.setOrientation(LinearLayout.HORIZONTAL);

            TextView mStepNumber = new TextView(CreateNewGuide.this);
            mStepNumber.setTypeface(null, Typeface.BOLD);
            mStepNumber.setTextSize(24);
            mStepNumber.setTextColor(Color.BLACK);
            TextView mStepTitle = new TextView(CreateNewGuide.this);
            mStepTitle.setTypeface(null, Typeface.BOLD);
            mStepTitle.setTextSize(24);
            mStepTitle.setTextColor(Color.BLACK);
            TextView mStepDesc = new TextView(CreateNewGuide.this);
            mStepDesc.setTextSize(17);
            mStepDesc.setTextColor(Color.DKGRAY);
            mStepDesc.setPadding(5, 10, 5, 10);

            Button addImage = new Button(CreateNewGuide.this);
            addImage.setText("Add Image to step " + currentStep);
            Button addDesc = new Button (CreateNewGuide.this);
            addDesc.setText("Add Text to Step " + currentStep);

            mStepNumber.setText("Step" +currentStep + " : ");
            mStepTitle.setText(title);
            mStepDesc.setText(desc);

            //we can use this later on to
            /*
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DecideText(v);
                }
            });
            */

            //Set up the views to show everything
            newTitleBlock.addView(mStepNumber);
            newTitleBlock.addView(mStepTitle);
            newStepBlock.addView(newTitleBlock);
            newStepBlock.addView(mStepDesc);
            newStepBlock.addView(addImage);
            newStepBlock.addView(addDesc);

            layoutFeed.addView(newStepBlock, currentIndex);

            //creates an object which holds all the data for the text in the step
            TextData mTextData = new TextData();
            mTextData.setText(desc);
            mTextData.setPlacement(currentIndex);
            mTextData.setType("Text");
            //adds the textdata object to our arraylist of data objects for firebase upload
            mGuideDataArrayList.add(mTextData);
            currentIndex++;
            currentStep++;

            if(currentStep > 1){
                mAddImage.setVisibility(View.VISIBLE);
            }
        }catch (Exception ex){
            ex.getMessage();
            return false;
        }
        return true;
    }


    /**
     * Builds and displays a menu of options for selecting a photo in the guide
     */
    private void DecideImage(final Uri imageUri, final View v){
        final CharSequence[] items = {"Delete Picture","View Picture","Edit Picture", "Swap Picture", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(CreateNewGuide.this);
        builder.setTitle("What would you like to do?");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(items[i].equals("Delete Picture")){
                    //Remove picture, indexes shift up
                    layoutFeed.removeView(v);
                    currentIndex--;
                }else if(items[i].equals("View Picture")){
                    //calls the activty to view the picture and passes the URI
                    Intent intent = new Intent(CreateNewGuide.this, ViewPhoto.class);
                    intent.putExtra("imageUri", imageUri);
                    startActivity(intent);
                }else if(items[i].equals("Edit Picture")){
                    Intent intent = new Intent(CreateNewGuide.this, EditPhoto.class);
                    intent.putExtra("imageUri", imageUri);
                    startActivity(intent);
                }else if(items[i].equals("Swap Picture")){
                    isSwapping = true;
                    currentPictureSwap = layoutFeed.indexOfChild(v);
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
    private void DecideText(final View v){
        final CharSequence[] items = {"Delete Text","Edit Text", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(CreateNewGuide.this);
        builder.setTitle("What would you like to do?");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(items[i].equals("Delete Text")) {
                    //Remove text, indexes shift up

                    //add functionality to cannot remove the first step
                        layoutFeed.removeView(v);
                        currentIndex--;
                        currentStep--;
                }else if(items[i].equals("Edit Text")){
                    //needs to do something similar to swap picture where you can set the mode to edidting and replace text instead of adding a new index.

                    //TextView text = (TextView) v;
                    //Intent intent = new Intent(CreateNewGuide.this, ViewPhoto.class);
                    //intent.putExtra("CURRENT TEXT", text.getText().toString());
                    //startActivity(intent);
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
    public void onClickGuideTitle(View view) {
        String newGuideTitle = mNewGuideTitle.getText().toString();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Guide Title");
        final EditText input = new EditText(this);
        input.setText(newGuideTitle);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        builder.setView(input);

        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mNewGuideTitle.setText(input.getText().toString());
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

    //Uploads a text block to the firestore database
    public void uploadText(TextData text){
        Log.i("UPLOADTEXT: ", "STARTING UPLOAD");
        if (text.getText().isEmpty()){return;}
        //Map<String,Object> dataToSave = new HashMap<String, Object>();
        //dataToSave.put(TEXT_VALUE_KEY,text);
        //dataToSave.put(PLACEMENT_KEY,place);
        DocumentReference textBlockRef = textData.document("textBlock" + textBlockNum);
        /*mDocRef.set(dataToSave).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Log.d("UPLOADTEXT: ", "onComplete: Text Block has been saved");
                }else{
                    Log.e("UPLOADTEXT: ", "onComplete: ",task.getException() );
                }
            }
        });
*/
        textBlockRef.set(text);
        textBlockNum++;
    }

    //Uploads an image reference to firebase storage and the firestore database
    public void uploadImage(final PictureData img, String picUri){
        Log.i("UPLOADIMAGE: ","Starting upload");

        Uri newUri = Uri.fromFile(new File(picUri));

        Glide.with(CreateNewGuide.this)
                .asBitmap()
                .load(newUri)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        resource.compress(Bitmap.CompressFormat.PNG,100,baos);
                        byte[] data = baos.toByteArray();

                        String path = "guideimages/users/" + mFirebaseAuth.getUid() + "/guide"+guideNum+"/" + UUID.randomUUID() + ".png";
                        img.setImgPath(path);
                        StorageReference imgRef = mStorage.getReference(path);

                        StorageMetadata metadata = new StorageMetadata.Builder()
                                .setCustomMetadata(mFirebaseAuth.getUid(),"guide"+guideNum+"/imgBlock" + imgBlockNum)
                                .build();

                        UploadTask uploadTask = imgRef.putBytes(data,metadata);
                        Log.i("UPLOADIMAGE: ","uploadtask");
                        uploadTask.addOnSuccessListener(CreateNewGuide.this,new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                //Can create a hashmap to upload but instead we use custom objects
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

        DocumentReference imgBlockRef = picData.document("imgBlock" + imgBlockNum);
        imgBlockRef.set(img);
        Log.i("UPLOADIMAGE: ","please be done");
        imgBlockNum++;
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
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outstate){
        //need a save path in case the activity is killed
        //will need to re-initialize cameraimagepicker
        outstate.putString("ImagePath",outputPath);
        //outstate.putInt("guideNum",guideNum);
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
        }
        super.onRestoreInstanceState(savedInstanceState);
    }
}
