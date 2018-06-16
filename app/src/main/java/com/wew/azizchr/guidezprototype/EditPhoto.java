package com.wew.azizchr.guidezprototype;

import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.agsw.FabricView.FabricView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.github.chrisbanes.photoview.PhotoView;

public class EditPhoto extends AppCompatActivity {

    private ImageDrawingView canvas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_photo);
        canvas = findViewById(R.id.photoEditWindow);

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
                        canvas.setImageBitmap(resource);
                    }
                });
    }
}
