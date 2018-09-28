package com.wew.azizchr.guidezprototype;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

/**
 * Created by Jeffrey on 2018-09-24.
 */

public class ImageUploadAsyncTask extends AsyncTask<ArrayList<PictureData>, Integer, Long>{

    @Override
    protected Long doInBackground(ArrayList<PictureData>[] images) {
        for (PictureData image: images[0]){
            Uri imageUri = Uri.parse(image.getUri());

        }
        return null;
    }
}
