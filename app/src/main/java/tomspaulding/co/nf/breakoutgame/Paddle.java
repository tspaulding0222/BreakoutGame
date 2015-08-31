package tomspaulding.co.nf.breakoutgame;

import android.graphics.RectF;

public class Paddle {

    //The containing rect for the paddle
    private RectF rect;

    //how long and tall paddle will be
    private float length;
    private float height;

    //x is the far left of the rectangle which forms our paddle
    private float x;

    //y is the top coordinate
    private float y;

    //this will hold the pixel per second speed that the paddle will move
    private float paddleSpeed;

    //which ways the paddle can move
    public final int STOPPED = 0;
    public final int LEFT = 1;
    public final int RIGHT = 2;

    //is the paddle moving and in what direction
    private int paddleMoving = STOPPED;

    public Paddle(int screenX, int screenY){
        //130 pixels wide and 20 pixels high
        length = 130;
        height = 20;

        //start the paddle in roughly the middle of the screen
        x = screenX / 2;
        y = screenY - 20;

        rect = new RectF(x, y, x + length, y + height);

        //how fast is the paddle
        paddleSpeed = 350;
    }

    //get the rectangle that defines the paddle
    public RectF getRect(){
        return rect;
    }

    //change the direction that the paddle is moving
    public void setMovementState(int state){
        paddleMoving = state;
    }

    //this update method will be called from update in BreakoutView
    //it determines if the paddle needs to move and changes the coordinates
    public void update(long fps){
        if(paddleMoving == LEFT){
            x = x - paddleSpeed / fps;
        }

        if(paddleMoving == RIGHT){
            x = x + paddleSpeed / fps;
        }

        rect.left = x;
        rect.right = x + length;
    }
}
