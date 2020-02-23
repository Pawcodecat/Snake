package com.example.snakegame;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;

import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


import java.io.IOException;
import java.util.Random;

public class GameActivity extends Activity {
    Canvas canvas;
    SnakeView snakeView;

    Bitmap headBitmap;
    Bitmap bodyBitmap;
    Bitmap tailBitmap;
    Bitmap appleBitmap;

    //sound
    //initialize sounds variables

    private SoundPool soundPool;

    int sample1 = -1;
    int sample2 = -1;
    int sample3 = -1;
    int sample4 = -1;

    //for snake movement
    int directionOfTravel = 0;

    int screenWidth;
    int screenHeight;
    int topGap;

    //stats
    long lastFrameTime;
    int fps;
    int score;
    int hi;

    //Game Objects
    int []snakeX;
    int []snakeY;
    int snakeLength;
    int appleX;
    int appleY;
    //the size in pixel of a place on the game board
    int blockSize;
    int numBlocksWidth;
    int numBlocksHigh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadSound();
        configureDisplay();
        snakeView = new SnakeView(this);
        setContentView(snakeView);
    }

    private void configureDisplay() {

        //find out the width and height of the screen
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;

        topGap = screenHeight/14;

        //Determine the size of each block/place on the game board
        blockSize = screenWidth/40;

        //Determine how many game block will fit into the height and width
        //leave one block for the score at the top
        numBlocksWidth = 40;
        numBlocksHigh = ((screenHeight - topGap))/blockSize;

        //Load and scale bitMaps
        headBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.head);
        bodyBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.body);
        tailBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.tail);
        appleBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.apple);

        //scale the bitmaps to match the block size
        headBitmap = Bitmap.createScaledBitmap(headBitmap,blockSize, blockSize, false);
        bodyBitmap = Bitmap.createScaledBitmap(bodyBitmap,blockSize, blockSize, false);
        tailBitmap = Bitmap.createScaledBitmap(tailBitmap,blockSize, blockSize, false);
        appleBitmap = Bitmap.createScaledBitmap(appleBitmap,blockSize, blockSize, false);

    }

    private void loadSound() {
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC,0);
        try{
            //create object of the 2 required classes
            AssetManager assetManager = getAssets();
            AssetFileDescriptor descriptor;

            //create our three fx in memory read for playing
            descriptor = assetManager.openFd("sample1.ogg");
            sample1 = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("sample2.ogg");
            sample2 = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("sample3.ogg");
            sample3 = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("sample4.ogg");
            sample4 = soundPool.load(descriptor, 0);
        }catch(IOException e){
            //print an error message to the console
            Log.e("error","failed to load sound files");
        }
    }


    //inner class

    class SnakeView extends SurfaceView implements Runnable {


        Thread ourThread;
        SurfaceHolder ourHolder;
        volatile boolean playingSnake;
        Paint paint;

        public SnakeView(Context context) {
            super(context);
            ourHolder = getHolder();
            paint = new Paint();

            //get a snkae this long
            snakeX = new int[200];
            snakeY = new int[200];

            //our starting snake
            getSnake();

            //get an appple to much
            getApple();
        }

        private void getApple() {

            Random random = new Random();
            appleX = random.nextInt(numBlocksWidth - 1) + 1;
            appleY = random.nextInt(numBlocksHigh - 1) + 1;
        }

        private void getSnake() {
            snakeLength = 3;

            //start snake head in the middle of the screen
            snakeX[0] = numBlocksWidth / 2;
            snakeY[0] = numBlocksWidth / 2;

            //then the body
            snakeX[1] = snakeX[0] - 1;
            snakeY[1] = snakeY[0];

            //and the tail
            snakeX[1] = snakeX[1] - 1;
            snakeY[1] = snakeY[0];
        }

        @Override
        public void run() {
            while (playingSnake) {
                updateGame();
                drawGame();
                controlFPS();
            }
        }

        private void controlFPS() {

            long timeThisFrame = (System.currentTimeMillis() - lastFrameTime);
            long timeToSleep = 100 - timeThisFrame;

            if (timeThisFrame > 0) {
                fps = (int) (1000 / timeThisFrame);
            }

            if (timeToSleep > 0) {
                try {
                    ourThread.sleep(timeToSleep);
                } catch (InterruptedException e) {
                    //print an error message to the console
                    Log.e("error", "failed to load sound files");
                }
            }
            lastFrameTime = System.currentTimeMillis();

        }

        private void drawGame() {
            if (ourHolder.getSurface().isValid()) {
                canvas = ourHolder.lockCanvas();

                canvas.drawColor((Color.BLACK)); //the background
                paint.setColor(Color.argb(255, 255, 255, 255));
                paint.setTextSize(topGap / 2);
                canvas.drawText("Score: " + score + " Hi: " + hi, 10, topGap - 5, paint);

                //draw a border - 4 lines, top right, bottom, left
                paint.setStrokeWidth(3); //4 pixel border
                canvas.drawLine(1, topGap, screenWidth - 1, topGap, paint);
                canvas.drawLine(screenWidth - 1, topGap, screenWidth - 1, topGap + (numBlocksHigh * blockSize), paint);
                canvas.drawLine(screenWidth - 1, topGap + (numBlocksHigh * blockSize), 1, topGap + (numBlocksHigh * blockSize), paint);
                canvas.drawLine(1, topGap, 1, topGap + (numBlocksHigh * blockSize), paint);

                //Draw the snake
                canvas.drawBitmap(headBitmap, snakeX[0] * blockSize, (snakeY[0] * blockSize) + topGap, paint);

                //draw the body
                for (int i = 1; i < snakeLength - 1; i++) {
                    canvas.drawBitmap(bodyBitmap, snakeX[i] * blockSize, (snakeY[i] * blockSize) + topGap, paint);
                }

                //draw the tail
                canvas.drawBitmap(appleBitmap, appleX * blockSize, (appleY * blockSize) + topGap, paint);

                ourHolder.unlockCanvasAndPost(canvas);
            }

        }

        private void updateGame() {
            //did the player get the apple
            if (snakeX[0] == appleX && snakeY[0] == appleY) {
                //grow the snake
                snakeLength++;
                //replace the apple
                getApple();
                //add to the score
                score = score + snakeLength;
                soundPool.play(sample1, 1, 1, 0, 0, 1);
            }

            //move the body - starting at the back
            for (int i = snakeLength; i > 0; i--) {
                snakeX[i] = snakeX[i - 1];
                snakeY[i] = snakeY[i - 1];

            }

            //move the head in appropriate direction
            switch (directionOfTravel) {
                case 0: //up
                    snakeY[0]--;
                    break;

                case 1: //right
                    snakeX[0]++;

                case 2: //down
                    snakeY[0]++;

                case 3: //left
                    snakeX[0]--;
            }
            //have we had an accident
            boolean dead = false;
            //with wall
            if (snakeX[0] == -1) dead = true;
            if (snakeX[0] >= numBlocksWidth) dead = true;
            if (snakeY[0] == -1) dead = true;
            if (snakeY[0] == numBlocksHigh) dead = true;

            //or even ourselves?
            for (int i = snakeLength - 1; i > 0; i--) {
                if ((i > 4) && (snakeX[0] == snakeX[i]) && (snakeY[0] == snakeY[i]))
                    dead = true;
            }

            if (dead) {
                //start again
                soundPool.play(sample4, 1, 1, 0, 0, 1);
                score = 0;
                getSnake();
            }


        }

        public void pause() {
            playingSnake = false;
            try {
                ourThread.join();
            } catch (InterruptedException e) {

            }
        }

        public void resume() {
            playingSnake = true;
            ourThread = new Thread(this);
            ourThread.start();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            switch(event.getAction() & MotionEvent.ACTION_MASK){
                case MotionEvent.ACTION_UP:
                    if(event.getX() >= screenWidth/2){
                        //turn right
                        directionOfTravel++;

                        if(directionOfTravel == 4){
                            //no such direction
                            //loop back to 0 (up)
                            directionOfTravel = 0;
                        }
                        else{
                            //turn left

                            directionOfTravel--;
                            if(directionOfTravel == -1){
                                // no such direction
                                //loop back to 3
                                directionOfTravel = 3;
                            }
                        }
                    }
            }
            return true;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        while(true){
            snakeView.pause();
            break;
        }
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        snakeView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        snakeView.pause();
    }


    public boolean onKeyDown(int keyCode, KeyEvent eveny){
        if(keyCode == KeyEvent.KEYCODE_BACK){
            snakeView.pause();
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            finish();
            return true;
        }
        return false;
    }
}
