package ca.mcgill.ecse211.lab1;

import java.util.LinkedList;

public class PController implements UltrasonicController {

  /* Constants */
  private static final int MOTOR_SPEED = 125;
  private static final int FILTER_OUT = 10;
  private static final double PROP_CONSTANT = 20.0;
  private static final int MAX_CORRECTION = 125;
  private static final int BANDCENTER = 30;
  private static final int BANDWIDTH = 2;
  private static final int CRITICAL_THRESHOLD = 15;
  private static final int CLOSE_VALUE = BANDCENTER - BANDWIDTH;

  private int distance;
  private int filterControl;
  private int leftSpeed;
  private int rightSpeed;

  // moving avg
  private static final int AVG_SIZE = 40;
  private int sampleCount;
  private int movingAvg;
  private LinkedList<Integer> avgBuffer;

  public PController() {
	this.distance = BANDCENTER;
    this.filterControl = 0;
    this.leftSpeed = 0;
    this.rightSpeed = 0;
    
    // moving avg
    this.sampleCount = 0;
    this.movingAvg = 0;
    this.avgBuffer = new LinkedList<Integer>();
    
    WallFollowingLab.leftMotor.setSpeed(leftSpeed); // Initalize motor rolling forward
    WallFollowingLab.rightMotor.setSpeed(rightSpeed);
    WallFollowingLab.leftMotor.forward();
    WallFollowingLab.rightMotor.forward();
  }

  @Override
  public void processUSData(int distance) {
	
	int correctedDistance = (int)(Math.sqrt(2.0)  * (double)distance);
	if (correctedDistance > 100) correctedDistance = 100;
	
	
	// Filtering
    if (correctedDistance >= 100 && filterControl < FILTER_OUT) {
      filterControl++;
    } else if (correctedDistance >= 100) {
      this.distance = 100;
    } else {
      filterControl = 0;
      this.distance = correctedDistance;
    }
    
    // Moving average
    if (this.distance < CLOSE_VALUE) {
      if (this.distance < CRITICAL_THRESHOLD)
        setMovingAverage(10);
      else
        setMovingAverage(10);
    }
    else
	  simpleMovingAvg();
  
    // Calculate error
    int error = BANDCENTER - this.distance;
    int absError = error > 0 ? error: -1*error;
    int leftSpeed=0, rightSpeed=0;
    
    // just right  
    if (absError < BANDWIDTH) {
    	leftSpeed = MOTOR_SPEED;
    	rightSpeed = MOTOR_SPEED;
    	setMotorsSpeed(leftSpeed, rightSpeed);
    } else {
    	// too close
    	int correction;
        if (error > 0) {
        	correction = calculateCorrection(absError);
        	leftSpeed = MOTOR_SPEED + correction;
        	rightSpeed = correction;
        	
        	WallFollowingLab.leftMotor.setSpeed(leftSpeed);
      	  	WallFollowingLab.rightMotor.setSpeed(rightSpeed);
      	  	WallFollowingLab.leftMotor.forward();
      	  	
      	  	if (this.distance < CRITICAL_THRESHOLD)  {
      	  		WallFollowingLab.rightMotor.setSpeed(rightSpeed);
      	  		WallFollowingLab.leftMotor.setSpeed(leftSpeed);
      	  		WallFollowingLab.rightMotor.backward();
      	  		WallFollowingLab.leftMotor.forward();
      	  	}
      	  	else
      	  		WallFollowingLab.rightMotor.forward();
        }
        // too far
        else {
        	int avgError = BANDCENTER - this.movingAvg;
        	int absAvgError = avgError > 0 ? avgError: -1*avgError;
        	
        	correction = calculateCorrection(absAvgError);
        	leftSpeed = MOTOR_SPEED - correction/5;
        	rightSpeed = MOTOR_SPEED + correction/2;	
        	setMotorsSpeed(leftSpeed, rightSpeed);
        }
    }
  }
  
 
  // Simple Moving Average
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
  
  private int calculateCorrection(int absError) {
	  int correction = (int) (PROP_CONSTANT * (double) absError );
	  correction = correction < MAX_CORRECTION ? correction: MAX_CORRECTION;
	  return correction;
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
