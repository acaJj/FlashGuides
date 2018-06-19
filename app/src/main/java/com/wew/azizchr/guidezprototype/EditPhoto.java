package com.wew.azizchr.guidezprototype;

import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ahmedadeltito.photoeditorsdk.BrushDrawingView;
import com.ahmedadeltito.photoeditorsdk.PhotoEditorSDK;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

public class EditPhoto extends AppCompatActivity {

    private RelativeLayout parent;
    private BrushDrawingView brushView;
    private ImageView img;
    private Button btnSaveEditedPic;
    private ImageDrawingView canvas;

    private PhotoEditorSDK mPhotoEditorSDK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_photo);

        //parent = findViewById(R.id.editorParent);
        //brushView = findViewById(R.id.brushView);
        //img = findViewById(R.id.imageToEdit);
        btnSaveEditedPic = findViewById(R.id.btnSaveEditedPic);
        canvas = findViewById(R.id.photoEditWindow);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        canvas.init(metrics);

        /**
         * Gets URI passed from photo, and stores it in the photoview
         */
        Uri myUri = getIntent().getParcelableExtra("imageUri");
        Glide.with(EditPhoto.this)
                .asBitmap()
                .load(myUri)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        canvas.setImage(resource);
         //               img.setImageBitmap(resource);
                    }
                });

        /*mPhotoEditorSDK = new PhotoEditorSDK.PhotoEditorSDKBuilder(EditPhoto.this)
                .parentView(parent)
                .childView(img)
                .deleteView(img)
                .brushDrawingView(brushView)
                .buildPhotoEditorSDK();

        mPhotoEditorSDK.setBrushDrawingMode(true);
    */}
}
