package com.wew.azizchr.guidezprototype;

import android.graphics.Color;
import android.support.annotation.NonNull;

import com.google.firebase.firestore.Blob;

import java.util.Collection;
import java.util.Iterator;

/**
 * Created by Jeffrey on 2018-06-01.
 * Models the data for a text block
 */

public class TextData extends GuideData {
    private final static String TYPE_TEXT = "Text";
    private final static String TYPE_LINK = "Link";

    private Blob text; //all separate text sections that the user adds to the step
    private boolean isBold;
    private boolean isItalic;
    private int color; //color of the text
    private int size; //font size of the text
    private String textType;

    public TextData(String id, String type, int placement, String guideId) {
        super(id,type, placement, guideId);
    }

    public Blob getText() {
        return text;
    }

    public void setText(Blob text) {
        if (text != null){
            this.text = text;
        }
    }

    public void stringToBlob(String text){
        if (text == null)return;

        if (!text.equals("")){
            byte[] bytes = text.getBytes();
            Blob data = Blob.fromBytes(bytes);
            setText(data);
        }
    }

    public String getStringFromBlob(){
        byte[] newBytes = this.text.toBytes();

        return new String(newBytes);
    }

    public boolean isBold() {
        return isBold;
    }

    public void setBold(boolean bold) {
        isBold = bold;
    }

    public boolean isItalic() {
        return isItalic;
    }

    public void setItalic(boolean italic) {
        isItalic = italic;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setTextStyle(boolean bold, boolean italic, int color, int size){
        this.isBold = bold;
        this.isItalic = italic;
        this.color = color;
        this.size = size;
    }

    public String getTextType() {
        return textType;
    }

    public void setTextType(String textType) {
        if (textType == null)return;

        if (textType.equals(TYPE_LINK) || textType.equals(TYPE_TEXT)){
            this.textType = textType;
        }
    }
}
