package ca.mcgill.ecse211.lab1;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import java.util.LinkedList;

public class PController implements UltrasonicController {

  /* Constants */
  private static final int MOTOR_SPEED = 125;
  private static final int FILTER_OUT = 10;
  private static final double PROP_CONSTANT = 20.0;
  private static final int MAX_CORRECTION = 125;
  private static final int BANDCENTER = 20;
  private static final int BANDWIDTH = 3;
  
  private int distance;
  private int filterControl;
  private int leftSpeed;
  private int rightSpeed;

  
  // moving avg test
  private static final int AVG_SIZE = 100;
  private static final double ALPHA = 0.01;
  private int sampleCount;
  private int movingAvg;
  private LinkedList<Integer> avgBuffer;
  private int sizeSum;
  private int lastTotal;
  private int lastNumerator;

  public PController() {
	this.distance = BANDCENTER;
    this.filterControl = 0;
    this.leftSpeed = 0;
    this.rightSpeed = 0;
    
    // moving avg test
    this.sampleCount = 0;
    this.movingAvg = 0;
    this.avgBuffer = new LinkedList<Integer>();
    this.sizeSum = (AVG_SIZE * (AVG_SIZE+1)) / 2;
    this.lastTotal = 0;
    this.lastNumerator = 0;
    
    WallFollowingLab.leftMotor.setSpeed(leftSpeed); // Initalize motor rolling forward
    WallFollowingLab.rightMotor.setSpeed(rightSpeed);
    WallFollowingLab.leftMotor.forward();
    WallFollowingLab.rightMotor.forward();
  }

  @Override
  public void processUSData(int distance) {
	
	int correctedDistance = (int)(Math.sqrt(2.0)  * (double)distance);
	if (correctedDistance > 250) correctedDistance = 250;
	
	
	// Filtering
    if (correctedDistance >= 100 && filterControl < FILTER_OUT) {
      filterControl++;
    } else if (correctedDistance >= 100) {
      this.distance = 100;
    } else {
      filterControl = 0;
      this.distance = correctedDistance;
    }
	
	// moving avg test
	updateMovingAvg();
    
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
      	  	
      	  	if (this.distance < 15) 
      	  		WallFollowingLab.rightMotor.backward();
      	  	else
      	  		WallFollowingLab.rightMotor.forward();
        }
        // too far
        else {
        	int avgError = BANDCENTER - this.movingAvg;
        	int absAvgError = avgError > 0 ? avgError: -1*avgError;
        	
        	correction = calculateCorrection(absAvgError);
        	leftSpeed = MOTOR_SPEED - correction/4;
        	rightSpeed = MOTOR_SPEED + correction/2;
        	setMotorsSpeed(leftSpeed, rightSpeed);
        }
    }
    //setMotorsSpeed(0,0);
  }
  
  private void updateMovingAvg() {
	  simpleMovingAvg();
	  //expWeightedMovingAvg();	  
  }
  
  // Exponentially Weighted Moving Average
  private void expWeightedMovingAvg() {
	  if (this.sampleCount == 0) {
		  this.sampleCount++;
		  this.movingAvg = this.distance;
	  } else {
		  this.movingAvg = (int) (ALPHA * (double)this.distance + (1.0-ALPHA) * (double)this.movingAvg);
	  }
  }
  
  // Weighted Moving Average
  private void weightedMovingAvg() {
	  if (this.sampleCount < AVG_SIZE - 1) {
		  this.sampleCount++;
		  this.avgBuffer.addLast(this.distance);
		  this.movingAvg = this.distance;
	  } else if (this.sampleCount == AVG_SIZE - 1) {
		  this.sampleCount++;
		  this.avgBuffer.addLast(this.distance);
		  int numerator = 0;
		  int total = 0;
		  for (int i=0; i<AVG_SIZE; i++) { 
			  numerator += (i+1) * this.avgBuffer.get(i);
			  total += this.avgBuffer.get(i);
		  }
		  this.lastTotal = total;
		  this.lastNumerator = numerator;
		  this.movingAvg = numerator / this.sizeSum;
	  } else {
		  int head = this.avgBuffer.removeFirst();
		  this.avgBuffer.addLast(this.distance);
		  int total = this.lastTotal + this.distance - head;
		  int numerator = this.lastNumerator + AVG_SIZE*this.distance - this.lastTotal;
		  
		  this.lastTotal = total;
		  this.lastNumerator = numerator;
		  this.movingAvg = numerator / this.sizeSum;
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
  
  private int calculateCorrection(int absError) {
	  // y = ax, maybe try something smoother like ^2
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
	System.out.printf("DISTANCE: %d\tAVG: %d\n", this.distance, this.movingAvg);
	//return this.rate;
    return this.distance;
  }

}
