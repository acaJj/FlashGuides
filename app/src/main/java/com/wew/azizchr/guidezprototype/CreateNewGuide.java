package com.wew.azizchr.guidezprototype;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.kbeanie.multipicker.api.CameraImagePicker;
import com.kbeanie.multipicker.api.ImagePicker;
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenImage;

import java.util.ArrayList;
import java.util.List;

public class CreateNewGuide extends AppCompatActivity {



    private static final String NAME_KEY = "name";
    private static final String PLACEMENT_KEY = "placement";
    private static final String TEXT_VALUE_KEY = "text";
    private static final String IMG_URL_KEY = "imgUrl";
    private static final String IMG_TAG = "IMG";
    private static final String TEXT_TAG = "TEXT";
    private static final String NEW_GUIDE = "NEW_GUIDE";
    private static final int SELECT_FILE =0;
    private static final int WRITE_TEXT =1;

    private String outputPath;

    int index;

    private CameraImagePicker camera;
    public LinearLayout layoutFeed;
    TextView mNewGuideTitle;

    String guideTitle = "NULL";
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

        mNewGuideTitle = (TextView) findViewById(R.id.txtNewGuideTitle);

        //Gets the guide name variable from previous activity and puts it in the title
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if(bundle != null){
            guideTitle = bundle.getString("GUIDE_TITLE");
        } else{
            guideTitle = "New Guide (No title)";
        }
        mNewGuideTitle.setText(guideTitle);

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

        index = 0;
        layoutFeed = (LinearLayout) findViewById(R.id.newGuideLayoutFeed);
    }

    public void onClickGallery(View view) {
        SelectImage();
    }

    public void onClickText(View view) {
        Intent intent = new Intent(CreateNewGuide.this,TextBlockWriterActivity.class);
        startActivityForResult(intent,WRITE_TEXT);
    }


    /**
     * Builds and displays a menu of options for placing a picture
     */
    private void SelectImage(){
        final CharSequence[] items = {"Camera","Gallery","Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(CreateNewGuide.this);
        builder.setTitle("Add Image");
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
                            PictureData picData = new PictureData();
                            picData.setImg(resource);
                            picData.setPlacement(index);
                            //uploadImage(picData, resource);
                        }
                    });

            newImgView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Gotta put modal here so you can do the shit
                    Intent intent = new Intent(CreateNewGuide.this, ViewPhoto.class);
                    intent.putExtra("imageUri", imageUri);
                    startActivity(intent);
                }
            });
            newImgView.setTag(IMG_TAG);

            //fits the image to the sides, fixes the view bounds, adds padding
            newImgView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            newImgView.setAdjustViewBounds(true);
            newImgView.setPadding(3, 10, 3, 10);
            layoutFeed.addView(newImgView, index);
            index++ ;

        }catch(Exception ex){
            Log.i("IMAGE ERROR: ", ex.getMessage());
            return false;
        }
        return true;
    }


    private void DecideImage(){
        final CharSequence[] items = {"Delete Picture","View Picture", "Swap Picture", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(CreateNewGuide.this);
        builder.setTitle("What would you like to do?");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(items[i].equals("Delete Picture")){
                    //Remove picture, call method to shift rest of indexes up
                }else if(items[i].equals("View Picture")){
                    Intent intent = new Intent(CreateNewGuide.this, ViewPhoto.class);
                    //intent.putExtra("imageUri", imageUri); - Decide image will pass the final URI to this method from Add Image
                    startActivity(intent);
                }else if(items[i].equals("Edit Picture")){
                    //Delete current picture, swap picture with whatever they want. Use SelectImage() maybe?
                }else if(items[i].equals("Cancel")){
                    dialogInterface.dismiss();
                }
            }
        });
        builder.show();
    }
}
