package ca.mcgill.ecse211.lab1;

import lejos.hardware.motor.*;

public class BangBangController implements UltrasonicController {
  private static final int SPEEDDELTA = 50;
  
  private final int bandCenter;
  private final int bandwidth;
  private final int motorLow;
  private final int motorHigh;
  private int distance;
  
  public BangBangController(int bandCenter, int bandwidth, int motorLow, int motorHigh) {
    // Default Constructor
    this.bandCenter = bandCenter;
    this.bandwidth = bandwidth;
    this.motorLow = motorLow;
    this.motorHigh = motorHigh;
    WallFollowingLab.leftMotor.setSpeed(motorHigh); // Start robot moving forward
    WallFollowingLab.rightMotor.setSpeed(motorHigh);
    WallFollowingLab.leftMotor.backward();
    WallFollowingLab.rightMotor.backward();

  }

  @Override
  public void processUSData(int distance) {
    this.distance = (int)(Math.sqrt(2.0) * (double)distance);
    
    int error = this.bandCenter - this.distance;   
    int absError = error > 0 ? error: -1*error;
    
    // just right
    if (absError < this.bandwidth)
    	setMotorsSpeed(this.motorHigh + SPEEDDELTA, this.motorHigh + SPEEDDELTA);
    // too close
    else if (error > 0)
    	setMotorsSpeed(this.motorHigh + 2*SPEEDDELTA, this.motorHigh - 2*SPEEDDELTA);
    // too far
    else 
    	setMotorsSpeed(this.motorHigh, this.motorHigh + SPEEDDELTA);
  }
  
  private void setMotorsSpeed(int leftSpeed, int rightSpeed) {
	  WallFollowingLab.leftMotor.setSpeed(leftSpeed);
	  WallFollowingLab.rightMotor.setSpeed(rightSpeed);
	  WallFollowingLab.leftMotor.backward();
	  WallFollowingLab.rightMotor.backward();
  }

  @Override
  public int readUSDistance() {
    return this.distance;
  }
}
