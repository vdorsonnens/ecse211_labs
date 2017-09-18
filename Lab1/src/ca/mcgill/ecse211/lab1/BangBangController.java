package ca.mcgill.ecse211.lab1;

import java.util.LinkedList;

public class BangBangController implements UltrasonicController {
  private static final int SPEED = 125;
  private static final int SPEEDDELTA = 70;
  private static final int BANDCENTER = 25;
  private static final int BANDWIDTH = 1;
  private static final int FILTER_OUT = 0;
  private static final int CRITICAL_THRESHOLD = 10;
  private static final int CLOSE_VALUE = BANDCENTER - BANDWIDTH;
  
  private static final int AVG_SIZE = 20;
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
      setMovingAverage(150);
    } else {
      filterControl = 0;
      this.distance = correctedDistance;
    }
    
    // Moving average
    if (this.distance < CLOSE_VALUE) {
        if (this.distance < CRITICAL_THRESHOLD)
          setMovingAverage(this.distance);
        else
          setMovingAverage(10);
      }
      else
  	    simpleMovingAvg();

    int error = BANDCENTER - this.movingAvg;   
    int absError = error > 0 ? error: -1*error;
    
    // just right
    if (absError < BANDWIDTH)
      setMotorsSpeed(SPEED +  SPEEDDELTA, SPEED + SPEEDDELTA);
    // too close
    else if (error > 0) {
    	if (this.movingAvg  < 15)
    		WallFollowingLab.leftMotor.setSpeed(SPEED + 120);
    	else
    		WallFollowingLab.leftMotor.setSpeed(SPEED +  SPEEDDELTA);
    	
    	WallFollowingLab.rightMotor.setSpeed(90);
    	WallFollowingLab.leftMotor.forward();
    	WallFollowingLab.rightMotor.backward();
    }
    // too far
    else {
    	if (this.movingAvg  > 100)
    	  setMotorsSpeed(SPEED, SPEED+70);
    	else 
    		setMotorsSpeed(SPEED-20, SPEED + 70);
    }
    
  }


  private void setMotorsSpeed(int leftSpeed, int rightSpeed) {
    WallFollowingLab.leftMotor.setSpeed(leftSpeed);
	WallFollowingLab.rightMotor.setSpeed(rightSpeed);
	WallFollowingLab.leftMotor.forward();
	WallFollowingLab.rightMotor.forward();
  }
  
//Simple Moving Average
 private void simpleMovingAvg() {
	  if (this.sampleCount < AVG_SIZE-1) {
		  this.sampleCount++;
		  this.avgBuffer.addLast(this.distance);
		  this.movingAvg = this.distance;
	  } else if (this.sampleCount == AVG_SIZE-1) {
		  this.sampleCount++;
		  this.avgBuffer.addLast(this.distance);
		  int sum = 0;
		  for (int n: avgBuffer)
			  sum += n;
		  this.movingAvg = sum/AVG_SIZE;
	  } else {
		  int head = this.avgBuffer.removeFirst();
		  this.avgBuffer.addLast(this.distance);
		  
		  this.movingAvg = this.movingAvg + this.distance/AVG_SIZE - head/AVG_SIZE;
	  }
 }
 
 private void setMovingAverage(int value) {
	  this.movingAvg = value;
	  this.avgBuffer = new LinkedList<Integer>();
	  for (int i=0; i<AVG_SIZE; i++) {
		  this.avgBuffer.addLast(value);
	  }
 }

  @Override
  public int readUSDistance() {
    return this.distance;
  }
}