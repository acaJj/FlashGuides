package com.wew.azizchr.guidezprototype;

import android.support.annotation.NonNull;

import java.util.Collection;
import java.util.Iterator;

/**
 * Created by Jeffrey on 2018-06-01.
 */

public class TextData extends GuideData {
    private String text; //all separate text sections that the user adds to the step
    private boolean isBold;
    private boolean isItalic;
    private String color; //color of the text
    private int size; //font size of the text

    public TextData() {

    }

    public TextData(String id, String type, int placement, String guideId, String stepTitle) {
        super(id, type, placement, guideId, stepTitle);
    }

    public TextData(String id, String type, int placement, String guideId, boolean isBold, boolean isItalic, String color) {
        super(id, type, placement, guideId);
        this.isBold = isBold;
        this.isItalic = isItalic;
        this.color = color;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
