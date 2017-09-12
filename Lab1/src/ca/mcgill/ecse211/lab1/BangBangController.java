package ca.mcgill.ecse211.lab1;

import lejos.hardware.motor.*;

public class BangBangController implements UltrasonicController {

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
    WallFollowingLab.leftMotor.forward();
    WallFollowingLab.rightMotor.forward();
  }

  @Override
  public void processUSData(int distance) {
    this.distance = distance;
    int error = this.bandCenter - distance;
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
	  WallFollowingLab.leftMotor.forward();
	  WallFollowingLab.rightMotor.forward();
  }

  @Override
  public int readUSDistance() {
    return this.distance;
  }
}