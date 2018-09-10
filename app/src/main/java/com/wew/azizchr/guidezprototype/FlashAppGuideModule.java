package com.wew.azizchr.guidezprototype;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.StorageReference;

import java.io.InputStream;

/**
 * A custom AppGuideModule to handle image loading. We register a model loader in the registry
 * to let Glide handle the logic behind when to use a specific model loader to load an image
 * Created by Jeffrey on 2018-09-10.
 */

@GlideModule
public class FlashAppGuideModule extends AppGlideModule {

    @Override
    public void registerComponents(Context context, Glide glide, Registry registry){
        //register FirebaseImageLoader to handle StorageReference
        registry.append(StorageReference.class, InputStream.class, new FirebaseImageLoader.Factory());
    }
}
