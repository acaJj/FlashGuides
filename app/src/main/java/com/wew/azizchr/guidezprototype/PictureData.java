package com.wew.azizchr.guidezprototype;

import com.google.firebase.firestore.GeoPoint;

/**
 * Created by Jeffrey on 2018-06-01.
 *
 *
 */

public class PictureData extends GuideData {

    private String imgPath;
    private String uri;

    public double latitude;
    public double longitude;

    public PictureData() {
    }

    public PictureData(String id, String type, int placement, String guideId) {
        super(id, type, placement, guideId);
    }

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

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}