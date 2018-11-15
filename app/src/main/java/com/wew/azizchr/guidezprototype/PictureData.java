package com.wew.azizchr.guidezprototype;

import android.graphics.Bitmap;
import android.net.Uri;
//import java.io.ByteArrayOutputStream;

/**
 * Created by Jeffrey on 2018-06-01.
 *
 * Image Bitmap will be taken from here instead of being done in the uploadImage method
 *
 */

public class PictureData extends GuideData {

    private String imgPath;
    private String uri;

    public float longitude,latitude;
    public boolean hasMap;

    public PictureData() {
    }

    public PictureData(String id, String type, int placement, String guideId) {
        super(id, type, placement, guideId);
    }

    /*public byte[] getImageData(){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        img.compress(Bitmap.CompressFormat.PNG,100,baos);
        return baos.toByteArray();
    }*/

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
