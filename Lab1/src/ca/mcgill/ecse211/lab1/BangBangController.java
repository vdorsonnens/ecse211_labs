package ca.mcgill.ecse211.lab1;

import java.util.LinkedList;

public class BangBangController implements UltrasonicController {
  private static final int SPEED = 125;
  private static final int SPEEDDELTA_RIGHT = 70;
  private static final int SPEEDDELTA_LEFT = 90;
  private static final int BANDCENTER = 35;
  private static final int BANDWIDTH = 3;
  private static final int FILTER_OUT = 5;
  private static final int CRITICAL_THRESHOLD = 10;
  private static final int CLOSE_VALUE = BANDCENTER - BANDWIDTH;
  
  private static final int AVG_SIZE = 40;
  private int sampleCount;
  private int movingAvg;
  private LinkedList<Integer> avgBuffer;

  private int distance;
  private int filterControl;

  public BangBangController() {
    // Default Constructor
    WallFollowingLab.leftMotor.setSpeed(SPEED); // Start robot moving forward
    WallFollowingLab.rightMotor.setSpeed(SPEED);
    WallFollowingLab.leftMotor.forward();
    WallFollowingLab.rightMotor.forward();
    
    this.sampleCount = 0;
    this.movingAvg = 0;
    this.avgBuffer = new LinkedList<Integer>();
  }

  @Override
  public void processUSData(int distance) {
    int correctedDistance = (int)(Math.sqrt(2.0) * (double)distance);
    
    // Filtering
    if (correctedDistance >= 100 && filterControl < FILTER_OUT) {
      filterControl++;
    } else if (correctedDistance >= 100) {
      this.distance = 100;
    } else {
      filterControl = 0;
      this.distance = correctedDistance;
    }

    int error = BANDCENTER - this.distance   ;
    int absError = error > 0 ? error: -1*error;
    
    // just right
    if (absError < BANDWIDTH)
      setMotorsSpeed(SPEED +  SPEEDDELTA_LEFT, SPEED + SPEEDDELTA_LEFT);
    // too close
    else if (error > 0) {
    	if (this.distance  < 15) {
    		WallFollowingLab.leftMotor.setSpeed(50);
    		WallFollowingLab.rightMotor.setSpeed(125);
    		WallFollowingLab.leftMotor.backward();
    		WallFollowingLab.rightMotor.backward();
    		try {
    	        Thread.sleep(500);
    	      } catch (Exception e) {
    	      } // Poor man's timed sampling
    			WallFollowingLab.leftMotor.setSpeed(SPEED + SPEEDDELTA_RIGHT*2);
    	}
    	else
    		WallFollowingLab.leftMotor.setSpeed(SPEED +  SPEEDDELTA_RIGHT);
    	
    	WallFollowingLab.rightMotor.setSpeed(SPEEDDELTA_RIGHT);
    	WallFollowingLab.leftMotor.forward();
    	WallFollowingLab.rightMotor.backward();
    }
    // too far
    else {
    	  setMotorsSpeed(SPEED, SPEED+SPEEDDELTA_LEFT);
    }
    
  }

  private void setMotorsSpeed(int leftSpeed, int rightSpeed) {
    WallFollowingLab.leftMotor.setSpeed(leftSpeed);
	WallFollowingLab.rightMotor.setSpeed(rightSpeed);
	WallFollowingLab.leftMotor.forward();
	WallFollowingLab.rightMotor.forward();
  }
  

  @Override
  public int readUSDistance() {
    return this.distance;
  }
}
