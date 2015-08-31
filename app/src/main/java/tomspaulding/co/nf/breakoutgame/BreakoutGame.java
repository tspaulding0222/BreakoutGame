package tomspaulding.co.nf.breakoutgame;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class BreakoutGame extends Activity {

    //the view of the game
    //also holds the logic of the game
    BreakoutView breakoutView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Initialize the game view and set it as the view
        breakoutView = new BreakoutView(this);
        setContentView(breakoutView);
    }

    @Override
    public void onResume(){
        super.onResume();

        //tell the breakoutview to resume also
        breakoutView.resume();
    }

    @Override
    public void onPause(){
        super.onPause();

        //tell the breakout view to pause also
        breakoutView.pause();
    }

    //Here is the implementation of breakoutview
    //Inner class, implements runnable
    class BreakoutView extends SurfaceView implements Runnable{

        //This the games thread
        Thread gameThread = null;

        //Surface holder that we will draw to
        SurfaceHolder ourHolder;

        //a boolean to track if the game is running
        volatile boolean playing;

        //a boolean to determine if the game is paused or not
        boolean paused = true;

        Canvas canvas;
        Paint paint;

        //variable  to track the games fps
        long fps;

        //used to calc the fps
        private long timeThisFrame;

        public BreakoutView(Context context){
            super(context);

            //Intialize ourHolder and paint objects
            ourHolder = getHolder();
            paint = new Paint();
        }

        @Override
        public void run(){
            while(playing){
                //capture the current time in ms at start of this frame
                long startFrameTime = System.currentTimeMillis();

                //update the frame
                if(!paused){
                    update();
                }

                draw();

                timeThisFrame = System.currentTimeMillis() - startFrameTime;
                if(timeThisFrame >= 1){
                    fps = 1000 / timeThisFrame;
                }
            }
        }

        //Everything that needs updated per frame
        public void update(){

        }

        //draw the newly updated scene
        public void draw(){
            //make sure the drawing surface is valid
            if(ourHolder.getSurface().isValid()){
                //lock the canvas to ready for draw
                canvas = ourHolder.lockCanvas();

                //Draw the background color
                canvas.drawColor(Color.argb(255, 26, 128, 182));

                //set the brush color
                paint.setColor(Color.argb(255, 255, 255, 255));

                //draw the paddle

                //draw the ball

                //draw the bricks

                //draw the hud

                //draw everything to the screen
                ourHolder.unlockCanvasAndPost(canvas);
            }
        }

        //if the game is paused, shutdown our thread
        public void pause(){
            playing = false;
            try{
                gameThread.join();
            }
            catch(InterruptedException e){
                Log.e("Error:", "Joining Thread, Fuck Balls, Titty Winkles");
            }
        }

        //if the game has been unpaused or is started/restarted
        public void resume(){
            playing = true;
            gameThread = new Thread(this);
            gameThread.start();
        }

        //the surfaceviews onTouchListener
        @Override
        public boolean onTouchEvent(MotionEvent motionEvent){
            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK){
                //if the player touches the screen
                case MotionEvent.ACTION_DOWN:

                    break;

                //player has lifted their finger up from the screen
                case MotionEvent.ACTION_UP:

                    break;
            }

            return true;
        }
    }//end of the breakoutview inner class
}
