package com.wew.azizchr.guidezprototype;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jeffrey on 2018-06-15.
 */

//TODO: This crashes because when you try to make it a view in the xml shit goes wrong idk why

public class ImageDrawingView extends android.support.v7.widget.AppCompatImageView implements View.OnTouchListener {

    public static int PEN_SIZE = 10;
    public static final int DEFAULT_COLOR = Color.BLACK;
    private static final float TOUCH_TOLERANCE = 4;

    private Bitmap mNewBitmap;
    private Canvas mCanvas;
    private Paint mInk;

    private Path mPath;
    private ArrayList<DrawPath> paths = new ArrayList<>();
    private float mX,mY;
    private int currentColor, currentSize;

    public ImageDrawingView(Context context){
        super(context);
        setOnTouchListener(this);
    }

    public void init(){
        currentColor = DEFAULT_COLOR;
        currentSize = PEN_SIZE;
    }

    public void setImage(Bitmap bitmap){
        mCanvas = new Canvas();
        mInk = new Paint(Paint.DITHER_FLAG);
        mInk.setColor(Color.RED);
        mInk.setStrokeWidth(5);
        Matrix matrix = new Matrix();
        mCanvas.drawBitmap(bitmap,matrix,mInk);
        setImageBitmap(bitmap);
    }

    private void touchStart(float x, float y){
        mPath = new Path();
        DrawPath dPath = new DrawPath(currentColor,currentSize,mPath);

        paths.add(dPath);
        mPath.reset();
        mPath.moveTo(x,y);
        mX = x;
        mY = y;
    }

    private void touchMove(float x, float y){
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE){
            mPath.quadTo(mX,mY,(x+mX)/2,(y+mY)/2);
            mX = x;
            mY = y;
        }
    }

    private void touchUp(){
        mPath.lineTo(mX,mY);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                touchStart(x,y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x,y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                invalidate();
                break;
        }
        invalidate();
        return true;
    }
}
