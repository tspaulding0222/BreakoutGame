package tomspaulding.co.nf.breakoutgame;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

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

        //the players paddle
        Paddle paddle;

        //the ball
        Ball ball;

        //up to 200 bricks
        Brick[] bricks = new Brick[200];
        int numBricks = 0;

        //for sound fx
        SoundPool soundPool;
        int beep1ID = -1;
        int beep2ID = -1;
        int beep3ID = -1;
        int loseLifeID = -1;
        int explodeID = -1;

        //the score
        int score = 0;

        //lives
        int lives = 3;

        //Current level the player is at
        int level = 1;

        //The max level you want to be played
        int maxLevel = 1;

        //tracks if the current level has ended
        boolean levelEnd = false;

        //a boolean to track if the game is running
        volatile boolean playing;

        //a boolean to determine if the game is paused or not
        boolean paused = true;

        //boolean to determine if the user has beat the game
        boolean winner = false;

        Canvas canvas;
        Paint paint;

        //variable  to track the games fps
        long fps;

        //used to calc the fps
        private long timeThisFrame;

        //the size of the screen
        int screenX;
        int screenY;

        public BreakoutView(Context context){
            super(context);

            //Intialize ourHolder and paint objects
            ourHolder = getHolder();
            paint = new Paint();

            //Get a display object
            Display display = getWindowManager().getDefaultDisplay();
            //load the res into a point object
            Point size = new Point();
            display.getSize(size);
            //intialize the screen size vars
            screenX = size.x;
            screenY = size.y;

            //create the users paddle
            paddle = new Paddle(screenX, screenY);

            //create the ball
            ball = new Ball(screenX, screenY);

            //load the sounds
            soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
            try{
                //Create objects fo teh 2 required classes
                AssetManager assetManager = context.getAssets();
                AssetFileDescriptor descriptor;

                //load our fx in memory
                descriptor = assetManager.openFd("beep1.ogg");
                beep1ID = soundPool.load(descriptor, 0);

                descriptor = assetManager.openFd("beep2.ogg");
                beep2ID = soundPool.load(descriptor, 0);

                descriptor = assetManager.openFd("beep3.ogg");
                beep3ID = soundPool.load(descriptor, 0);

                descriptor = assetManager.openFd("loseLife.ogg");
                loseLifeID = soundPool.load(descriptor, 0);

                descriptor = assetManager.openFd("explode.ogg");
                explodeID = soundPool.load(descriptor, 0);
            }
            catch(IOException e){
                Log.e("error", "failed to load sound files");
            }

            createBricksAndRestart();
        }

        public void createBricksAndRestart(){
            //put the ball back to start
            ball.reset(screenX, screenY);

            //undo the levelEnd
            levelEnd = false;

            //Default Brick Size
            int brickWidth = screenX /8;
            int brickHeight = screenY /10;

            //number of bricks left
            numBricks = 0;

            //build a wall of bricks
            buildBrickWall(brickWidth, brickHeight);

            //reset scores and lives
            score = 0;
            lives = 3;
        }

        public void buildBrickWall(int brickWidth, int brickHeight){
            //Various brick formations for the levels
            if(level == 1){
                //8 columns, 3 rows
                for(int column = 0; column < 1; column++){
                    for(int row = 0; row < 1; row++){
                        bricks[numBricks] = new Brick(row, column, brickWidth, brickHeight);
                        numBricks++;
                    }
                }
            }
            else if(level == 2){
                //8 columns, 4 rows
                for(int column = 0; column < 1; column++){
                    for(int row = 0; row < 1; row++){
                        bricks[numBricks] = new Brick(row, column, brickWidth, brickHeight);
                        numBricks++;
                    }
                }
            }
            else if(level == 3){
                //Make the bricks half the size
                brickWidth = brickWidth / 2;
                brickHeight = brickHeight / 2;

                //16 columns, 4 rows
                for(int column = 0; column < 16; column++){
                    for(int row = 0; row < 4; row++){
                        bricks[numBricks] = new Brick(row, column, brickWidth, brickHeight);
                        numBricks++;
                    }
                }
            }
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
            //move the users paddle if required
            paddle.update(fps);

            //check the ball for colliding
            for(int i = 0; i < numBricks; i++){
                if(bricks[i].getVisibility()){
                    if(RectF.intersects(bricks[i].getRect(), ball.getRect())){
                        bricks[i].setInvisible();
                        ball.reverseYVelocity();
                        score = score + 10;
                        soundPool.play(explodeID, 1, 1, 0, 0, 1);
                    }
                }
            }

            //check for ball colliding with paddle
            if(RectF.intersects(paddle.getRect(), ball.getRect())){
                ball.reverseYVelocity();
                ball.clearObstacleY(paddle.getRect().top - 2);

                //determine the x direction
                ball.paddleBounch(paddle.getRect(), ball.getRect());

                soundPool.play(beep1ID, 1, 1, 0, 0, 1);
            }

            //Reset the ball when it hits bottom of screen and deduct a life
            if(ball.getRect().bottom > screenY){
                paused = true;
                ball.reset(screenX, screenY);
                paddle.centerPaddle(screenX);

                //lose a life
                lives--;
                soundPool.play(loseLifeID, 1, 1, 0, 0, 1);

                if(lives == 0){
                    paused = true;
                    if(level >= 2){
                        level--;
                    }
                }
            }

            //bounce the ball back when it hits top of screen
            if(ball.getRect().top < 0){
                ball.reverseYVelocity();
                ball.clearObstacleY(12);
                soundPool.play(beep2ID, 1, 1, 0, 0, 1);
            }

            //bounce the ball back when it hits left side of screen
            if(ball.getRect().left < 0){
                ball.reverseXVelocity();
                ball.clearObstacleX(2);
                soundPool.play(beep3ID, 1, 1, 0, 0, 1);
            }

            //bound the ball back when it hits right side of screen
            if(ball.getRect().right > screenX - 10){
                ball.reverseXVelocity();
                ball.clearObstacleX(screenX - 22);
                soundPool.play(beep3ID, 1, 1, 0, 0, 1);
            }

            //pause if cleared screen
            if(score == numBricks * 10){
                //if max level reached then end the game
                if(level >= maxLevel){
                    winner = true;
                    level = 1;
                }
                else{
                    level++;
                }

                paused = true;
            }

            //update the ball location
            ball.update(fps);
        }

        //draw the newly updated scene
        public void draw(){
            //make sure the drawing surface is valid
            if(ourHolder.getSurface().isValid()){
                //lock the canvas to ready for draw
                canvas = ourHolder.lockCanvas();

                //Draw the background color
                canvas.drawColor(Color.argb(255, 57, 57, 57));

                //set the brush color
                paint.setColor(Color.argb(255, 255, 255, 255));

                //draw the paddle
                canvas.drawRect(paddle.getRect(), paint);

                //draw the ball
                canvas.drawCircle(ball.getRect().centerX(), ball.getRect().centerY(), ball.getRect().width(), paint);

                //change the color of the brush
                paint.setColor(Color.argb(255, 249, 129, 0));

                //draw the bricks
                for(int i = 0; i < numBricks; i++){
                    if(bricks[i].getVisibility()){
                        canvas.drawRect(bricks[i].getRect(), paint);
                    }
                }

                //change the brush color
                paint.setColor(Color.argb(255, 255, 255, 255));

                //draw the score
                paint.setTextSize(40);
                canvas.drawText("Score: " + score + " Lives: " + lives, 10, 50, paint);

                //draw the current level
                String levelString = "Level: " + level;
                canvas.drawText(levelString, screenX - paint.measureText(levelString, 0, levelString.length()), 50, paint);

                //has the player cleared the screen or lost?
                if(score == numBricks * 10 || lives <= 0){
                    //run the level over logic if the game is not already paused
                    levelEnded();
                }

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
                    paused = false;

                    //if game is over then reset
                    if(levelEnd){
                        //Run the logic for the level being over
                        createBricksAndRestart();
                    }

                    //Paddle movement for press of left and right on screen
                    if(motionEvent.getX() > screenX / 2){
                        //move the paddle
                        paddle.setMovementState(paddle.RIGHT);
                    }
                    else{
                        paddle.setMovementState(paddle.LEFT);
                    }

                    break;

                //player has lifted their finger up from the screen
                case MotionEvent.ACTION_UP:
                    paddle.setMovementState(paddle.STOPPED);

                    break;
            }

            return true;
        }

        public void levelEnded(){
            //set the levelend var
            levelEnd = true;

            //Check to see if the complete game is won
            if(winner){
                gameWinner();
            }
            else{
                //has the player cleared the screen?
                if(score == numBricks * 10){
                    paint.setTextSize(90);
                    canvas.drawText("Congratulations! On to level " + level, 10, screenY/2, paint);
                }

                //has player lost?
                if(lives <= 0){
                    paint.setTextSize(90);
                    canvas.drawText("YOU HAVE LOST, Touch to Restart", 10, screenY/2, paint);
                }
            }
        }

        public void gameWinner(){
            paint.setTextSize(90);
            canvas.drawText("Winner Winner Winner!!", 10, screenY/2, paint);
        }
    }//end of the breakoutview inner class
}
