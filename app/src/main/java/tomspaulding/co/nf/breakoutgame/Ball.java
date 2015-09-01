package tomspaulding.co.nf.breakoutgame;

import android.graphics.RectF;

public class Ball {
    RectF rect;
    float xVelocity;
    float yVelocity;
    float ballWidth = 10;
    float ballHeight = 10;

    public final int FAST_X_VELOCITY = 800;
    public final int MEDIUM_X_VELOCITY = 500;
    public final int SLOW_X_VELOCITY = 200;

    public Ball(int screenX, int screenY){

        //start the ball travelling straight up at 100 pixels per second
        xVelocity = -200;
        yVelocity = -400;

        //place the ball in the center of the screen at the bottom
        //make it a ten by ten pixel square
        rect = new RectF();
    }

    public RectF getRect(){
        return rect;
    }

    public void update(long fps){
        rect.left = rect.left + (xVelocity / fps);
        rect.top = rect.top + (yVelocity /fps);
        rect.right = rect.left + ballWidth;
        rect.bottom = rect.top - ballHeight;
    }

    public void reverseYVelocity(){
        yVelocity = -yVelocity;
    }

    public void reverseXVelocity(){
        xVelocity = -xVelocity;
    }

    public void paddleBounch(RectF paddleRect, RectF ballRect){
        //Get paddle x positions
        float paddleLeft = paddleRect.left;
        float paddleRight = paddleRect.right;

        //Get the ball x positions
        float ballLeft = ballRect.left;
        float ballRight = ballRect.right;

        float leftDifference = ballLeft - paddleLeft;
        float rightDifference = paddleRight - ballRight;

        float paddleWidth = paddleRect.width();
        float ballWidth = ballRect.width();

        //if ball touched the right side of paddle
        if(rightDifference < leftDifference){
            //Get hitzone size
            float rightHitZone = (paddleWidth / 2) / 3;

            //Depending on where the ball struck the paddle, set the velocity
            if(rightDifference < rightHitZone/2){
                xVelocity = FAST_X_VELOCITY;
                System.out.println("Speedo Fast V Right");
            }
            else if(rightDifference < rightHitZone){
                xVelocity = MEDIUM_X_VELOCITY;
                System.out.println("Speedo Med V Right");

            }
            else{
                xVelocity = SLOW_X_VELOCITY;
                System.out.println("Speedo Slow V Right");

            }
        }
        //if ball touched left side of paddle
        else if (leftDifference < rightDifference){
            //Get hitzone size
            float rightHitZone = (paddleWidth / 2) / 3;

            //Depending on where the ball struck the paddle, set the velocity
            if(leftDifference < rightHitZone / 2){
                xVelocity = -FAST_X_VELOCITY;
                System.out.println("Speedo Fast V Left");

            }
            else if(leftDifference < rightHitZone){
                xVelocity = -MEDIUM_X_VELOCITY;
                System.out.println("Speedo Med V Left");

            }
            else{
                xVelocity = -SLOW_X_VELOCITY;
                System.out.println("Speedo Slow V Left");

            }
        }
    }

    public void clearObstacleY(float y){
        rect.bottom = y;
        rect.top = y - ballHeight;
    }

    public void clearObstacleX(float x){
        rect.left = x;
        rect.right = x + ballWidth;
    }

    public void reset(int x, int y){
        rect.left = x / 2;
        rect.top = y - 20;
        rect.right = x / 2 + ballWidth;
        rect.bottom = y - 20 - ballHeight;

        resetBallVelocity();
    }

    public void resetBallVelocity(){
        xVelocity = -200;
        yVelocity = -400;
    }
}
