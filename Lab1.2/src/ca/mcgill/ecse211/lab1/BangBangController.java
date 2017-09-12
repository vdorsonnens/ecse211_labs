package ca.mcgill.ecse211.lab1;

import lejos.hardware.motor.*;

public class BangBangController implements UltrasonicController {
  private static final int FILTER_OUT = 20;
	
	
  private final int bandCenter;
  private final int bandwidth;
  private final int motorLow;
  private final int motorHigh;
  private int distance;
  private int filterControl;
  
  // MAs
  //private static final int SMA_N = 10; // avg over last n;
    
  //private int movingAverage;
  //private int dataCount;
  //private ArrayList<Integer> dataBuff;

  public BangBangController(int bandCenter, int bandwidth, int motorLow, int motorHigh) {
    // Default Constructor
    this.bandCenter = bandCenter;
    this.bandwidth = bandwidth;
    this.motorLow = motorLow;
    this.motorHigh = motorHigh;
    this.filterControl = 0;
    WallFollowingLab.leftMotor.setSpeed(motorHigh); // Start robot moving forward
    WallFollowingLab.rightMotor.setSpeed(motorHigh);
    WallFollowingLab.leftMotor.backward();
    WallFollowingLab.rightMotor.backward();
  
    // Values for moving average
    //this.movingAverage = 0;
    //this.dataCount = 0;
    //this.dataBuff = new ArrayList<Integer>(); //change this for a SMA_N sized queue
  }

  @Override
  public void processUSData(int distance) {
	  if (distance >= 255 && filterControl < FILTER_OUT) {
	      // bad value, do not set the distance var, however do increment the
	      // filter value
	      filterControl++;
	    } else if (distance >= 255) {
	      // We have repeated large values, so there must actually be nothing
	      // there: leave the distance alone
	      this.distance = distance;
	    } else {
	      // distance went below 255: reset filter and leave
	      // distance alone.
	      filterControl = 0;
	      this.distance = distance;
	    }
	  
    this.distance = (int)(Math.sqrt(2.0) * (double)distance);
    int error = this.bandCenter - distance;
    
    // SMA
    //error = SMA(error);
    
    //TODO WMA
    //TODO EWMA
    
    int absError = error > 0 ? error: -1*error;
    
    // just right
    if (absError < this.bandwidth)
    	setMotorsSpeed(this.motorHigh, this.motorHigh);
    // too close
    else if (error > 0) 
    	setMotorsSpeed(this.motorHigh, this.motorLow);
    // too far
    else 
    	setMotorsSpeed(this.motorLow, this.motorHigh);
  }
  
  private void setMotorsSpeed(int leftSpeed, int rightSpeed) {
	  WallFollowingLab.leftMotor.setSpeed(leftSpeed);
	  WallFollowingLab.rightMotor.setSpeed(rightSpeed);
	  WallFollowingLab.leftMotor.backward();
	  WallFollowingLab.rightMotor.backward();
  }

  /*private int SMA(int currentValue) {
    int nNew;
    int nFirst;
    int firstVal = 0;

    if (this.dataCount < SMA_N-1) {
      this.dataCount++;
      nNew = this.dataCount;
      nFirst = 1
    } else {
      firstVal = this.dataBuff.get(0);
      this.dataBuff.
      nNew = SMA_;
      nFirst = SMA_N;
    }
    this.movingAverage = this.movingAverage + currentValue/this.dataCount - first/(this.dataCount-1);
  }*/

  @Override
  public int readUSDistance() {
    return this.distance;
  }
}
