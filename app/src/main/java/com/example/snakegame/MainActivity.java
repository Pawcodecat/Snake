package com.example.snakegame;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MainActivity extends Activity {
    Canvas canvas;
    SnakeAnimalView snakeAnimalView;

    //the snake head sprite sheet
    Bitmap headAnimalBitmap;

    //the partition of bitmap to be drawn in current frame
    Rect rectToBeDrawn;

    //the dimension of single frame
    int frameWidth = 64;
    int frameHeight = 64;
    int numFrames = 6;
    int frameNumber;

    int screenWidth;
    int screenHeight;

    //starts
    long lastFrameTime;
    int fps;
    int hi;

    //to start the game from onTouchEvent
    Intent i;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //find out the width and height of the screen
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;

        headAnimalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.head_sprite_sheet);

        snakeAnimalView = new SnakeAnimalView(this);
        setContentView(snakeAnimalView);
        i = new Intent(this,GameActivity.class);
    }

    class SnakeAnimalView extends SurfaceView implements Runnable {
        Thread ourThread = null;
        SurfaceHolder ourHolder;
        volatile boolean playingSnake;
        Paint paint;

        public SnakeAnimalView(Context context) {
            super(context);
            ourHolder = getHolder();
            paint = new Paint();
            frameHeight = headAnimalBitmap.getHeight();
            frameWidth = headAnimalBitmap.getWidth();
        }

        @Override
        public void run() {
            while(playingSnake){
                update();
                Draw();
                controlFPS();
            }
        }

        private void controlFPS() {

            long timeThisFrame = (System.currentTimeMillis() - lastFrameTime);
            long timeToSleep = 500 - timeThisFrame;

            if(timeThisFrame > 0){
                fps = (int)(1000/timeThisFrame);
            }

            if(timeToSleep > 0){
                try{
                    ourThread.sleep(timeToSleep);
                }catch(InterruptedException e){

                }
                lastFrameTime = System.currentTimeMillis();
            }
        }

        private void Draw() {
            if(ourHolder.getSurface().isValid()){
                canvas = ourHolder.lockCanvas();
            }
        }

        private void update() {
        }
    }
}
