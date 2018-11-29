package com.wew.azizchr.guidezprototype;

import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;

import com.github.chrisbanes.photoview.PhotoView;

/**
 * Created by Chris
 * Lets the user view an image using the photoview API.
 */

public class ViewPhoto extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**
         * Makes the application fullscreen (removes notification bar)
         */
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setBackgroundColor(Color.BLACK);
        setContentView(R.layout.activity_view_photo);

        /**
         * Gets URI passed from photo, and stores it in the photoview
         */
        Uri myUri = getIntent().getParcelableExtra("imageUri");
        PhotoView photoView = (PhotoView) findViewById(R.id.photo_view);
        photoView.setImageURI(myUri);
    }
}
