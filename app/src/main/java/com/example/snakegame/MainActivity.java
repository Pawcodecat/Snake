package com.example.snakegame;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
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
                canvas.drawColor(Color.BLACK); //background
                paint.setColor(Color.argb(255,255,255,255));
                paint.setTextSize(150);
                canvas.drawText("Snake",10,150,paint);
                paint.setTextSize(25);

                canvas.drawText("Hi Score:  "+ hi,10,screenHeight-50,paint);

                //Draw the snake head
                //make this Rect Whatever size and location
                Rect destRect = new Rect(screenWidth/2-100,screenHeight/2-100,screenWidth/2+100, screenHeight/2+100);
                canvas.drawBitmap(headAnimalBitmap,rectToBeDrawn,destRect,paint);
                ourHolder.unlockCanvasAndPost(canvas);
            }
        }

        private void update() {

            //which frame should be drawn
            rectToBeDrawn = new Rect((frameNumber * frameWidth) -1, 0, (frameNumber * frameWidth + frameWidth)-1,frameHeight );

            //now the next frame
            frameNumber++;

            // don't try and draw frames that don't exist
            if(frameNumber == numFrames){
                frameNumber = 0; //back to the first
            }
        }

        public void pause(){
            playingSnake = false;
            try {
                ourThread.join();
            }catch(InterruptedException e){

            }
        }

        public void resumme(){
            playingSnake = true;
            ourThread = new Thread();
            ourThread.start();
        }

        //snakeView class and onTouchEvent method
        //which simply starts the game when the screen
        //is touched anywhere, we enter the game following code


        @Override
        public boolean onTouchEvent(MotionEvent event) {
            startActivity(i);
            return true;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        while(true){
            snakeAnimalView.pause();
            break;
        }
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        snakeAnimalView.resumme();
    }

    @Override
    protected void onPause() {
        super.onPause();
        snakeAnimalView.pause();
    }

    //for back press
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode == KeyEvent.KEYCODE_BACK){
            snakeAnimalView.pause();
            finish();
            return true;
        }
        return false;
    }
}












