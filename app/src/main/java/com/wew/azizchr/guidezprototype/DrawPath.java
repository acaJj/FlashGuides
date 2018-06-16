package com.wew.azizchr.guidezprototype;

import android.graphics.Path;

/**
 * Created by Jeffrey on 2018-06-15.
 */

public class DrawPath {
    public int color;
    public boolean emboss;
    public boolean blur;
    public int strokeWidth;
    public Path path;

    public DrawPath(int color, int strokeWidth, Path path){
        this.color = color;
        this.strokeWidth = strokeWidth;
        this.path = path;
    }

    public DrawPath(int color, boolean emboss, boolean blur, int strokeWidth, Path path) {
        this.color = color;
        this.emboss = emboss;
        this.blur = blur;
        this.strokeWidth = strokeWidth;
        this.path = path;
    }
}
